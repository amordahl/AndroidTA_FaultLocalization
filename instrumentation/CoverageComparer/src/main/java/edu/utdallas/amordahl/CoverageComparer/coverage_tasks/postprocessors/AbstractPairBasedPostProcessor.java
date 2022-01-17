//package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors;
//
//import java.nio.file.Path;
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.Map.Entry;
//
//import org.apache.commons.lang3.tuple.Pair;
//
//import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;
//
///**
// * A post processor whose processing is predicated on pairs of tasks (e.g., whether two coverage files
// * were measured on the same APK).
// * 
// * This processor's implementation of {@link #postProcess(PassedFailed)} does not take into account which
// * tasks were passed and failed. Thus, if the transformation is dependent upon the task being passed or failed,
// * it should take that into account by referencing the field pf, which 
// * @author Austin Mordahl
// *
// * @param <S> The same type as the PassedFailed object that this post processor uses.
// */
//public abstract class AbstractPairBasedPostProcessor<S> extends AbstractPostProcessor<S> {
//
//	public AbstractPairBasedPostProcessor(PassedFailed<S> pf) {
//		super(pf);
//	}
//	
//	/**
//	 * Calls {@link #transform(Entry, boolean)} on each entry in this.pf.
//	 */
//	@Override
//	public PassedFailed<S> postProcess() {
//		PassedFailed<S> modifiedPf = new PassedFailed<S>();
//		Collection<Pair<Entry<Path, Collection<S>>, Entry<Path, Collection<S>>>> allPairs = getPairs(this.pf);
//		
//	}
//	
//	public Collection<Pair<Entry<Path, Collection<S>>, Entry<Path, Collection<S>>>> getPairs(PassedFailed<S> pf) {
//		Collection<Entry<Path, Collection<S>>> allEntries = new LinkedList<Entry<Path, Collection<S>>>();
//		
//		allEntries.addAll(pf.getFailed().entrySet());
//		allEntries.addAll(pf.getPassed().entrySet());
//		
//		
//		
//		
//	}
//	
//	public abstract boolean matches(Entry<Path, Collection<S>> entry1, Entry<Path, Collection<S>> entry2);
//	
//	/**
//	 * Given an Entry from a PassedFailed list, and whether or not it was from a passed or failed run,
//	 * this method performs some transformation on that entry.
//	 * @param entry The entry to transform.
//	 * @param isFailed Whether the entry was from a failing run.
//	 * @return The transformed entry.
//	 */
//	public abstract Entry<Path, Collection<S>> transform (Pair<Entry<Path, Collection<S>>, Entry<Path, Collection<S>>> pairedEntries);
//
//}
