package edu.utdallas.amordahl.coverage_instrumenter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import edu.utdallas.amordahl.FLPropReader;
import edu.utdallas.amordahl.LoggerHelper;

public class LoggerAgent {
    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("In LoggerAgent.");
        if (args != null && args != "") {
        	try {
				FLPropReader.getInstance().setOutputPrefix(args);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        PrimaryTransformer transformer = new PrimaryTransformer();
        instrumentation.addTransformer(transformer);
        LoggerHelper.printCoverageInfo();
    }
}