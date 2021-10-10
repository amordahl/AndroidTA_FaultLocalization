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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.util.CoveredLine;
import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

public class BaselineInstlogProcessor implements ICoverageTaskProcessor<Path, CoveredLine> {

	private static Logger logger = LoggerFactory.getLogger(BaselineInstlogProcessor.class);

	private boolean readIntermediates;
	
	@Override
	public PassedFailed<Path, CoveredLine> processCoverageTask(CoverageTask ct) {
		logger.trace("Entered processCoverageTask.");
		Map<Path, Collection<CoveredLine>> passed = mapPathToMap(ct.getPassed());
		Map<Path, Collection<CoveredLine>> failed = mapPathToMap(ct.getFailed());
		PassedFailed<Path, CoveredLine> pf = new PassedFailed<Path, CoveredLine>();
		pf.setPassed(passed);
		pf.setFailed(failed);
		return pf;
	}
	
	public BaselineInstlogProcessor() {
		this.readIntermediates = true;
	}
	
	/**
	 * 
	 * @param readIntermediates Whether or not to read intermediate files.
	 */
	public BaselineInstlogProcessor(boolean readIntermediates) {
		this.readIntermediates = readIntermediates;
	}

	private Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".intermediate");
	}
		
	private Collection<CoveredLine> readInstLogFile(Path p) {
		logger.trace("In readInstLogFile with argument {}", p);
		// First, check for intermediate files.
		Path intermediate = getIntermediateName(p);
		if (intermediate != null && Files.exists(intermediate) && this.readIntermediates) {
			logger.debug("Intermediate file for {} found.", p.toString());
			Collection<CoveredLine> intermediateContent = readSetFromFile(intermediate); 
			return intermediateContent;
		}
		
		// If we get here, the previous line did not return and thus, we need to read from scratch.
		logger.debug("Not reading in intermediate file for {}", p.toString());
		ArrayList<CoveredLine> fileContent = new ArrayList<>();
		HashMap<Integer, String> mapping = new HashMap<>();
		try (Scanner sc = new Scanner(p.toFile())) {
			while (sc.hasNext()) {
				// Replace non-printable characters
				String line = sc.next().replaceAll("\\P{Print}", "");
				if (line.contains("=")) {
					// Mapping line. Need to store map in hashmap.
					String[] tokens = line.split("=");
					mapping.put(Integer.valueOf(tokens[1]), tokens[0]);
				} else if (line.contains(":")) {
					String[] tokens = line.split(":");
					String actualName;
					try {
						actualName = mapping.get(Integer.valueOf(tokens[0]));
					} catch (NumberFormatException nfe) {
						logger.warn("Could not cast {} to int. Instead, using raw value as name.", tokens[0]);
						actualName = tokens[0];
					}
					fileContent.add(new CoveredLine(actualName, Integer.valueOf(tokens[1])));
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("Could not find path {}. Returning empty coverage set.", p.toString());
		}
		writeSetToFile(fileContent, intermediate);
		return fileContent;
	}

	private Map<Path, Collection<CoveredLine>> mapPathToMap(Set<Path> ps) {
		logger.trace("In mapPathToMap with argument {}", ps);
		return ps.stream().collect(Collectors.toMap(p -> p, p -> readInstLogFile(p)));
	}
	
	public String getName() { return "BASELINE INSTLOG PROCESSOR"; }
	
	private static Collection<CoveredLine> readSetFromFile(Path intermediate) {
		logger.trace("In readSetFromFile.");
		Collection<CoveredLine> result = null;
		try (FileInputStream f = new FileInputStream(intermediate.toFile());
				ObjectInputStream o = new ObjectInputStream(f)) {
			Collection<String> intermediateCollection = (List<String>) o.readObject();
			result = intermediateCollection.stream().map(s -> CoveredLine.fromString(s)).collect(Collectors.toList());
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
	
	private static void writeSetToFile(Collection<CoveredLine> content, Path intermediate) {
		logger.trace("In writeSetToFile");
		try (FileOutputStream f = new FileOutputStream(intermediate.toFile());
				ObjectOutputStream o = new ObjectOutputStream(f)) {
			o.writeObject(content.stream().map(c -> c.toString()).collect(Collectors.toList()));
			logger.debug("Successfully wrote intermediate file {}", intermediate.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
