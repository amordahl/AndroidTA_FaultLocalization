package edu.utdallas.amordahl.coverage_instrumenter;
import java.lang.instrument.Instrumentation;

import edu.utdallas.amordahl.LoggerHelper;

public class LoggerAgent {
    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("In LoggerAgent.");
        PrimaryTransformer transformer = new PrimaryTransformer();
        instrumentation.addTransformer(transformer);
        LoggerHelper.printCoverageInfo();
    }
}