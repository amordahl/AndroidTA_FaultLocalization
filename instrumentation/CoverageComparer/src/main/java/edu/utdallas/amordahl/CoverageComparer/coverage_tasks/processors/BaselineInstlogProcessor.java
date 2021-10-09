package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

	private Collection<CoveredLine> readInstLogFile(Path p) {
		logger.trace("In readInstLogFile");
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
		return fileContent;
	}

	private Map<Path, Collection<CoveredLine>> mapPathToMap(Set<Path> ps) {
		logger.trace("In mapPathToMap");
		return ps.parallelStream().collect(Collectors.toMap(p -> p, p -> readInstLogFile(p)));
	}
	
	public String getName() { return "BASELINE INSTLOG PROCESSOR"; }

}
