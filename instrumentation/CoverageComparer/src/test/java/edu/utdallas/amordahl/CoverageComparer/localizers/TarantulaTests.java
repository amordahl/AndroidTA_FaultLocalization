package edu.utdallas.amordahl.CoverageComparer.localizers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.BaselineInstlogProcessor;
import edu.utdallas.amordahl.CoverageComparer.util.CoverageTaskTests;
import edu.utdallas.amordahl.CoverageComparer.util.CoveredLine;
import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

@RunWith(Parameterized.class)
public class TarantulaTests extends CoverageTaskTests {

	public TarantulaTests(Path coverageTask, Path answerKey) throws FileNotFoundException, IOException, ParseException {
		super(coverageTask, answerKey);
		// TODO Auto-generated constructor stub
	}
	
	@Parameters
	public static Collection<Path[]> getParams() {
		setup();
		return testsAndAnswerKeys;
	}
	
	public Map<String, Double> getSuspiciousnessAnswers() {
		@SuppressWarnings("unchecked")
		Map<Object, Object> jarr = (Map<Object, Object>)( (JSONObject) this.answerKey.get("tarantula"));
		
		// Map jarr to a map of strings to Doubles.
		Map<String, Double> results = jarr.entrySet().stream().collect(Collectors.toMap(
				e -> (String)e.getKey(),
				e -> {if (e.getValue() instanceof Double) return (Double)e.getValue();
						else return ((Long)e.getValue()).doubleValue();}));
		return results;
		
	}
	@Test
	public void testTarantulaValues() {
		PassedFailed<CoveredLine> pf = new BaselineInstlogProcessor().processCoverageTask(ct);
		Map<CoveredLine, Double> suspiciousnesses = new TarantulaLocalizer<CoveredLine>().computeSuspiciousness(pf);
		Map<String, Double> suspiciousnessWithStrings = suspiciousnesses.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
		Map<String, Double> suspiciousnessKey = this.getSuspiciousnessAnswers();

		// Can't directly compare equality, because of double imprecision.
		//  Thus, we create this data structure to compare in a more appropriate way for doubles. 
		Set<Boolean> equals = new HashSet<Boolean>();
		for (String key: suspiciousnessKey.keySet()) {
			equals.add(Precision.compareTo(suspiciousnessKey.get(key), suspiciousnessWithStrings.get(key), 0.001d) == 0);
		}
		
		assert(!equals.contains(false));
	}	

}
