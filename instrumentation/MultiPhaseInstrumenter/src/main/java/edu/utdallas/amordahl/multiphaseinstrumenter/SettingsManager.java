package edu.utdallas.amordahl.multiphaseinstrumenter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsManager {
	private static Map<String, String> settingsMap = new HashMap<String,String>();
	private static Logger logger = LoggerFactory.getLogger(SettingsManager.class);
	
	private Path coverageFile;
	
	public static Path getCoverageFile() {
		try {		
			return settingsMap.containsKey("coverage") ? 
					Paths.get(settingsMap.get("coverage")) :
						null;
		} catch (InvalidPathException ipe) {
			throw new RuntimeException(String.format("Could not "
					+ "convert coverage file parameter %s to path.", settingsMap.get("coverage")));
		}
	}
	
	/**
	 * Options are passed to a javaagent as follows:
	 * java -javaagent:<path/to/agent>=args
	 * @param args A series of key value pairs, specified as key1,val1;key2,val2
	 */
	public static void parseOpts(String args) {
		String[] tokens = args.split(";");
		for (String t: tokens) {
			String[] keyVal = t.split(",");
			if (keyVal.length != 2) {
				throw new RuntimeException(String.format("Malformed argument string %s", t));
			}
			logger.info(String.format("Argument %s passed with value %s", keyVal[0], keyVal[1]));
			settingsMap.put(keyVal[0].toLowerCase(), keyVal[1]);
		}
		SettingsManager.sanityCheck();
	}
	
	public static String get(String key) {
		return settingsMap.get(key);
	}
	
	/**
	 * Checks that the settings were entered correctly.
	 */
	private static void sanityCheck() {
		return;
	}
}
