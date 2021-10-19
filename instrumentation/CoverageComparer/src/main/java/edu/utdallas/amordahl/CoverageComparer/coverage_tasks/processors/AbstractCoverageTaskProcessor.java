package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;
import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

public abstract class AbstractCoverageTaskProcessor<S extends CoverageRecord<?, ?>> {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractCoverageTaskProcessor.class);
	private boolean readIntermediates;
	
	/**
	 * Constructs an AbstractCoverageTaskProcessor that reads in intermediate files.
	 */
	public AbstractCoverageTaskProcessor() {
		this.readIntermediates = false;
	}
	
	/**
	 * Constructs an AbstractCoverageTaskProcessor.
	 * @param readIntermediates Whether to read in intermediate files. If false, will read all files from scratch regardless of whether an intermediate file exists.
	 */
	public AbstractCoverageTaskProcessor(boolean readIntermediates) {
		this.readIntermediates = readIntermediates;
	}
	
	/**
	 * Given a path, return the intermediate name.
	 * @param p The path of the instrumentation log file.
	 * @return The path to store or read the intermediate results from.
	 */
	protected abstract Path getIntermediateName(Path p);
	
	/**
	 * Gets the name of this processor.
	 * @return The name of the processor.
	 */
	public abstract String getName();
	
	/**
	 * 
	 * @param ps A set of paths, pointing to instrumentation logs.
	 * @return A map, mapping each path in ps to its content.
	 */
	private Map<Path, Collection<S>> mapPathToMap(Set<Path> ps) {
		logger.trace("In mapPathToMap with argument {}", ps);
		return ps.parallelStream().collect(Collectors.toMap(p -> p, p -> readInstFileOrGetIntermediate(p)));
	}

	/**
	 * Given a coverage task, reads in all of the content in its passed and failed sets.
	 * @param ct A CoverageTask.
	 * @return The PassedFailed object, representing the content of the CoverageTask.
	 */
	public PassedFailed<S> processCoverageTask(CoverageTask ct) {
		logger.trace("Entered processCoverageTask.");
		Map<Path, Collection<S>> passed = mapPathToMap(ct.getPassed());
		Map<Path, Collection<S>> failed = mapPathToMap(ct.getFailed());
		PassedFailed<S> pf = new PassedFailed<S>();
		pf.setPassed(passed);
		pf.setFailed(failed);
		return pf;
	}

	/**
	 * Given a path, will check if there is an intermediate file available (if {@link #readIntermediates} is true). If it exists, it will read it via {@link #readSetFromFile(Path)}. Otherwise, it will read in via {@link #readInstFile(Path)} and write the intermediate file via {@link #writeSetToFile(Collection, Path)}.
	 * @param p The path to the instrumentation log.
	 * @return A collection, representing the content of that file.
	 */
	protected Collection<S> readInstFileOrGetIntermediate(Path p) {
		logger.trace("In readInstLogFile with argument {}", p);
		// First, check for intermediate files.
		Path intermediate = getIntermediateName(p);
		if (intermediate != null && Files.exists(intermediate) && this.readIntermediates) {
			logger.debug("Intermediate file for {} found.", p.toString());
			Collection<S> intermediateContent = readSetFromFile(intermediate); 
			return intermediateContent;
		}
		
		// If we get here, the previous line did not return and thus, we need to read from scratch.
		logger.debug("Not reading in intermediate file for {}", p.toString());
		Collection<S> fileContent = readInstFile(p);
		writeSetToFile(fileContent, intermediate);
		return fileContent;
	}
	 
	/**
	 * Reads in the content from an intermediate file.
	 * @param intermediate The path to the intermediate file.
	 * @return The content of the intermediate file.
	 */
	protected Collection<S> readSetFromFile(Path intermediate) {
		logger.trace("In readSetFromFile.");
		Collection<S> result = null;
		try (FileInputStream f = new FileInputStream(intermediate.toFile());
				ObjectInputStream o = new ObjectInputStream(f)) {
			result = (Collection<S>) o.readObject();
			logger.debug("Successfully read in object of size {} from intermediate file {}", result.size(), intermediate.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Writes a collection to a file.
	 * @param content The collection to write.
	 * @param intermediate The path to the intermediate file.
	 */
	protected void writeSetToFile(Collection<S> content, Path intermediate) {
		logger.trace("In writeSetToFile");
		try (FileOutputStream f = new FileOutputStream(intermediate.toFile());
				ObjectOutputStream o = new ObjectOutputStream(f)) {
			o.writeObject(content);
			logger.debug("Successfully wrote intermediate file {}", intermediate.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Given a path, reads in its contents. Client must override.
	 * @param p A path to an instrumentation file.
	 * @return A list of its contents.
	 */
	protected Collection<S> readInstFile(Path p) {
		logger.trace("In readInstLogFile with argument {}", p);
		ArrayList<S> fileContent = new ArrayList<>();
		try (Scanner sc = new Scanner(p.toFile())) {
			while (sc.hasNextLine()) {
				// Replace non-printable characters
				String line = sc.nextLine().replaceAll("\\P{Print}", "");
				Collection<S> cr = processLine(line);
				fileContent.addAll(cr);
			}
		} catch (FileNotFoundException e) {
			logger.error("Could not find path {}. Returning empty coverage set.", p.toString());
		}
		return fileContent;
	}

	/**
	 * How to process an individual line in the coverage record. Clients must override.
	 * A line may produce anywhere from 0 to many records.
	 * @param line The line from the instrumentation file.
	 * @return A collection of <S>, indicating the result of processing that line.
	 */
	public abstract Collection<S> processLine(String line);
}
