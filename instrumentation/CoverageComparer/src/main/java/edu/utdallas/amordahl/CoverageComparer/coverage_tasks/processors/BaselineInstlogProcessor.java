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
		String[] tokens = line.split(":");
		if (tokens.length < 2) return records;
		String actualName = tokens[0];
		try {
			records.add(new SimpleLineCoverageRecord(
					String.format("%s:%d", actualName, Integer.valueOf(tokens[1]))));
			logger.debug("Records equals {}", records.toString());
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			logger.info("Could not convert {} to an integer. Skipping this line.", tokens[1]);
		}
		return records;
	}

	public String getName() { return "BASELINE INSTLOG PROCESSOR"; }

	@Override
	protected boolean allowParallelLineProcessing() {
		return true;
	}

}
