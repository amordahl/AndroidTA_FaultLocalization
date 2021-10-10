package edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import edu.utdallas.amordahl.CoverageComparer.util.CoveredDataStructure;

public class DataStructureContentLogProcessor extends AbstractCoverageTaskProcessor<CoveredDataStructure> {

	@Override
	public String getName() {
		return "DataStructureContentLogProcessor";
	}

	@Override
	protected Collection<CoveredDataStructure> readInstFile(Path p) {
		Collection<CoveredDataStructure> fileContent = new ArrayList<>();
		try (Scanner sc  = new Scanner(p.toFile())) {
			while (sc.hasNext()) {
				// Remove any non-printable characters.
				String line = sc.next().replaceAll("\\P{Print}", "");
				String location = line.split(",")[0];
				String content = line.substring(line.indexOf(","));
				if (!content.startsWith("[") || !content.endsWith("]")) {
					throw new RuntimeException(String.format("Malformed content string %s", content));
				}
				List<String> contentAsList = Arrays.asList(content.substring(1, content.length() - 1).split(","));
				ArrayList<String> contentAsArrayList = new ArrayList<String>(contentAsList);
				CoveredDataStructure cds = new CoveredDataStructure(location, contentAsArrayList);
				fileContent.add(cds);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileContent;
	}

	@Override
	protected Path getIntermediateName(Path p) {
		return p.resolveSibling("." + p.getFileName() + ".datastructurelog" + ".intermediate");
	}

}
