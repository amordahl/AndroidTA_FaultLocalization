package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageLocation;
import edu.utdallas.amordahl.CoverageComparer.util.DataStructureScalarPropertyRecord;

public class DataStructureScalarPropertyProcessor extends AbstractCoverageTaskProcessor<DataStructureCoverageLocation, Integer> {

	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructurescalarlog" + ".intermediate");
	}

	@Override
	public String getName() {
		return "DataStructureScalarPropertyProcessor";
	}

	@Override
	public Map<DataStructureCoverageLocation, Integer> processLine(String line) {
		Map<DataStructureCoverageLocation, Integer> cd = new HashMap<DataStructureCoverageLocation, Integer>();
		if (!line.startsWith("DATASTRUCTURE:")) {
			return cd;
		}
		String[] tokens = line.split(",");
		cd.put(new DataStructureCoverageLocation(tokens[0], tokens[1]), Integer.valueOf(tokens[2]));
		return cd;
	}

	@Override
	protected boolean allowParallelLineProcessing() {
		// TODO Auto-generated method stub
		return true;
	}

}
