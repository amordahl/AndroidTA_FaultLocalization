package edu.utdallas.amordahl.coverage_instrumenter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.FLPropReader;

public class PropReader {
	public PropReader() throws IOException {
		this.setPropValues();
	}
	private Logger logger = LoggerFactory.getLogger(PropReader.class);
	private Path outputDir;
	
	public void setPropValues() throws IOException {
		String propFileName = "config.properties";

		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)){
			Properties props = new Properties();
			if (inputStream == null) {
				throw new FileNotFoundException(String.format("Could not find property file %s", propFileName));
			}
			props.load(inputStream);
			setOutputFile(Paths.get(props.getProperty("output_dir")));
			logger.debug("outputDirectory is " + getOutputFile());
		}
	}

	public Path getOutputFile() throws IOException {
		return this.outputDir;
	}

	public void setOutputFile(Path outputFile) {
		this.outputDir = outputFile;
	}
}
