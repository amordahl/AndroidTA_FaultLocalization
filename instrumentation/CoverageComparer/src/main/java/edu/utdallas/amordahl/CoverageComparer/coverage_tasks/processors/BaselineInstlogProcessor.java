package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.util.SimpleLineCoverageRecord;

public class BaselineInstlogProcessor extends AbstractCoverageTaskProcessor<SimpleLineCoverageRecord> {

	private SimpleLineCoverageRecord processNonMappingLine(Map<Integer, String> mappings, String line) {
		String[] tokens = line.split(":");
		String actualName;
		try {
			actualName = mapping.get(Integer.valueOf(tokens[0]));
		} catch (NumberFormatException nfe) {
			logger.info("Could not cast {} to int. Instead, using raw value as name.", tokens[0]);
			actualName = tokens[0];
		}
		try {
		    return new SimpleLineCoverageRecord(String.format("%s:%d", actualName, Integer.valueOf(tokens[1])));
		} catch (NumberFormatException nfe) {
		    // TODO: Actually handle this. For now, leaving this as a dirty patch.
		    return null;
		}
	}
	@Override
	protected Collection<SimpleLineCoverageRecord> readInstFile(Path p) {
		logger.info(String.format("Trying to read in file %s.", p.toString()));
		StopWatch readingTime = new StopWatch();
		readingTime.start();
		try {
			logger.trace("In readInstLogFile with argument {}", p);
			Map<Integer, String> locationMapping = new HashMap<Integer, String>();
			// Hashset to make lookup cheaper, localization was taking forever because of this.
			HashSet<SimpleLineCoverageRecord> fileContent = (HashSet<SimpleLineCoverageRecord>) Files.lines(p).parallel().filter(s -> !s.contains("=") && s.contains(":")).map(s -> processNonMappingLine(locationMapping, s)).filter(s -> s != null).collect(Collectors.toSet());
			readingTime.stop();
			logger.info(String.format("Finished reading in file %s. Took %d seconds.", p.toString(), readingTime.getTime()/1000));
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
	public Collection<SimpleLineCoverageRecord> processLine(String line) {
		logger.trace("Called processLine with {}", line);
		Collection<SimpleLineCoverageRecord> records = new ArrayList<SimpleLineCoverageRecord>();
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
			records.add(new SimpleLineCoverageRecord(
					String.format("%s:%d", actualName, Integer.valueOf(tokens[1]))));
			logger.debug("Records equals {}", records.toString());
		}
		return records;
	}
	
	public String getName() { return "BASELINE INSTLOG PROCESSOR"; }

}
