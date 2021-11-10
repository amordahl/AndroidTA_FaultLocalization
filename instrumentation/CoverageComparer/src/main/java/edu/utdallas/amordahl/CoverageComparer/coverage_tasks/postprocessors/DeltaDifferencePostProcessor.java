//package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors;
//
//import java.nio.file.Path;
//import java.util.Collection;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.stream.Collectors;
//import java.util.AbstractMap.SimpleEntry;
//
//import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;
//
//public class DeltaDifferencePostProcessor<S> extends AbstractPostProcessor<S> {
//
//	private boolean onlyTransformFailed;
//
//	/**
//	 * Constructs a new DeltaDifferencePostProcessor instance.
//	 * @param onlyTransformFailed If true, only failed test cases are transformed. Otherwise, all
//	 *  cases which are determined to match are applied.
//	 */
//	public DeltaDifferencePostProcessor(boolean onlyTransformFailed) {
//		super();
//		this.onlyTransformFailed = onlyTransformFailed;
//	}
//	
//	public DeltaDifferencePostProcessor() {
//		super();
//		this.onlyTransformFailed = false;
//	}
//
//	@Override
//	protected Entry<Path, Collection<S>> transform(Entry<Path, Collection<S>> entry, PassedFailed<S> pf) {
//		if (this.onlyTransformFailed && !pf.getFailed().containsKey(entry.getKey())) {
//			return entry;
//		}
//		else {
//			// Find the partner.
//			Collection<S> partnerContent = getPartner(entry.getKey(), pf);
//			Collection<S> transformed = entry.getValue().stream().filter(e -> !partnerContent.contains(e)).collect(Collectors.toList());
//			return new SimpleEntry<Path, Collection<S>>(entry.getKey(), transformed);
//		}
//	}
//	
//	/**
//	 * Given an entry, searches through pf in order to find its partner (e.g., one on the same apk).
//	 * Will only return the first partner it finds.
//	 * @param entry The entry 
//	 * @param pf The full PassedFailed set.
//	 * @return The partner.
//	 */
//	@SuppressWarnings("unchecked")
//	private Collection<S> getPartner(Path key, PassedFailed<S> pf) {
//		Map<Path, Path> pairs = pf.getOriginatingTask().getPairs();
//		if (pairs == null) {
//			throw new RuntimeException(String.format("Cannot compute differences because the "
//					+ "coverage task file %s did not indicate pairs.", pf.getOriginatingTask().getOriginalPath()));
//		} else {
//			return (Collection<S>) PassedFailed.findValueForKey(pf, pairs.get(key));
//		}
//	}
//}
