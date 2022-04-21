package cs.utd.soles.buildphase;

import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.CommandThread;

public class BuildScriptRunner {

    public static boolean runBuildScript(SetupClass c){

        //hopefully this test script just prints out true/false
        String command = "./"+c.getBuildScriptFile();

        CommandThread testThread = new CommandThread(command);
        testThread.start();

        return testThread.returnOutput().equals("true");

    }
}
