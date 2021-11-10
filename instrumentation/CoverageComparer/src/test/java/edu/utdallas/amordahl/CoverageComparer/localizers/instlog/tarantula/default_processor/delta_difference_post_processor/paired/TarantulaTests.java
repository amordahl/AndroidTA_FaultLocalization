package edu.utdallas.amordahl.CoverageComparer.localizers.instlog.tarantula.default_processor.delta_difference_post_processor.paired;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.json.simple.parser.ParseException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.AbstractPostProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.DeltaDifferencePostProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.AbstractCoverageTaskProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.BaselineInstlogProcessor;
import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;
import edu.utdallas.amordahl.CoverageComparer.localizers.TarantulaLocalizer;
import edu.utdallas.amordahl.CoverageComparer.util.AnswerKeyBasedTester;
import edu.utdallas.amordahl.CoverageComparer.util.SimpleLineCoverageRecord;
import edu.utdallas.amordahl.CoverageComparer.util.TestUtils;

@RunWith(Parameterized.class)
public class TarantulaTests extends AnswerKeyBasedTester<String, Boolean> {

	public TarantulaTests(Path coverageTaskPath, Path answerKeyPath)
			throws FileNotFoundException, IOException, ParseException {
		super(coverageTaskPath, answerKeyPath);
		// TODO Auto-generated constructor stub
	}

	private static final String INDEX_FILE = "/instlog_tests/tarantula/default_processor/delta_difference_post_processor/paired/index.json";
	
	@Parameters
	public static Collection<Path[]> getParams() {
		return TestUtils.getTestsAndAnswerKeys(INDEX_FILE);
	}

	@Override
	public AbstractCoverageTaskProcessor<String, Boolean> getActp() {
		return new BaselineInstlogProcessor();
	}

	@Override
	public ILocalizer<String, Boolean> getIl() {
		return new TarantulaLocalizer<String, Boolean>();
	}

	@Override
	public String getAnswerKeyKey() {
		return "tarantula";
	}
//
//	@Override
//	public AbstractPostProcessor<String, Boolean> getPostProcessor() {
//		return new DeltaDifferencePostProcessor<String, Boolean>();
//	}
//	
}
