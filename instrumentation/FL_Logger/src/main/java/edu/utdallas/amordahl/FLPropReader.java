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

	private static FLPropReader me;

	public static FLPropReader getInstance() throws IOException {
		if (me == null)
			me = new FLPropReader();
		return me;
	}

	private Logger logger = LoggerFactory.getLogger(FLPropReader.class);
	private String outputFile;
	private String outputPrefix;
	private Path classFileOutputDir;
	
	private FLPropReader() throws IOException {
		this.setPropValues();
	}

	public Path getClassFileOutputDir() {
		return classFileOutputDir;
	}

	public Path getOutputFile() throws IOException {
		// This is to prevent a thread from getting the output file before everything
		//  has been set up.
		if (this.outputPrefix != null) {
			return Paths.get(this.outputFile.replace("{}", this.outputPrefix));
		}
		return Paths.get(this.outputFile.replace("{}-", ""));
	}

	public String getOutputPrefix() {
		return outputPrefix;
	}

	public void setClassFileOutputDir(Path classFileOutputDir) {
		this.classFileOutputDir = classFileOutputDir;
	}

	public void setOutputFile(String outputFile) {
		logger.info("output file is " + outputFile);
		this.outputFile = outputFile;
	}

	public void setOutputPrefix(String outputPrefix) {
		logger.info("output prefix is " + outputPrefix);
		this.outputPrefix = outputPrefix;
	}

	public void setPropValues() throws IOException {
		String propFileName = "config.properties";

		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
			Properties props = new Properties();
			if (inputStream == null) {
				throw new FileNotFoundException(String.format("Could not find property file %s", propFileName));
			}
			//logger.info("trying to read properties from " + inputStream.toString());
			props.load(inputStream);
			setOutputFile(props.getProperty("output_file"));
			setClassFileOutputDir(Paths.get(props.getProperty("output_dir")));
			//logger.info("outputFile is " + getOutputFile());
			//logger.info("output directory is " + getClassFileOutputDir());
		}
	}
}
