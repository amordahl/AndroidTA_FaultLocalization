package edu.utdallas.amordahl.CoverageComparer.localizers.datastructurelog.tarantula.elementwise_processor.no_post_processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.json.simple.parser.ParseException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.AbstractCoverageTaskProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.DataStructureElementwiseLogProcessor;
import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;
import edu.utdallas.amordahl.CoverageComparer.localizers.TarantulaLocalizer;
import edu.utdallas.amordahl.CoverageComparer.util.AnswerKeyBasedTester;
import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;
import edu.utdallas.amordahl.CoverageComparer.util.TestUtils;

@RunWith(Parameterized.class)
public class Tests extends AnswerKeyBasedTester<CoverageRecord<String, String>> {

	public Tests(Path coverageTaskPath, Path answerKeyPath) throws FileNotFoundException, IOException, ParseException {
		super(coverageTaskPath, answerKeyPath);
		// TODO Auto-generated constructor stub
	}

	@Override
	public AbstractCoverageTaskProcessor<CoverageRecord<String, String>> getActp() {
		return new DataStructureElementwiseLogProcessor();
	}

	@Override
	public ILocalizer<CoverageRecord<String, String>> getIl() {
		return new TarantulaLocalizer<CoverageRecord<String, String>>();
	}

	@Override
	public String getAnswerKeyKey() {
		return "tarantula";
	}
	
	private static final String INDEX_FILE = "/datastructure_tests/tarantula/elementwise_processor/no_post_processor/index.json";
	
	@Parameters
	public static Collection<Path[]> getParams() {
		return TestUtils.getTestsAndAnswerKeys(INDEX_FILE);
	}

}
