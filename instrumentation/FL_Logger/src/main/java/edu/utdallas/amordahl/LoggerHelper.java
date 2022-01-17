package edu.utdallas.amordahl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private static Map<String, Integer> covered = new HashMap<String, Integer>();

	/**
	 * Threadsafe way to log coverage information. Will await a lock on the file
	 * before writing to prevent crashes.
	 * 
	 * @param linenumber The linenumber to log that was covered.
	 * @param location   The class in which the coverage is being recorded.
	 * @throws IOException
	 */
	public static void logCoverageInfo(int linenumber, String location) throws IOException {
		logger.debug("Logging coverage info.");
		String fullLoc = String.format("%s:%d", location, linenumber).replaceAll("\\P{Print}", "");
		synchronized (covered) {
			if (covered.containsKey(fullLoc)) {
				covered.put(fullLoc, covered.get(fullLoc) + 1);
				return;
			} else {
				covered.put(fullLoc, 1);
				System.out.println("COVERAGE:" + fullLoc);
			}
		}
	} 

	public static void logDataStructure(Object obj, String name, int lineNumber) {
		logDataStructure(obj, name, lineNumber, 0, "size", "UNKNOWN");
	}

	private static void logDataStructureInfo(Object obj, String dataType, String name, int lineNumber, int index, 
			SupportedInstrumentations type, String content) {
		System.out.println(String.format("DATASTRUCTURE:%s:%d-%d,%s,%s",
				name, lineNumber, index, dataType, content));
	}
	
	public static void logDataStructure(Object obj, String name, int lineNumber, int index, 
			String dataType, String instrumentationType) {
		logger.debug("logDataStructure called from location {}:{}-{} with datatype {}", name, lineNumber, index, dataType);
		switch (SupportedInstrumentations.valueOf(instrumentationType.toUpperCase())) {
		case SIZE:
			if (obj == null) obj = new String[] {};

			if (obj instanceof Collection) {
				logDataStructureInfo(obj, dataType, name, lineNumber, index, SupportedInstrumentations.SIZE,
						Integer.toString(((Collection<?>)obj).size()));
			} else if (obj instanceof Map) {
				logDataStructureInfo(obj, dataType, name, lineNumber, index, SupportedInstrumentations.SIZE,
						Integer.toString(((Map<?, ?>)obj).size()));
			} else {
				logger.error("Could not cast data structure at {}:{} of type {} to collection or map.",
								name, lineNumber, dataType);
			}
			break;
		case CONTENT:
			if (obj == null) obj = new String[] {};

			logDataStructureInfo(obj, dataType, name, lineNumber, index,
					SupportedInstrumentations.CONTENT, obj.toString());
			break;
		case NULL:
			// Three potential values:
			// 1 = null, or empty collection
			// 0 = neither
			int result;
			if (obj == null) {
				result = 1;
			} else {
				if (obj instanceof Collection) {
					Collection<?> objAsCollection = ((Collection<?>)obj);
					result = objAsCollection.size() == 0 ? 1 : 0;
				} else {
					result = 0;
				}
			}
			logDataStructureInfo(obj, dataType, name, lineNumber, index,
					SupportedInstrumentations.NULL, Integer.toString(result));
			break;
		default:
			logger.error("Instrumentation type %s could not be parsed.", instrumentationType);
				
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
