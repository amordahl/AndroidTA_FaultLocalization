package edu.utdallas.amordahl.multiphaseinstrumenter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.SupportedInstrumentations;

public class SettingsManager {
	private static Map<String, String> settingsMap = new HashMap<String, String>();
	private static Logger logger = LoggerFactory.getLogger(SettingsManager.class);

	public static Set<SupportedInstrumentations> getInstrumentationType() {
		Set<SupportedInstrumentations> result = new HashSet<>();
		if (settingsMap.containsKey("type")) {
			String val = settingsMap.get("type");
			String[] vals = new String[] { val };
			if (vals[0].contains(",")) {
				vals = vals[0].split(",");
			}
			for (String v : vals) {
				result.add(SupportedInstrumentations.valueOf(v.toUpperCase()));
			}
		} else {
			result.add(SupportedInstrumentations.CONTENT);
		}
		if (result.size() > 1) {
			throw new UnsupportedOperationException("Have not implemented multiple types of instrumentation at once.");
		}
		return result;
	}

	public static Path getCoverageFile() {
		logger.trace("Inside getCoverageFile");
		try {
			return settingsMap.containsKey("coverage") ? Paths.get(settingsMap.get("coverage")) : null;
		} catch (Exception ipe) {
			throw new RuntimeException(String.format("Could not " + "convert coverage file parameter %s to path.",
					settingsMap.get("coverage")));
		}
	}

	/**
	 * Options are passed to a javaagent as follows: java
	 * -javaagent:<path/to/agent>=args
	 * 
	 * @param args A series of key value pairs, specified as key1,val1;key2,val2
	 */
	public static void parseOpts(String args) {
		String[] tokens = args.split(";");
		for (String t : tokens) {
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
