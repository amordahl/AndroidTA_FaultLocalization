package edu.utdallas.amordahl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

public class LoggerHelper {

	private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
	private static HashMap<String, Wrapped> LOGS = new HashMap<>();
	private static HashMap<String, Integer> COVERAGE = new HashMap<>();
	private static int NUM_ITERS = 0;
	private static int LAST_SIZE = -1;
	private static long LAST_TIME = new Date().getTime();
	private static FileWriter fw;

	private static BufferedWriter bw;
	public static void logCoverageInfo(int linenumber, String location) throws IOException {
		if (fw == null) fw = new FileWriter(FLPropReader.getInstance().getOutputFile().toFile(), true);
		if (bw == null && fw != null) bw = new BufferedWriter(fw);
		synchronized (bw) {
			bw.write(String.format("%s:%d\n", location, linenumber));
		}
		/*
		 * // logger.debug("LogCoverageInfo called."); // while (!canWrite) {} //
		 * canWrite = false; // try (FileWriter fw = new
		 * FileWriter(FLPropReader.getInstance().getOutputFile().toFile(), true); //
		 * BufferedWriter bw = new BufferedWriter(fw)) { String fullLocation =
		 * String.format("logCoverageInfo %s:%d", location, linenumber);
		 * System.out.println(fullLocation); // bw.write(fullLocation); if
		 * (!COVERAGE.containsKey(fullLocation)) { COVERAGE.put(fullLocation,
		 * Integer.valueOf(0)); } COVERAGE.put(fullLocation, COVERAGE.get(fullLocation)
		 * + 1); // }
		 */	}
	
	public static void logObjArray(Object[] objs, String location) throws Exception {
		LOGS.put(location, Wrapper.wrapObject(objs));
		if (LOGS.size() > LAST_SIZE) {
			FileWriter fw = new FileWriter("/Users/austin/Desktop/results.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			long newTime = new Date().getTime();
			bw.write(String.format("Size changed from %d to %d after %f seconds (time %s)\n", LAST_SIZE, LOGS.size(),
					(newTime - LAST_TIME) / 1000.0, new SimpleDateFormat("HH:mm:ss").format(new Date())));
			LAST_TIME = newTime;
			bw.close();
			fw.close();
		}
		LAST_SIZE = LOGS.size();
		NUM_ITERS++;
		System.out.println(
				String.format("Size of logs: %d (%d iterations) (location %s)", LOGS.size(), NUM_ITERS, location));
	}
	
	public static void printCoverageInfo() {
		System.out.println(COVERAGE);
	}

	protected void finalize() throws IOException {
		if (bw != null) bw.close();
		if (fw != null) fw.close();
	}
}
