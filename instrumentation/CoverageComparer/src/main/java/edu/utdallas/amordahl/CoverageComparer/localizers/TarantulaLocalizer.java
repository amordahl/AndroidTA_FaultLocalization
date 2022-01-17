package edu.utdallas.amordahl.CoverageComparer.localizers;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

/**
 * A localizer that computes the Tarantula localization rankings.
 * @author Austin Mordahl
 * @param <K>
 * @param <V>
 * @param <T> 
 */
public class TarantulaLocalizer<K, V> implements ILocalizer<K, V> {

	@SuppressWarnings("unchecked")
	@Override
	public Map<Pair<K, V>, Double> computeSuspiciousness(PassedFailed<K, V> pf) {
		Map<Pair<K, V>, Double> suspiciousnesses = Collections.synchronizedMap(new HashMap<Pair<K, V>, Double>());
		
//		// Add all S'es into the universe set.
//		for (Map<Path, Collection<?>> m : new Map[] {pf.getPassed(), pf.getFailed()} ) {
//			for (Entry<Path, Collection<?>> e: m.entrySet()) {
//				e.getValue().forEach(s -> universe.add((S) s));
//			}
//		}
		
		// For each s, compute the suspiciousness
		pf.getUniverse().parallelStream().forEach(u -> {
			int numPassed = countOccurrences(u, pf, pf.getPassed());
			int numFailed = countOccurrences(u, pf, pf.getFailed());
			int totFailed = pf.getFailed().size();
			int totPassed = pf.getPassed().size();
			suspiciousnesses.put(u, computeSuspiciousnessValue(numPassed, numFailed, totFailed, totPassed));
		});
		
		return suspiciousnesses;
		
	}
	// The tarantula formula is:
	// S(t) = (faulty(t) / totFaulty) / ( (faulty(t) / totFaulty) + (successful(t) / totSuccessful)
	/**
	 * Compute the actual suspiciousness value of something given the following information:
	 * @param numPassed The number of passed cases the element occurred in.
	 * @param numFailed The number of failed cases the element occurred in
	 * @param totFailed The total number of failed test cases.
	 * @param totPassed The total number of passed test cases.
	 * @return The computed Tarantula suspiciousness value.
	 */
	private double computeSuspiciousnessValue(int numPassed, int numFailed, int totFailed, int totPassed) {
		return ((double)numFailed / totFailed) / ( ((double)numFailed / totFailed) + ((double)numPassed / totPassed));
	}
	
	/**
	 * Count the number of times element occurs in the collection values of the map.
	 * @param element The element we are counting.
	 * @param map A map of items to collections.
	 * @return The number of values in the map that contained element.
	 */
	private Integer countOccurrences(Pair<K, V> element, PassedFailed<K, V> pf, Collection<Path> files) {
		int count = 0;
		for (Path p: files) {
			Set<V> results = pf.getValueOfInPath(element.getKey(), p);
			if (results.contains(element.getRight())) {
				count += 1;
			}
		}
		return count;
	}

	public String getName() { return "TARANTULA"; }
}
