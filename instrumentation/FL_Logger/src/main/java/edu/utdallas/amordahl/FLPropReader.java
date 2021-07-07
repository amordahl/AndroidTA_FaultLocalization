package edu.utdallas.amordahl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Credit to the tutorial at https://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/
// "Java Properties File: How to Read config.properties Values in Java?" by App Shah on February 8, 2020.
// Accessed on 7/7/2021

public class FLPropReader {
	
	public FLPropReader () throws IOException {
		this.setPropValues();
	}
	private Logger logger = LoggerFactory.getLogger(FLPropReader.class);
	private Path outputFile;
	
	public void setPropValues() throws IOException {
		String propFileName = "config.properties";

		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)){
			Properties props = new Properties();
			if (inputStream == null) {
				throw new FileNotFoundException(String.format("Could not find property file %s", propFileName));
			}
			props.load(inputStream);
			setOutputFile(Paths.get(props.getProperty("output_file")));
			logger.info("outputFile is " + getOutputFile());
		}
	}

	public Path getOutputFile() throws IOException {
		return this.outputFile;
	}

	public void setOutputFile(Path outputFile) {
		this.outputFile = outputFile;
	}
}
