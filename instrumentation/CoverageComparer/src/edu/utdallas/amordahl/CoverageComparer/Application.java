package edu.utdallas.amordahl.CoverageComparer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	public static Options setupCommandLineOptions() {
		Options options = new Options();
		options.addOption(Option.builder().argName("c1").hasArg().required().longOpt("coverage_file_1")
				.desc("The first coverage file.").build());
		options.addOption(Option.builder().argName("c2").hasArg().required().longOpt("coverage_file_2")
				.desc("The second coverage file.").build());
		options.addOption(
				Option.builder().argName("o").hasArg().required().longOpt("output").desc("The output file").build());
		return options;
	}

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(setupCommandLineOptions(), args);
		} catch (ParseException e) {
			logger.error("Could not parse command line options. Please try again.");
			System.exit(1);
		}
		
		ArrayList<String> fc1 = readFileContents(cmd.getOptionValue("c1"));
		ArrayList<String> fc2 = readFileContents(cmd.getOptionValue("c2"));
		
		// Compute frequencies
		HashMap<String, Integer> fm1, fm2;
		fm1 = computeFrequencyMap(fc1);
		fm2 = computeFrequencyMap(fc2);
		
		// Compute sets
		HashSet<String> set1, set2;
		set1 = convertListToSet(fc1);
		set2 = convertListToSet(fc2);
		
		// Now, compute the differences.
		JSONObject jo = new JSONObject();
	}
	
	private static ArrayList<String> readFileContents(String fileName) {
		ArrayList<String> fileContent = new ArrayList<String>();
		try (Scanner sc = new Scanner(new File(fileName))) {
			while (sc.hasNextLine()) {
				fileContent.add(sc.next());
			}
		} catch (FileNotFoundException e) {
			logger.error("Could not find file " + fileName);
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(2);
		}
		return fileContent;
	}
	
	private static HashMap<String, Integer> computeFrequencyMap(ArrayList<String> fileContents) {
		HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();
		
		for (String k: fileContents) {
			if (!frequencyMap.containsKey(k)) {
				frequencyMap.put(k, Integer.valueOf(0));
			}
			frequencyMap.put(k, frequencyMap.get(k) + 1);
		}
		
		return frequencyMap;
		
	}
	
	private static HashSet<String> convertListToSet(ArrayList<String> al) {
		HashSet<String> hs = new HashSet<String>();
		for (String k: al) {
			hs.add(k);
		}
		return hs;
	}

}
