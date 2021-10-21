package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageRecord;

/**
 * This processor reads in data structure contents, and constructs elementwise pairings for localization. In other words, 
 * @author Austin
 *
 */
public class DataStructureElementwiseLogProcessor extends AbstractCoverageTaskProcessor<DataStructureCoverageRecord> {


	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DataStructureElementwiseLogProcessor.class);
	
	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructureelementlog" + ".intermediate");
	}


	public DataStructureElementwiseLogProcessor() { super(); }
	public DataStructureElementwiseLogProcessor(boolean readIntermediates) {
		super(readIntermediates);
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
	public Collection<DataStructureCoverageRecord> processLine(String line) {
		Collection<DataStructureCoverageRecord> result = new ArrayList<DataStructureCoverageRecord>();
		Collection<DataStructureCoverageRecord> contentLog = new DataStructureContentLogProcessor().processLine(line);
		for (DataStructureCoverageRecord cr : contentLog) {
			if (cr.getClass().isInstance(Collection.class)) {
		
		}
		return result;
	}
		return result;
	}
}
