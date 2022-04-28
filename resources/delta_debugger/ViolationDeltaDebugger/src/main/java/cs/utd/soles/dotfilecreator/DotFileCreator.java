package cs.utd.soles.dotfilecreator;

import cs.utd.soles.setup.SetupClass;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class DotFileCreator {
    public static File createDotForProject(SetupClass programInfo){
        programInfo.getPerfTracker().startTimer("jdeps_timer");




        File rootZipDir = turnJarOrApkIntoClassFileDir(programInfo.getAPKFile());
        //need to find way to get only our projects classes we care about, inolves package name and such
        File projectClassesDir=rootZipDir;
        try {
            String[] command = {"jdeps", "-R", "-verbose", "-dotoutput", projectClassesDir.getAbsolutePath() + "/dotfiles", projectClassesDir.getAbsolutePath()};

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
            //TODO:: figure out which thing is our class things.
            return projectClassesDir;
        }catch(Exception e){
            programInfo.getPerfTracker().stopTimer("jdeps_timer");
            e.printStackTrace();
        }
        return null;
    }

    private static File turnJarOrApkIntoClassFileDir(File apkFile) {

        File jarFile = apkFile;
        if(apkFile.getName().contains(".apk")){
            //this is an apk file, first convert it into a jar file
            //command
            String outputFilePath=apkFile.getAbsolutePath().replace(".apk",".jar");
            // ./d2j-dex2jar.sh -f  "path to apk" -o "outputfile.jar"
            jarFile= Paths.get(outputFilePath).toFile();
        }

        jarFile.renameTo(new File(jarFile.getAbsolutePath().replace(".jar", ".zip")));

        String destUnzipFile = jarFile.getAbsolutePath().replace(".zip","");

        try{
            ZipFile src = new ZipFile(jarFile);
            src.extractAll(destUnzipFile);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return Paths.get(destUnzipFile).toFile();
    }
}
