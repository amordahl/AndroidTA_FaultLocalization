package edu.utdallas.amordahl.coverage_instrumenter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.FLPropReader;
import edu.utdallas.amordahl.LoggerHelper;

public class LoggerAgent {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(LoggerAgent.class);
	
	public static void premain(String args, Instrumentation instrumentation) throws IOException{
        System.out.println("In LoggerAgent.");
        if (args != null && args != "") {
        	try {
				FLPropReader.getInstance().setOutputPrefix(args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(3);
			}
        }
        PrimaryTransformer transformer = new PrimaryTransformer();
        instrumentation.addTransformer(transformer);
        LoggerHelper.printCoverageInfo();
    }
}