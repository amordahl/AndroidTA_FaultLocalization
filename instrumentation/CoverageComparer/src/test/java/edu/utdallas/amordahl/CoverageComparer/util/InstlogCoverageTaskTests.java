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

public class InstlogCoverageTaskTests {
	
	private static Path TEST_LOCATIONS = Paths.get(InstlogCoverageTaskTests.class.getResource("/instlog_tests/coverage_task_test_resources/index.json").getFile());
	protected static List<Path[]> testsAndAnswerKeys = new ArrayList<Path[]>();
	
	private static String getPathFromResource(String path) {
		return InstlogCoverageTaskTests.class.getResource(path).getFile();
	}
	/**
	 * Reads in test pairs from the {@link #TEST_LOCATIONS} file.
	 */
	public static void setup() {
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jObj = (JSONObject) jsonParser.parse(new FileReader(TEST_LOCATIONS.toFile()));
			JSONObject tests = (JSONObject) jObj.get("tests");
			for (Object key: tests.keySet()) {
				Path[] pair = new Path[] { Paths.get(getPathFromResource((String)key)),
						Paths.get(getPathFromResource((String)tests.get(key))) };
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
	}

	private Path coverageTask;
	protected JSONObject answerKey;
	protected CoverageTask ct;
	
	public InstlogCoverageTaskTests(Path coverageTask, Path answerKey) throws FileNotFoundException, IOException, ParseException {
		this.coverageTask = coverageTask;
		this.ct = CoverageTaskReader.getCoverageTaskFromFile(this.coverageTask);
		this.ct.setPassed(fixPaths(ct.getPassed()));
		this.ct.setFailed(fixPaths(ct.getFailed()));
		this.answerKey = (JSONObject) new JSONParser().parse(new FileReader(answerKey.toFile()));
	}

	private Set<Path> fixPaths(Set<Path> sp) {
		return sp.stream().map(p -> Paths.get(InstlogCoverageTaskTests.getPathFromResource(p.toString()))).collect(Collectors.toSet());
	}
	
}
