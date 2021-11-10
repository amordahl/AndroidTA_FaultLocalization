//package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;
//
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
//import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageLocation;
//import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageRecord;
//import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;
//
///**
// * This processor reads in data structure contents, and constructs elementwise pairings for localization. In other words, 
// * @author Austin
// *
// */
//public class DataStructureElementwiseLogProcessor extends AbstractCoverageTaskProcessor<DataStructureCoverageLocation, Object> {
//
//
//	@SuppressWarnings("unused")
//	private static Logger logger = LoggerFactory.getLogger(DataStructureElementwiseLogProcessor.class);
//	
//	@Override
//	protected Path getIntermediateName(Path p) {
//		return p.resolveSibling("." + p.getFileName() + ".datastructureelementlog" + ".intermediate");
//	}
//
//
//	public DataStructureElementwiseLogProcessor() { super(); }
//	public DataStructureElementwiseLogProcessor(boolean readIntermediates) {
//		super(readIntermediates);
//	}
//	
//	@Override
//	public String getName() {
//		return "DataStructureElementwiseLogProcessor";
//	}
//
//
//	/**
//	 * Hijacks {@link #DataStructureContentLogProcessor.processLine(String)}, and then simply creates a single
//	 * coverage record for each element in the array.
//	 */
//	@Override
//	public Map<DataStructureCoverageLocation, Object> processLine(String line) {
//		Map<DataStructureCoverageLocation, Object> result = new HashMap<DataStructureCoverageLocation, Object>();
//		Map<DataStructureCoverageLocation, Object> contentLog = new DataStructureContentLogProcessor().processLine(line);
//		for (Entry<DataStructureCoverageLocation, Object> cr : contentLog.entrySet()) {
//			try {
//				for (Object t: (Collection)cr.getValue()) {
//					result.put(new DataStructureCoverageRecord(cr.getLocation(), cr.getType(), t));
//				}
//			} else {
//				result.add(cr);
//		}
//		return result;
//	}
//		return result;
//	}
//
//	/**
//	 * Given a coverage task, reads in all of the content in its passed and failed sets.
//	 * @param ct A CoverageTask.
//	 * @return The PassedFailed object, representing the content of the CoverageTask.
//	 */
//	public PassedFailed<DataStructureCoverageLocation, Object> processCoverageTask(CoverageTask ct) {
//		logger.trace("Entered processCoverageTask.");
//		
//		PassedFailed<DataStructureCoverageLocation, Object> pf = new PassedFailed<DataStructureCoverageLocation, Object>();
//		pf.setPassed(ct.getPassed());
//		pf.setFailed(ct.getFailed());
//		if (ct.getOther() != null) {
//			pf.setOther(ct.getOther());
//		}
//		pf.setOriginatingTask(ct);
//		pf.getAllFiles().stream().forEach(p -> {
//			Map<DataStructureCoverageLocation, Object> x = readInstFileOrGetIntermediate(p);
//			for (Entry<DataStructureCoverageLocation, Object> e : x.entrySet()) {
//				
//			}
//			try {
//				Collection<Object> objAsCollection = 
//			}
//			pf.setAllValuesForPath(p, readInstFileOrGetIntermediate(p)));
//		}
//		return pf;
//	}
//	
//	@Override
//	protected boolean allowParallelLineProcessing() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//}
