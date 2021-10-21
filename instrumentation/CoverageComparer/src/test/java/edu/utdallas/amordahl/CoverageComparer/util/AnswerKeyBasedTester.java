package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTaskReader;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.AbstractPostProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.AbstractCoverageTaskProcessor;
import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;

/**
 * An abstract class that implements the large majority of the functionality of testing a localizer
 *  based on an index, listing coverage logs in JSON format and an answerkey, also in JSON format.
 *  
 * In addition to the abstract methods, clients must implement their own method to return parameters
 * (i.e., a method annotated with org.junit.runners.Parameterized.Parameters that returns a collection of 
 * path arrays. See the implementation in 
 * edu.utdallas.amordahl.CoverageComparer.localizers.instlog.tarantula.default_processor.no_post_processor.TarantulaTests for an example.
 * 
 * Furthermore, so that this class isn't picked

 * @author Austin Mordahl
 *
 * @param <S> The type of the CoverageRecord that is produced by the coverage task processor.
 */
public abstract class AnswerKeyBasedTester<S extends ICoverageRecord<?, ?>> {

	public CoverageTask getCoverageTask() {
		return coverageTask;
	}

	public JSONObject getAnswerKey() {
		return answerKey;
	}
	
	private CoverageTask coverageTask;
	private JSONObject answerKey;
	public abstract AbstractCoverageTaskProcessor<S> getActp();
	public abstract ILocalizer<S> getIl();
	public abstract AbstractPostProcessor<S> getPostProcessor();

	public void setCoverageTask(CoverageTask coverageTask) {
		this.coverageTask = coverageTask;
	}

	public void setAnswerKey(JSONObject answerKey) {
		this.answerKey = answerKey;
	}

	/**
	 * Constructs a new tester.
	 * @param coverageTaskPath The path to the coverage task.
	 * @param answerKeyPath The path to the answer key for the coverage task.
	 * @param actp The processor to use to process the coverage tasks.
	 * @param localizer The localizer to use to compute localization results.
	 * @param answerKeyKey The key in the answer key to use.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public AnswerKeyBasedTester(Path coverageTaskPath, Path answerKeyPath) throws FileNotFoundException, IOException, ParseException {;
		this.setCoverageTask(CoverageTaskReader.getCoverageTaskFromFile(coverageTaskPath));
		this.coverageTask.setPassed(TestUtils.fixPaths(coverageTask.getPassed()));
		this.coverageTask.setFailed(TestUtils.fixPaths(coverageTask.getFailed()));
		this.coverageTask.setOther(TestUtils.fixPaths(coverageTask.getOther()));
		this.setAnswerKey((JSONObject) new JSONParser().parse(new FileReader(answerKeyPath.toFile())));
	}

	public abstract String getAnswerKeyKey();

	@Test
	public void testValues() {		
		AbstractCoverageTaskProcessor<S> actp = this.getActp();
		AbstractPostProcessor<S> postProcessor = this.getPostProcessor();
		ILocalizer<S> il = this.getIl();

		PassedFailed<S> pf = actp.processCoverageTask(this.getCoverageTask());
		pf = postProcessor.postProcess(pf);
		Map<S, Double> suspiciousnesses = il.computeSuspiciousness(pf);
		Map<String, Double> suspiciousnessWithStrings = suspiciousnesses.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
		Map<String, Double> suspiciousnessKey = this.getSuspiciousnessAnswers();
		Set<Boolean> equals = new HashSet<Boolean>();
		for (String key: suspiciousnessKey.keySet()) {
			if (suspiciousnessWithStrings.containsKey(key)) {
				equals.add(Precision.compareTo(suspiciousnessKey.get(key), suspiciousnessWithStrings.get(key), 0.001d) == 0);
			}
		}
		
		assert(!equals.contains(false));
	}

	public Map<String, Double> getSuspiciousnessAnswers() {
		String answerKeyKey = this.getAnswerKeyKey();
		@SuppressWarnings("unchecked")
		Map<Object, Object> jarr = (Map<Object, Object>)( (JSONObject) this.getAnswerKey().get(answerKeyKey));
		
		// Map jarr to a map of strings to Doubles.
		Map<String, Double> results = jarr.entrySet().stream().collect(Collectors.toMap(
				e -> (String)e.getKey(),
				e -> {if (e.getValue() instanceof Double) return (Double)e.getValue();
						else return ((Long)e.getValue()).doubleValue();}));
		return results;
	}
}
