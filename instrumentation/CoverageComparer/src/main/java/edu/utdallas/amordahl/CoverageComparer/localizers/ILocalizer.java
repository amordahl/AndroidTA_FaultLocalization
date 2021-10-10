package edu.utdallas.amordahl.CoverageComparer.localizers;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

/**
 * An interface defining the behavior of localizers.
 * @author austin
 *
 * @param <T> A type indicating a test case (e.g., paths or strings).
 * @param <S> The items that are associated with a test case.
 */
public interface ILocalizer<S> {
	
	/**
	 * Computes the results from a fault localization scheme.
	 * @param pf A PassedFailed object.
	 * @return A map from items to their suspiciousness.
	 */	
	public Map<S, Double> computeSuspiciousness(PassedFailed<S> pf);

	public String getName();

}
