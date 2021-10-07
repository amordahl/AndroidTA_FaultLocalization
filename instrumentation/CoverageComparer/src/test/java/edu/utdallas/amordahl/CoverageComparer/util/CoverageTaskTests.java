package edu.utdallas.amordahl.CoverageComparer.util;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CoverageTaskTests {
	
	private static Path TEST_LOCATIONS = Paths.get(CoverageTaskTests.class.getResource("/coverage_task_test_resources/index.json").getFile());
	private static List<Path[]> testsAndAnswerKeys = new ArrayList<Path[]>();
	
	private static String getPathFromResource(String path) {
		return CoverageTaskTests.class.getResource(path).getFile();
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
	
	@Parameters
	public static Collection<Path[]> getParams() {
		setup();
		return testsAndAnswerKeys;
	}

	private Path coverageTask;
	private JSONObject answerKey;
	private CoverageTask ct;
	
	public CoverageTaskTests(Path coverageTask, Path answerKey) throws FileNotFoundException, IOException, ParseException {
		this.coverageTask = coverageTask;
		this.ct = CoverageTaskReader.getCoverageTaskFromFile(this.coverageTask);
		this.answerKey = (JSONObject) new JSONParser().parse(new FileReader(answerKey.toFile()));
	}
	
	/**
	 * Check that the size of the failed tasks are correct.
	 */
	@Test
	public void testCoverageTaskFailed() {
		assertEquals(ct.getFailed().size(), ((Long) this.answerKey.get("failed_size")).intValue());
	}
	
	@Test
	public void testCoverageTaskPassed() {
		assertEquals(ct.getPassed().size(),((Long)this.answerKey.get("passed_size")).intValue());
	}
	
	@Test
	public void testCoverageTaskLocalization() {
		assertEquals(ct.getLocalization().toString(), ((String)this.answerKey.get("localization")).toUpperCase());
	}
	
}
