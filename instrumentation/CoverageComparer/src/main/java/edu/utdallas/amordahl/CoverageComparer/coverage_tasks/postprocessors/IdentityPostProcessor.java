package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map.Entry;

import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

/**
 * Does no post-processing, simply returns the passed set.
 * @author Austin Mordahl
 *
 */
public class IdentityPostProcessor<S> extends AbstractPostProcessor<S> {

	@Override
	public PassedFailed<S> postProcess(PassedFailed<S> pf) {
		return pf;
	}

	@Override
	protected Entry<Path, Collection<S>> transform(Entry<Path, Collection<S>> entry,
			PassedFailed<S> pf) {
		// TODO Auto-generated method stub
		return null;
	}
}
