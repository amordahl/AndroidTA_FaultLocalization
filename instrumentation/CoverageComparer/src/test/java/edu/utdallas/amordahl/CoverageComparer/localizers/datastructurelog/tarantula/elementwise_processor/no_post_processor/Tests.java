//package edu.utdallas.amordahl.CoverageComparer.localizers.datastructurelog.tarantula.elementwise_processor.no_post_processor;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.Collection;
//
//import org.json.simple.parser.ParseException;
//import org.junit.runner.RunWith;
//import org.junit.runners.Parameterized;
//import org.junit.runners.Parameterized.Parameters;
//
//import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.AbstractPostProcessor;
//import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.IdentityPostProcessor;
//import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.AbstractCoverageTaskProcessor;
//import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.DataStructureElementwiseLogProcessor;
//import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;
//import edu.utdallas.amordahl.CoverageComparer.localizers.TarantulaLocalizer;
//import edu.utdallas.amordahl.CoverageComparer.util.AnswerKeyBasedTester;
//import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageLocation;
//import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageRecord;
//import edu.utdallas.amordahl.CoverageComparer.util.TestUtils;
//
//@RunWith(Parameterized.class)
//public class Tests extends AnswerKeyBasedTester<DataStructureCoverageLocation, Object> {
//
//	public Tests(Path coverageTaskPath, Path answerKeyPath) throws FileNotFoundException, IOException, ParseException {
//		super(coverageTaskPath, answerKeyPath);
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	public AbstractCoverageTaskProcessor<DataStructureCoverageLocation, Object> getActp() {
//		return new DataStructureElementwiseLogProcessor();
//	}
//
//	@Override
//	public ILocalizer<DataStructureCoverageLocation, Object> getIl() {
//		return new TarantulaLocalizer<DataStructureCoverageLocation, Object>();
//	}
//
//	@Override
//	public String getAnswerKeyKey() {
//		return "tarantula";
//	}
//	
//	private static final String INDEX_FILE = "/datastructure_tests/tarantula/elementwise_processor/no_post_processor/index.json";
//	
//	@Parameters
//	public static Collection<Path[]> getParams() {
//		return TestUtils.getTestsAndAnswerKeys(INDEX_FILE);
//	}
//
//	@Override
//	public AbstractPostProcessor<DataStructureCoverageRecord> getPostProcessor() {
//		return new IdentityPostProcessor<DataStructureCoverageRecord>();
//	}
//
//}
