package cs.utd.soles;

import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flow;
import com.utdallas.cs.alps.flows.Flowset;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class Runner {

    private static String config1;
    private static String apkName;
    private static String config2;
    private static boolean targetType;
    private static boolean violationOrNot;
    static ArrayList<Flow> config1Flows;
    static ArrayList<Flow> config2Flows;
    private static String programConfigString;

    public static void main(String[] args) throws IOException, InterruptedException {


        SchemaGenerator.generateSchema();

        //args is just a flowset
        handleArgs(args);

        if(!violationOrNot){
            System.exit(0);
        }

        File p = new File("verifier/results/");
        p.mkdirs();
        p = new File("verifier/results/"+programConfigString+"_results.txt");
        p.createNewFile();
        FileWriter fw = new FileWriter(p);
        String s = "" + runAQL(apkName,config1,config2,programConfigString);
        fw.write(s);
        fw.flush();
        fw.close();

    }

    private static void handleArgs(String[] args) {
        AQLFlowFileReader reader = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);

        //everything we need is in this here object
        Flowset thisViolation = reader.getFlowSet(Paths.get(args[0]).toFile());

        apkName="/home/dakota/documents/droidbench30_apks/"+thisViolation.getApk();
        config1="/home/dakota/documents/AndroidTAEnvironment/configurations/FlowDroid/1-way/config_FlowDroid_"+thisViolation.getConfig1()+".xml";
        config2 = "/home/dakota/documents/AndroidTAEnvironment/configurations/FlowDroid/1-way/config_FlowDroid_" + thisViolation.getConfig2() + ".xml";
        targetType=thisViolation.getType().equalsIgnoreCase("soundness");
        violationOrNot=thisViolation.getViolation().toLowerCase().equals("true");;
        config1Flows = thisViolation.getConfig1_FlowList();
        config2Flows = thisViolation.getConfig2_FlowList();
        programConfigString = thisViolation.getApk()+"_"+thisViolation.getConfig1()+"_"+thisViolation.getConfig2()+"_"+targetType+"_"+violationOrNot;
        //the files with no flows we still need the apk info from so that we can save its apk, so figure out the apk from the filename
        //fix apkName
        if(apkName.equals("/")){

            String fileName = Paths.get(args[0]).toFile().getName();

            String[] split = fileName.split("_");

            //TODO:: THIS WONT WORK FOR DROIDBENCH PLEASE FIX
            apkName="/"+split[3]+"_"+split[4];
            System.out.println("this apk name: "+apkName);
            //this project shouldnt be minimized - nothing to minimize to.
        }
    }
    public static boolean runAQL(String apk, String generatingConfig1, String generatingConfig2, String programConfigString) throws IOException, InterruptedException {

        //this bit runs and captures the output of the aql script
        String[] command1 ={"python","runaql.py",generatingConfig1,apk,"-f"};
        String[] command2 ={"python","runaql.py",generatingConfig2,apk,"-f"};


        ProcessBuilder pb1 = new ProcessBuilder(command1);
        ProcessBuilder pb2 = new ProcessBuilder(command2);

        Process command1P = pb1.start();

        String command1Out = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(command1P.getInputStream()));
        while (command1P.isAlive()) {
            while (in.ready()) {
                command1Out += in.readLine() + "\n";
            }
        }
        command1P.waitFor();

        Process command2P = pb2.start();


        String command2Out = "";
        in = new BufferedReader(new InputStreamReader(command2P.getInputStream()));
        while (command2P.isAlive()) {
            while (in.ready()) {
                command2Out += in.readLine() + "\n";
            }
        }
        command2P.waitFor();



        File output1 = handleOutput("1",Long.toHexString(System.currentTimeMillis()), command1Out,programConfigString);
        File output2 = handleOutput("2",Long.toHexString(System.currentTimeMillis()), command2Out,programConfigString);

        return handleAQL(output1, output2);

    }
    private static File handleOutput(String ID, String time, String outString, String programConfigString) throws IOException {

        String fp = "verifier/tempfiles/aqlfiles/"+programConfigString+time+"out"+ID+".xml";

        File f = Paths.get(fp).toFile();
        f.mkdirs();
        if(f.exists())
            f.delete();
        f.createNewFile();
        System.out.println(outString);
        String xmlString ="";
        if(outString.contains("<answer/>")){
            xmlString ="<answer>\n</answer>";
        }else if(outString.contains("<answer>")){
            xmlString = outString.substring(outString.indexOf("<answer>"), outString.indexOf("</answer>") + 9);
        }else{
            return null;
        }
        String header = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n";

        FileWriter fw = new FileWriter(f);
        fw.write(header);
        fw.write(xmlString);
        fw.flush();
        fw.close();

        return f;
    }
    private static boolean handleAQL(File o1, File o2){


        if(o1==null||o2==null) {
            System.out.println("Aborted cause one of files was null");
            return false;
        }
        ArrayList<Flow> flowList = new ArrayList<>();

        boolean returnVal=false;


        //add all the flows we found
        if(violationOrNot){
            flowList.addAll(getFlowStrings(o2));
            flowList.removeAll(getFlowStrings(o1));
        }else {
            flowList.addAll(getFlowStrings(o2));
            flowList.addAll(getFlowStrings(o1));
        }

        //check and see if we maintain all the flows we want to
        ArrayList<Flow> checkList = new ArrayList<>();
        checkList.addAll(config1Flows);
        checkList.addAll(config2Flows);
        checkList.removeAll(flowList);

        //System.out.println("Flows not found");
        /*for(Flow x: checkList){
            System.out.println(x.getSink().getStatementFull() + " " + x.getSink().getMethod());
            System.out.println(x.getSource().getStatementFull() + " " + x.getSource().getMethod());

        }
        System.out.println("Flows checked for");
        for(Flow x: flowList){
            System.out.println(x.getSink().getStatementFull() + " " + x.getSink().getMethod());
            System.out.println(x.getSource().getStatementFull() + " " + x.getSource().getMethod());

        }*/

        //only return true if we managed a change that preserves every flow we wanted to
        if(checkList.size()==0){
            returnVal=true;
        }

        //in the case of soundness, the first list has a flow the second does not (so we recreate the violation if we remove all the common flows AND the targetflow is still in the list)
        //same for precision except we remove from the second
        return returnVal;
    }
    public static ArrayList<Flow> getFlowStrings(File xmlFile){
        AQLFlowFileReader aff = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);
        Iterator<Flow> flowIt = aff.getFlows(xmlFile);
        ArrayList<Flow> out = new ArrayList<Flow>();

        //maybe deduplicate here to keep consistent with Austin
        System.out.println("FLOWS FOR THIS XML FILE: " + xmlFile.getName());
        while(flowIt.hasNext()){
            Flow x = flowIt.next();
            out.add(x);
            System.out.println(x.getSink().getStatementFull() + " " + x.getSink().getMethod());
            System.out.println(x.getSource().getStatementFull() + " " + x.getSource().getMethod());
        }
        return out;
    }
}

