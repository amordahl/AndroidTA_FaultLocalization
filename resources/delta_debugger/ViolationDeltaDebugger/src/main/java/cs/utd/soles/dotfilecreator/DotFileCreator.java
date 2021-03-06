package cs.utd.soles.dotfilecreator;

import cs.utd.soles.setup.SetupClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class DotFileCreator {
    public static File createDotForProject(SetupClass programInfo){
        programInfo.getPerfTracker().startTimer("jdeps_timer");
        try {
            String[] command = {"jdeps", "-R", "-verbose", "-dotoutput", programInfo.getTargetProject().getProjectClassFiles() + "/dotfiles", programInfo.getTargetProject().getProjectClassFiles()};

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            Process p = pb.start();
            String result = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (p.isAlive())
                while (in.ready()) {
                    result += in.readLine() + "\n";
                }
            p.waitFor();
            programInfo.getPerfTracker().stopTimer("jdeps_timer");
            return programInfo.getTargetProject().getDotFile();
        }catch(Exception e){
            programInfo.getPerfTracker().stopTimer("jdeps_timer");
            e.printStackTrace();
        }
        return null;
    }
}
