package edu.utdallas.amordahl;

import java.awt.RenderingHints.Key;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

public class LoggerHelper {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);

	private static HashMap<String, Wrapped> LOGS = new HashMap<>();
	private static int NUM_ITERS = 0;
	private static int LAST_SIZE = -1;
	private static long LAST_TIME = new Date().getTime();
	private static FileWriter fw;
	private static BufferedWriter bw;
	private static HashMap<String, Integer> classToInt = new HashMap<String, Integer>();

	/**
	 * Threadsafe way to log coverage information. Will await a lock on the file
	 * before writing to prevent crashes.
	 * 
	 * @param linenumber The linenumber to log that was covered.
	 * @param location   The class in which the coverage is being recorded.
	 * @throws IOException
	 */
	public static void logCoverageInfo(int linenumber, String location) throws IOException {
		Integer mapping;
		synchronized (classToInt) {
			if (classToInt.containsKey(location)) {
				mapping = classToInt.get(location);
			} else {
				// Find maximum key
				if (classToInt.size() == 0) {
					classToInt.put(location, Integer.valueOf(1));
				} else {
					Integer max = Integer.valueOf(-1);
					for (String key : classToInt.keySet()) {
						if (classToInt.get(key) > max)
							max = classToInt.get(key);
					}
					classToInt.put(location, max + 1);
				}
				mapping = classToInt.get(location);
				System.out.println(String.format("%s=%d", location, mapping));
			}
		}
		System.out.println(String.format("%d:%d", mapping, linenumber));
	}

	public static void logDataStructure(Object obj, String name, int lineNumber) {
		logDataStructure(obj, name, lineNumber, "size");
	}

	private static void logDataStructureInfo(Object obj, String name, int lineNumber, 
			SupportedInstrumentations type, String content) {
		System.out.println(String.format("Data structure at location %s:%d (%s) is %s",
				name, lineNumber, type.toString(), content));
	}
	
	public static void logDataStructure(Object obj, String name, int lineNumber, String type) {
		if (obj == null) obj = new String[] {};
		switch (SupportedInstrumentations.valueOf(type.toUpperCase())) {
		case SIZE:
			try {
				logDataStructureInfo(obj, name, lineNumber, SupportedInstrumentations.SIZE,
						Integer.toString(((Collection<?>)obj).size()));
			} catch (ClassCastException cce) {
				logger.error("Could not cast data structure at {}:{} to collection.",
								name, lineNumber);
			}
			break;
		case CONTENT:
			logDataStructureInfo(obj, name, lineNumber, 
					SupportedInstrumentations.CONTENT, obj.toString());
			break;
		case NULL:
			logDataStructureInfo(obj, name, lineNumber, 
					SupportedInstrumentations.NULL, (obj == null) ? "1" : "0");
			break;
		default:
			logger.error("Instrumentation type %s could not be parsed.", type);
				
		}
	}

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

	protected void finalize() throws IOException {
		if (bw != null)
			bw.close();
		if (fw != null)
			fw.close();
	}
}
