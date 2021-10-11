package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;

/**
 * This processor reads in data structure contents, and constructs elementwise pairings for localization. In other words, 
 * @author Austin
 *
 */
public class DataStructureElementwiseLogProcessor extends AbstractCoverageTaskProcessor<CoverageRecord<String, String>> {


	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructureelementlog" + ".intermediate");
	}


	@Override
	public String getName() {
		return "DataStructureElementwiseLogProcessor";
	}


	/**
	 * Hijacks {@link #DataStructureContentLogProcessor.processLine(String)}, and then simply creates a single
	 * coverage record for each element in the array.
	 */
	@Override
	public Collection<CoverageRecord<String, String>> processLine(String line) {
		Collection<CoverageRecord<String, String>> result = new ArrayList<CoverageRecord<String, String>>();
		Collection<CoverageRecord<String, ArrayList<String>>> contentLog = new DataStructureContentLogProcessor().processLine(line);
		for (CoverageRecord<String, ArrayList<String>> cr : contentLog) {
			for (String s: cr.getDataStructureContent()) {
				result.add(new CoverageRecord<String, String>(cr.getLocation(), s));
			}
		}
		return result;
	}

}
