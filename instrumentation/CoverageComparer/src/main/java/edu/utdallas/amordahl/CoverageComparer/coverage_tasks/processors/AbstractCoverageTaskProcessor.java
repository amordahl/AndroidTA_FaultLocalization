package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.WriteAbortedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.util.ICoverageRecord;
import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

/**
 * Superclass of all task processors.
 * @author austin
 *
 * @param <S> Whatever the coverage log records (e.g., line, data structure, etc).
 * @param <T> The value of each coverage log record (e.g., a line is present or not (boolean)).
 */
public abstract class AbstractCoverageTaskProcessor<S, T> {

	private static Logger logger = LoggerFactory.getLogger(AbstractCoverageTaskProcessor.class);
	private boolean readIntermediates;

	/**
	 * Constructs an AbstractCoverageTaskProcessor that reads in intermediate files.
	 */
	public AbstractCoverageTaskProcessor() {
		this.readIntermediates = true;
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
	 * Whether to allow parallel processing of lines.
	 * @return True if parallel processing is allowed, false otherwise.
	 */
	protected abstract boolean allowParallelLineProcessing();
	
	/**
	 * Processes the actual files.
	 * 
	 * Uses the value of {@link #allowParallelLineProcessing()} to determine whether to allow
	 * parallel file processing. If {@link #allowParallelLineProcessing()} is true, then we
	 * expect a lot of memory usage from reading in an individual file, because in order to
	 * process lines in parallel we read in the whole thing. Alternatively, if {@link #allowParallelLineProcessing()}
	 * is false, then we will take advantage of Scanner's lazy loading to save memory. In that case, we 
	 * can use a parallel stream to read in files.
	 * 
	 * @param ps A set of paths, pointing to instrumentation logs.
	 * @return A map, mapping each path in ps to its content.
	 */
//	private Map<Path, Collection<S>> mapPathToMap(Set<Path> ps) {
//		logger.trace("In mapPathToMap with argument {}", ps);
////		if (this.allowParallelLineProcessing()) {
////			return ps.stream().collect(Collectors.toMap(p -> p, p -> readInstFileOrGetIntermediate(p)));
////		} else {
//			return ps.parallelStream().collect(Collectors.toMap(p -> p, p -> readInstFileOrGetIntermediate(p)));
////		}
//	}

	/**
	 * Given a coverage task, reads in all of the content in its passed and failed sets.
	 * @param ct A CoverageTask.
	 * @return The PassedFailed object, representing the content of the CoverageTask.
	 */
	public PassedFailed<S, T> processCoverageTask(CoverageTask ct) {
		logger.trace("Entered processCoverageTask.");
		
		PassedFailed<S, T> pf = new PassedFailed<S, T>();
		pf.setPassed(ct.getPassed());
		pf.setFailed(ct.getFailed());
		if (ct.getOther() != null) {
			pf.setOther(ct.getOther());
		}
		pf.setOriginatingTask(ct);
		pf.getAllFiles().parallelStream().forEach(p -> pf.setAllValuesForPath(p, readInstFileOrGetIntermediate(p)));
		return pf;
	}

	/**
	 * Given a path, will check if there is an intermediate file available (if {@link #readIntermediates} is true). If it exists, it will read it via {@link #readSetFromFile(Path)}. Otherwise, it will read in via {@link #readInstFile(Path)} and write the intermediate file via {@link #writeSetToFile(Collection, Path)}.
	 * @param p The path to the instrumentation log.
	 * @return A collection, representing the content of that file.
	 */
	protected Map<S, T> readInstFileOrGetIntermediate(Path p) {
		logger.trace("In readInstLogFile with argument {}", p);
		// First, check for intermediate files.
		Path intermediate = getIntermediateName(p);

		try {
			if (intermediate != null && Files.exists(intermediate) && this.readIntermediates) {
				logger.debug("Intermediate file for {} found.", p.toString());
				Map<S, T> intermediateContent = readSetFromFile(intermediate);
				return intermediateContent;
			}
		} catch (IOException | ClassNotFoundException ex) {
			logger.debug("Error trying to read in intermediate file.", ex);
		}

		// If we get here, the previous line did not return and thus, we need to read from scratch.
		logger.debug("Not reading in intermediate file for {}", p.toString());
		Map<S, T> fileContent = readInstFile(p);
		try {
			writeSetToFile(fileContent, intermediate);
		} catch (IOException e) {
			logger.debug("Error trying to write intermediate file.", e);
		}

		return fileContent;
	}

	/**
	 * Reads in the content from an intermediate file.
	 * @param intermediate The path to the intermediate file.
	 * @return The content of the intermediate file.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	protected Map<S, T> readSetFromFile(Path intermediate) throws IOException, ClassNotFoundException {
		logger.trace("In readSetFromFile.");
		Map<S, T> result = null;
		try (FileInputStream f = new FileInputStream(intermediate.toFile());
				ObjectInputStream o = new ObjectInputStream(f)) {
			result = (Map<S, T>) o.readObject();
			logger.debug("Successfully read in object of size {} from intermediate file {}", result.size(), intermediate.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw e;
		} 
		return result;
	}

	/**
	 * Writes a collection to a file.
	 * @param fileContent The collection to write.
	 * @param intermediate The path to the intermediate file.
	 * @throws IOException 
	 */
	protected void writeSetToFile(Map<S, T> fileContent, Path intermediate) throws IOException {
		logger.trace("In writeSetToFile");
		try (FileOutputStream f = new FileOutputStream(intermediate.toFile());
				ObjectOutputStream o = new ObjectOutputStream(f)) {
			o.writeObject(fileContent);
			logger.debug("Successfully wrote intermediate file {}", intermediate.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	/**
	 * Given a path, reads in its contents. Client must override.
	 * @param p A path to an instrumentation file.
	 * @return A list of its contents.
	 */
	protected Map<S, T> readInstFile(Path p) {
		logger.trace("In readInstFile with argument {}", p);
		Collection<String> fileContent = new ArrayList<String>();
		Map<S, T> processedContent = new HashMap<S, T>();
		try (Scanner sc = new Scanner(p.toFile())) {
			while (sc.hasNextLine()) {
				// Replace non-printable characters
				String line = sc.nextLine().replaceAll("\\P{Print}", "");
					// Put it here so we can take advantage of Scanner's lazy loading -- otherwise it would load
					//  the whole file into memory even if we then wanted to process each line sequentially.
					Map<S, T> cr = processLine(line);
					processedContent.putAll(cr);
				}
		} catch (FileNotFoundException e) {
			logger.error("Could not find path {}. Returning empty coverage set.", p.toString());
		}
//		if (this.allowParallelLineProcessing()) {
//			// If we do allow parallel line processing, then we instead allow the entire file to be read in
//			// and stored in memory, and then process it all at once. To compensate for this,
//			// !this.allowParallelLineProcessing() is used to determine whether to process multiple files at once.
//			fileContent.parallelStream().forEach(s -> processedContent.putAll(processLine(s)));
//		}
		return processedContent;
	}

	/**
	 * How to process an individual line in the coverage record. Clients must override.
	 * A line may produce anywhere from 0 to many records.
	 * @param line The line from the instrumentation file.
	 * @return A collection of <S>, indicating the result of processing that line.
	 */
	public abstract Map<S, T> processLine(String line);
}
