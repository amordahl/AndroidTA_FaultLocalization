package edu.utdallas.amordahl.CoverageComparer.coverageTasks;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.util.SupportedLocalization;

/**
 * In charge of reading in Coverage tasks from JSON.
 * @author Austin Mordahl
 *
 */
public class CoverageTaskReader {
	
	private static Logger logger = LoggerFactory.getLogger(CoverageTaskReader.class);
	
	private final static SupportedLocalization DEFAULT_LOCALIZATION = SupportedLocalization.TARANTULA;
	/**
	 * Given a file, reads it in and returns the {@link CoverageTask} it represents.
	 * @param path The file to read a coverage record from.
	 * @return The CoverageTask the file represents.
	 */
	public static CoverageTask getCoverageTaskFromFile(Path path) {
		logger.trace("In getCoverageTaskFromFile with argument {}", path);
		if (!path.toString().endsWith("json")) {
			logger.warn("Path {} does not end in .json, yet JSON is the only CoverageTask scheme available. "
					+ "Attempting to read in as JSON anyway.", path);
		}
		
		// Try reading in as JSON.
		CoverageTask ct = null;
		try {
			ct = getCoverageTaskFromJson(path);
		} catch (FileNotFoundException fnfe) {
			logger.error("File {} does not exist.", path);
		} catch (ParseException pe) {
			logger.error("Could not parse file {}", path);
		} catch (IOException ie) {
			logger.error("IOException occurred while trying to read {}. Message is {}", path, ie.getMessage());
		}
		
		logger.trace("Returning {} from getCoverageTaskFromFile.", ct.toString());
		return ct;
	}

	/**
	 * Reads in a {@link CoverageTask} from JSON.
	 * @param path A JSON file.
	 * @return The CoverageTask the JSON file represents.
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static CoverageTask getCoverageTaskFromJson(Path path) throws FileNotFoundException, IOException, ParseException {
		logger.trace("Inside getCoverageTaskFromJson.");
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject)jsonParser.parse(new FileReader(path.toFile()));
		
		// Read in fields.
		JSONArray failed = (JSONArray)jsonObj.get("failed");
		JSONArray passed = (JSONArray)jsonObj.get("passed");
		
		logger.debug("Read in {} objects from the failed field in {}.", failed.size(), path.toString());
		logger.debug("Read in {} objects from the passed field in {}.", passed.size(), path.toString());
		
		// Check if the file specifies a localization scheme.
		SupportedLocalization sl = CoverageTaskReader.DEFAULT_LOCALIZATION;
		if (jsonObj.containsKey("localization")) {
			sl = SupportedLocalization.valueOf((String) jsonObj.get("localization"));
			logger.debug("Read in localization scheme {} from file {}", sl.toString(), path.toString());
		}
		
		// Check if the file specifies pairs.
		JSONObject jsonPairs = (JSONObject)jsonObj.get("pairs");
		Map<Path, Path> pairs = null;
		if (jsonPairs != null) {
			pairs = (Map<Path, Path>) jsonPairs.entrySet().stream()
					.collect(Collectors.toMap(k -> Paths.get((String)k), v -> Paths.get((String)v)));
		}
		
		CoverageTask ct = new CoverageTask(path, new HashSet<Path>(pathify(passed)), new HashSet<Path>(pathify(failed)), sl);
		ct.setPairs(pairs);
		return ct;
	}
	
	/**
	 * Turns a JSON array into a list of paths.
	 * @param jsonArr
	 * @return The same array represented as a List<Path>.
	 */
	private static List<Path> pathify(JSONArray jsonArr) {
		logger.trace("Inside pathify");
		List<Path> result = new ArrayList<Path>();
		for (Object obj: jsonArr) {
			String objAsString = (String)obj;
			try {
				result.add(Paths.get(objAsString));
			} catch (InvalidPathException ipe) {
				logger.warn("Could not convert string {} to path.", objAsString);
			}
		}
		logger.trace("Returning {} from pathify", result);
		return result;
	}

}
