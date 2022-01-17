package edu.utdallas.amordahl.multiphaseinstrumenter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.FLPropReader;
import edu.utdallas.amordahl.LoggerHelper;

/**
 * The entrypoint of the instrumentation. This class is responsible
 * for reading and setting up properties of the analysis.
 * 
 * @author Austin Mordahl
 *
 */
public class LoggerAgent {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LoggerAgent.class);
	
	public static void premain(String args, Instrumentation instrumentation) throws IOException{
        if (args != null && args != "") {
	    logger.debug("Parsing arguments {}", args);
        	SettingsManager.parseOpts(args);
        }
	logger.debug("No arguments read.");
        PrimaryTransformer transformer = new PrimaryTransformer();
        instrumentation.addTransformer(transformer);
    }
}
