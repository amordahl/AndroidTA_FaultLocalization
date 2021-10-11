package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;
import edu.utdallas.amordahl.CoverageComparer.util.CoveredLine;

public class BaselineInstlogProcessor extends AbstractCoverageTaskProcessor<CoverageRecord<String, Boolean>> {

	private static Logger logger = LoggerFactory.getLogger(BaselineInstlogProcessor.class);

	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".intermediate");
	}
		
	
	private HashMap<Integer, String> mapping = new HashMap<>();

	@Override
	public Collection<CoverageRecord<String, Boolean>> processLine(String line) {
		logger.trace("Called processLine with {}", line);
		Collection<CoverageRecord<String, Boolean>> records = new ArrayList<CoverageRecord<String, Boolean>>();
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
			records.add(new CoverageRecord<String, Boolean>(
					String.format("%s:%d", actualName, Integer.valueOf(tokens[1])), true));
			logger.debug("Records equals {}", records.toString());
		}
		return records;
	}
	
	public String getName() { return "BASELINE INSTLOG PROCESSOR"; }

}
