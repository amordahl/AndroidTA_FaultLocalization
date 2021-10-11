package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;

public class DataStructureContentLogProcessor extends AbstractCoverageTaskProcessor<CoverageRecord<String, ArrayList<String>>> {

	@Override
	public String getName() {
		return "DataStructureContentLogProcessor";
	}


	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructurecontentlog" + ".intermediate");
	}

	@Override
	public Collection<CoverageRecord<String, ArrayList<String>>> processLine(String line) {
		Collection<CoverageRecord<String, ArrayList<String>>> result = new ArrayList<>();
		String location = line.split(",")[0];
		String content = line.substring(line.indexOf(","));
		if (!content.startsWith("[") || !content.endsWith("]")) {
			throw new RuntimeException(String.format("Malformed content string %s", content));
		}
		List<String> contentAsList = Arrays.asList(content.substring(1, content.length() - 1).split(","));
		ArrayList<String> contentAsArrayList = new ArrayList<String>(contentAsList);
		result.add(new CoverageRecord<String, ArrayList<String>>(location, contentAsArrayList));
		return result;
	}

}
