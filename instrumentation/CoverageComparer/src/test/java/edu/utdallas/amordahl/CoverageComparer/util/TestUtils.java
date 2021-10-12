package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTaskReader;

public class TestUtils {

	public static String getPathFromResource(String fileLocation) {
		return TestUtils.class.getResource(fileLocation).getFile();
	}

	public static Set<Path> fixPaths(Set<Path> sp) {
		return sp.stream().map(p -> Paths.get(TestUtils.getPathFromResource(p.toString()))).collect(Collectors.toSet());
	}

	public static List<Path[]> getTestsAndAnswerKeys(String indexFile) {
		Path resolvedIndexFile = Paths.get(getPathFromResource(indexFile));
		List<Path[]> testsAndAnswerKeys = new ArrayList<>();
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jObj = (JSONObject) jsonParser.parse(new FileReader(resolvedIndexFile.toFile()));
			JSONObject tests = (JSONObject) jObj.get("tests");
			for (Object key : tests.keySet()) {
				Path[] pair = new Path[] { Paths.get(getPathFromResource((String) key)),
						Paths.get(getPathFromResource((String) tests.get(key))) };
				testsAndAnswerKeys.add(pair);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testsAndAnswerKeys;
	}

}
