package cs.utd.soles.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReadProcess {

    public static String readProcess(String[] commands) throws IOException {

        ProcessBuilder b = new ProcessBuilder(commands);
        b.redirectErrorStream(true);
        Process p = b.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String output = "";
        String line = null;

        while((line=r.readLine())!=null){
            output+=line+"\n";
        }
        return output;
    }

    public static String readProcess(String commands) throws IOException {

        ProcessBuilder b = new ProcessBuilder(commands);
        b.redirectErrorStream(true);
        Process p = b.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String output = "";
        String line = null;

        while((line=r.readLine())!=null){
            output+=line+"\n";
        }
        return output;
    }

}
