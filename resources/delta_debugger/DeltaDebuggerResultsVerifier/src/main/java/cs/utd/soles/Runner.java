package cs.utd.soles;

import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Violation;
import cs.utd.soles.SchemaGenerator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Runner {

    //program takes in debugger directory, eats all the .txt files (which are the results of a run)
    //program also takes in violation_logs directory so we know what to compare stuff to, this is optional

    public static void main(String[] args) throws IOException {

        File debuggerDir = Paths.get(args[0]).toFile();

        String[] extensions = {"txt"};
        List<File> allTXTFiles = ((List<File>) FileUtils.listFiles(debuggerDir, extensions, false));


        File violationLogsDir= null;
        ArrayList<Violation> violationThingList =new ArrayList<>();
        if(args.length>1) {
            violationLogsDir=Paths.get(args[1]).toFile();
            extensions = new String[]{"xml"};
            List<File> violationFiles = ((List<File>) FileUtils.listFiles(violationLogsDir, extensions, false));
            SchemaGenerator.generateSchema();
            for(File x: violationFiles) {
                AQLFlowFileReader thing = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);
                violationThingList.add(thing.getThisViolation(x));

            }
        }


        String output="";
        String header="apk,config1,config2,runtime,type,avgRot,totRot,avgAql,totAql,avgComp,totComp,totProposed,totComplete,numCandidate,%AQL,%Comp,compFailed\n";
        output+=header;
        for(File x: allTXTFiles){
            //get the data, verify the results
            String name = x.getName().replace("_time.txt","");
            //apk config1 config2
            String apkName = name.substring(0, name.indexOf("config"));
            name = name.substring(apkName.length());
            String config1 = name.substring(0, name.lastIndexOf("config"));
            name = name.substring((config1.length()));
            String config2 = name.substring(0);

            Scanner sc = new Scanner(x);
            String in="";
            while(sc.hasNextLine()){
                in+=sc.nextLine()+"\n";
            }
            in = in.replaceAll(":","");
            String[] inArr = in.split("\\W+");
            LineObj j = new LineObj(
                    apkName,config1,config2,
                    inArr[1], inArr[3], inArr[5],
                    inArr[7], inArr[9],inArr[11],
                    inArr[13], inArr[15],inArr[17],
                    inArr[19], inArr[21],inArr[23],
                    inArr[25], inArr[27]);

            String line=j.apk+","+j.config1+","+j.config2+","+j.runtime+","+j.violation_type+","+j.avgRotation+","+j.totalRotation+","+j.avgAQL+","+j.totalAQL+","+j.avgCompile+","+j.totalCompile+","+j.totalProposed+","+j.totalComplete+","+j.numCandidate+","+j.percentAQL+","+j.percentCompile+","+j.compFailed+"\n";


            //if we want to verify the results of this line
            if(violationLogsDir !=null){

                //get the right input
                Violation matchingV = null;
                for(Violation v: violationThingList){
                    if(v.getConfig1().equals(config1.replace("config_flowdroid",""))
                        && v.getConfig2().equals(config2.replace("config_flowdroid",""))
                        && v.getApk().equals(apkName+".apk")){
                        //this violation matches up to this output
                        matchingV=v;
                    }

                }

                //check that our output is expected

                File f = new File(debuggerDir.getAbsolutePath()+"/minimized_apks/"+apkName+config1+config2+".apk");

                //TODO:: just do the run aql process on this apk and check it gets every flow in matchingV.flows()
                //TODO:: append to end of line and also header maybe


            }




            output+=line;
        }
        File outF = new File("deltadebugger_results"+Long.toHexString(System.currentTimeMillis()) +".csv");
        FileWriter fw = new FileWriter(outF);
        fw.write(output);
        fw.flush();
        fw.close();

    }
}
