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
public class IdentityPostProcessor extends AbstractPostProcessor<Object> {

	@Override
	public PassedFailed<Object> postProcess(PassedFailed<Object> pf) {
		return pf;
	}

	@Override
	protected Entry<Path, Collection<Object>> transform(Entry<Path, Collection<Object>> entry,
			PassedFailed<Object> pf) {
		// TODO Auto-generated method stub
		return null;
	}
}
