package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

public class DeltaDifferencePostProcessor<S> extends AbstractPostProcessor<S> {

	private boolean onlyTransformFailed;

	/**
	 * Constructs a new DeltaDifferencePostProcessor instance.
	 * @param onlyTransformFailed If true, only failed test cases are transformed. Otherwise, all
	 *  cases which are determined to match are applied.
	 */
	public DeltaDifferencePostProcessor(boolean onlyTransformFailed) {
		super();
		this.onlyTransformFailed = onlyTransformFailed;
	}

	@Override
	protected Entry<Path, Collection<S>> transform(Entry<Path, Collection<S>> entry, PassedFailed<S> pf) {
		if (this.onlyTransformFailed && !pf.getFailed().containsKey(entry.getKey())) {
			return entry;
		}
		else {
			return transformHelper(entry, getPartner(entry, pf), pf);
		}
	}
	
	/**
	 * Given an entry, searches through pf in order to find its partner (e.g., one on the same apk).
	 * Will only return the first partner it finds.
	 * @param entry The entry 
	 * @param pf The full PassedFailed set.
	 * @return The partner.
	 */
	private Entry<Path, Collection<S>> getPartner(Entry<Path, Collection<S>> entry, PassedFailed<S> pf) {
		// TODO Auto-generated method stub
		return null;
	}

	private Entry<Path, Collection<S>> transformHelper(Entry<Path, Collection<S>> transformee,
			Entry<Path, Collection<S>> partner, PassedFailed<S> pf) {
		Collection<S> difference = new LinkedList<S>(transformee.getValue());
		// Compute delta
		difference.removeAll(partner.getValue());
		return new SimpleEntry<Path, Collection<S>>(transformee.getKey(), difference);
		
	}
}
