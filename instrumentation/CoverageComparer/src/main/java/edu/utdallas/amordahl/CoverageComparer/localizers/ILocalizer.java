package edu.utdallas.amordahl.CoverageComparer.localizers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An interface defining the behavior of localizers.
 * @author austin
 *
 * @param <T> A type indicating a test case (e.g., paths or strings).
 * @param <S> The items that are associated with a test case.
 */
public interface ILocalizer<T, S> {
	
	/**
	 * Computes the results from a fault localization scheme.
	 * @param passed The map from passed items to some collection.
	 * @param failed The map from failed items to some collection.
	 * @return A map from items to their suspiciousness.
	 */
	public Map<S, Double> computeSuspiciousness(Map<T, Collection<S>> passed, Map<T, Collection<S>> failed);
}
