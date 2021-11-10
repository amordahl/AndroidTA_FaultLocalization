//package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors;
//
//import java.nio.file.Path;
//import java.util.Collection;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.stream.Collectors;
//
//import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;
//
///**
// * Post processors accept a PassedFailed set, and process it in some way.
// * @author Austin Mordahl
// *
// */
//public abstract class AbstractPostProcessor<S, T> {
//
//	public PassedFailed<S, T> postProcess(PassedFailed<S, T> pf) {
//		PassedFailed<S, T> modifiedPf = new PassedFailed<S, T>();
//		modifiedPf.setFailed(transformMap(pf.getFailed(), pf));
//		modifiedPf.setPassed(transformMap(pf.getPassed(), pf));
//		return modifiedPf;
//	}
//	
//	/**
//	 * Applies {@link #transform(Entry, PassedFailed)} to each entry in the map.
//	 * @param map The map to transform.
//	 * @param pf The PassedFailed set that includes the map (included in case {@link #transform(Entry, PassedFailed)} needs contextual information.
//	 * @return The transformed map.
//	 */
//	protected Map<Path, Collection<S>> transformMap(Map<Path, Collection<S>> map, PassedFailed<S> pf) {
//		return map.entrySet().stream().map(e -> transform(e, pf))
//				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
//	}
//	
//	/**
//	 * Transforms an Entry.
//	 * @param entry The entry to transform.
//	 * @param pf The full PassedFailed set.
//	 * @return The transformed entry. Should return entry if no transformation takes place.
//	 */
//	protected abstract Entry<Path, Collection<S>> transform(Entry<Path, Collection<S>> entry, PassedFailed<S> pf);
//		
//}
