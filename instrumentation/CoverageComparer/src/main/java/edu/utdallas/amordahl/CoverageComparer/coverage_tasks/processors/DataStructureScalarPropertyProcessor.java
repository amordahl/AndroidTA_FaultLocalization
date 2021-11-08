package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import edu.utdallas.amordahl.CoverageComparer.util.DataStructureCoverageRecord;

public class DataStructureScalarPropertyProcessor extends AbstractCoverageTaskProcessor<DataStructureCoverageRecord> {

	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructurenulllog" + ".intermediate");
	}

	@Override
	public String getName() {
		return "DataStructureNullProcessor";
	}

	@Override
	public Collection<DataStructureCoverageRecord> processLine(String line) {
		Collection<DataStructureCoverageRecord> cd = new ArrayList<DataStructureCoverageRecord>();
		String[] tokens = line.split(",");
		DataStructureCoverageRecord dscr = new DataStructureCoverageRecord(tokens[0], tokens[1], tokens[2]);
		cd.add(dscr);
		return cd;
	}

}
