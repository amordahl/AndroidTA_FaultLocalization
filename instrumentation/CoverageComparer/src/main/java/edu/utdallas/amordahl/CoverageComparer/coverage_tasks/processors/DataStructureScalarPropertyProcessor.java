package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import edu.utdallas.amordahl.CoverageComparer.util.DataStructureScalarPropertyRecord;

public class DataStructureScalarPropertyProcessor extends AbstractCoverageTaskProcessor<DataStructureScalarPropertyRecord> {

	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructurescalarlog" + ".intermediate");
	}

	@Override
	public String getName() {
		return "DataStructureScalarPropertyProcessor";
	}

	@Override
	public Collection<DataStructureScalarPropertyRecord> processLine(String line) {
		Collection<DataStructureScalarPropertyRecord> cd = new ArrayList<DataStructureScalarPropertyRecord>();
		if (!line.startsWith("DATASTRUCTURE:")) {
			return cd;
		}
		String[] tokens = line.split(",");
		DataStructureScalarPropertyRecord dscr = new DataStructureScalarPropertyRecord(tokens[0], tokens[1], Integer.valueOf(tokens[2]));
		cd.add(dscr);
		return cd;
	}

}
