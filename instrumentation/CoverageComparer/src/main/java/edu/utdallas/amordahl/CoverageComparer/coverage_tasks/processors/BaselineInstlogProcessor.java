package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaselineInstlogProcessor extends AbstractCoverageTaskProcessor<String, Boolean> {

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
	public Map<String, Boolean> processLine(String line) {
		logger.trace("Called processLine with {}", line);
		Map<String, Boolean >records = new HashMap<String, Boolean>();
		String[] tokens = line.split(":");
		if (tokens.length < 2) return records;
		String actualName = tokens[0];
		try {
			records.put(String.format("%s:%d", actualName, Integer.valueOf(tokens[1])), true);
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
