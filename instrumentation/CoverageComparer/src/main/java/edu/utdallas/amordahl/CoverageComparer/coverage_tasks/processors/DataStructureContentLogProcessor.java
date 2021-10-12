package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.CoverageComparer.util.CoverageRecord;

public class DataStructureContentLogProcessor extends AbstractCoverageTaskProcessor<CoverageRecord<String, ArrayList<String>>> {

	private static Logger logger = LoggerFactory.getLogger(DataStructureContentLogProcessor.class);
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
		String content = line.substring(line.indexOf(",")+1);
		if (!content.startsWith("[") || !content.endsWith("]")) {
			logger.warn("Could not parse {} from line {} into an array -- treating as one object.", content, line);
			result.add(new CoverageRecord<String, ArrayList<String>>(location, 
					new ArrayList<String>(Collections.singletonList(content))));
		} else if (content.replace(" ", "").equals("[]")) {
			logger.warn("Skipping line {} because content is empty.", line);
		} else {
		List<String> contentAsList = 
				Arrays.asList(content.substring(1, content.length() - 1).split(","))
				.stream().map(s -> s.trim()).collect(Collectors.toList());
		logger.debug("Content on line {} is {}", line, contentAsList);
		ArrayList<String> contentAsArrayList = new ArrayList<String>(contentAsList);
		result.add(new CoverageRecord<String, ArrayList<String>>(location, contentAsArrayList));
		}
		return result;
	}

}
