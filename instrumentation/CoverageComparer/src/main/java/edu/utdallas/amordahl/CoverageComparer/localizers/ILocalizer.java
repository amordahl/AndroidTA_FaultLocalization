package edu.utdallas.amordahl.CoverageComparer.localizers;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

/**
 * An interface defining the behavior of localizers.
 * @author austin
 *
 * @param <S> The items that are associated with a test case.
 */
public interface ILocalizer<K, V> {
	
	/**
	 * Computes the results from a fault localization scheme.
	 * @param pf A PassedFailed object.
	 * @return A map from items to their suspiciousness.
	 */	
	public Map<Pair<K, V>, Double> computeSuspiciousness(PassedFailed<K, V> pf);

	public String getName();

}
