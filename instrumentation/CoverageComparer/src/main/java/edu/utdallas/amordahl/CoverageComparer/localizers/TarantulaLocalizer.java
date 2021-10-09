package edu.utdallas.amordahl.CoverageComparer.localizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A localizer that computes the Tarantula localization rankings.
 * @author Austin Mordahl
 *
 * @param <T> 
 */
public class TarantulaLocalizer<T, S> implements ILocalizer<T, S> {

	@SuppressWarnings("unchecked")
	@Override
	public Map<S, Double> computeSuspiciousness(Map<T, Collection<S>> passed, Map<T, Collection<S>> failed) {
		Map<S, Double> suspiciousnesses = new HashMap<S, Double>();
		Set<S> universe = new HashSet<S>();
		
		// Add all S'es into the universe set.
		for (Map<T, Collection<S>> m : new Map[] {passed, failed} ) {
			for (Entry<T, Collection<S>> e: m.entrySet()) {
				e.getValue().forEach(s -> universe.add(s));
			}
		}
		
		// For each s, compute the suspiciousness
		for (S u: universe) {
			int numPassed = countOccurrences(u, passed);
			int numFailed = countOccurrences(u, failed);
			int totFailed = failed.keySet().size();
			int totPassed = passed.keySet().size();
			suspiciousnesses.put(u, computeSuspiciousnessValue(numPassed, numFailed, totFailed, totPassed));
		}
		
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
	private Integer countOccurrences(S element, Map<T, Collection<S>> map) {
		int count = 0;
		for (Collection<S> s: map.values()) {
			if (s.contains(element)) {
				count += 1;
			}
		}
		return count;
	}

	public String getName() { return "TARANTULA"; }
}
