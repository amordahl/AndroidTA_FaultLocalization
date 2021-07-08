package edu.utdallas.amordahl.CoverageComparer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
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
		
		
	}

}
