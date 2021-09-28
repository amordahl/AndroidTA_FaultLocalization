package edu.utdallas.amordahl.coverage_instrumenter.phase2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.LoggerHelper;

public class LoggerAgent {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LoggerAgent.class);

	public static void premain(String args, Instrumentation instrumentation) throws IOException {		
		Set<LineCoverageRecord> covered = populateCoverageFile(args);
		PrimaryTransformer transformer = new PrimaryTransformer(covered);
		instrumentation.addTransformer(transformer);
		LoggerHelper.printCoverageInfo();
	}

	/**
	 * Reads in a coverage file and returns a set of covered lines.
	 * @param args The arguments passed to the JavaAgent -- should only be a single file name.
	 * @return A set of LineCoverageRecords.
	 * @throws IOException
	 */
	private static Set<LineCoverageRecord> populateCoverageFile(String args)
			throws IOException {
		// Open the coverage file and read in the lines.
		Set<LineCoverageRecord> covered = new HashSet<LineCoverageRecord>();
		if (args != null && args != "") {
			logger.info(String.format("Trying to read coverage from %s", args));
			try (FileReader fr = new FileReader(Paths.get(args).toFile()); BufferedReader br = new BufferedReader(fr)) {
				covered = br.lines().map(l -> LineCoverageRecordFactory.makeLineCoverageRecord(l))
						.filter(r -> r != null).collect(Collectors.toSet());
				logger.info(String.format("Read in %d lines from coverage file.", covered.size()));
			} catch (FileNotFoundException fnfe) {
				logger.error(String.format("Could not find file %s", args));
			}
		}
		return covered;
	}
}