package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;

public class BaselineInstlogProcessor extends AbstractCoverageTaskProcessor<CoverageRecord<String, Boolean>> {

	private Pair<Integer, String> processMappingLines(String line) {
		String[] tokens = line.split("=");
		return new ImmutablePair<Integer, String>(Integer.valueOf(tokens[1]), tokens[0]);
	}
	 
	private CoverageRecord<String, Boolean> processNonMappingLine(Map<Integer, String> mappings, String line) {
		String[] tokens = line.split(":");
		String actualName;
		try {
			actualName = mapping.get(Integer.valueOf(tokens[0]));
		} catch (NumberFormatException nfe) {
			logger.warn("Could not cast {} to int. Instead, using raw value as name.", tokens[0]);
			actualName = tokens[0];
		}
		return new CoverageRecord<String, Boolean>(
				String.format("%s:%d", actualName, Integer.valueOf(tokens[1])), Boolean.class, true);
	}
	@Override
	protected Collection<CoverageRecord<String, Boolean>> readInstFile(Path p) {
		System.out.println(String.format("Trying to read in file %s.", p.toString()));
		StopWatch readingTime = new StopWatch();
		readingTime.start();
		try {
			logger.trace("In readInstLogFile with argument {}", p);
			Map<Integer, String> locationMapping = 
					Files.lines(p).parallel().filter(s -> s.contains("=")).map(s -> processMappingLines(s)).collect(Collectors.toMap(k -> k.getKey(), k -> k.getValue()));
			List<CoverageRecord<String, Boolean>> fileContent = Files.lines(p).parallel().filter(s -> !s.contains("=")).map(s -> processNonMappingLine(locationMapping, s)).collect(Collectors.toList());
			readingTime.stop();
			System.out.println(String.format("Finished reading in file %s. Took %d seconds.", p.toString(), readingTime.getTime()/1000));
			return fileContent;
		
		} catch (FileNotFoundException e) {
			logger.error("Could not find path {}. Returning empty coverage set.", p.toString());
		} catch (IOException e) {
			logger.error("Error reading in path {}. Returning empty coverage set.", p.toString());
		}
		return null;
	}
	
	private static Logger logger = LoggerFactory.getLogger(BaselineInstlogProcessor.class);

	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".intermediate");
	}
		
	public BaselineInstlogProcessor() { super(); }
	public BaselineInstlogProcessor(boolean readIntermediates) {
		super(readIntermediates);
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
					String.format("%s:%d", actualName, Integer.valueOf(tokens[1])), Boolean.class, true));
			logger.debug("Records equals {}", records.toString());
		}
		return records;
	}
	
	public String getName() { return "BASELINE INSTLOG PROCESSOR"; }

}
