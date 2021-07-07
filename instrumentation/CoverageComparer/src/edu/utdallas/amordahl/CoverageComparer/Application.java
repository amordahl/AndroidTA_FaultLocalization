package edu.utdallas.amordahl.CoverageComparer;

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
		options.addOption("c1", "coverage1", true,
				"The first coverage file.");
		options.addOption("c2", "coverage2", true,
				"The second coverage file.");
		options.addOption("o", "output", true,
				"Where the output should be written.");
		return options;
	}
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(setupCommandLineOptions(), args);
		} catch (ParseException e) {
			logger.error("Could not parse command line options. Please try again.");
			System.exit(1);
		}
	}

}
