package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

public interface ICoverageTaskProcessor<T, S> {
	
	public PassedFailed<T, S> processCoverageTask(CoverageTask ct);
}
