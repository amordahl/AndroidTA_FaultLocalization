package cs.utd.soles;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flow;
import org.javatuples.Pair;


import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;


/**
 * This is used to test the AST against the target, it also parses the target file so it can be compared
 * */



public class TesterUtil implements ThreadHandler{

    boolean soundness;
    String targetFile=null;
    String xmlSchemaFile=null;
    int candidateCountJava=0;
    int compilationFailedCount=0;
    final Object lockObj;
    boolean isViolation=false;

    ArrayList<Flow> config1Flows;
    ArrayList<Flow> config2Flows;

    public TesterUtil(String targetFile, String xmlSchemaFile, boolean violationType, final Object lockObj){
        this.targetFile=targetFile;
        this.xmlSchemaFile=xmlSchemaFile;
        this.soundness=violationType;
        config1Flows=FlowJSONHandler.turnTargetPathIntoFlowList(targetFile);
        this.lockObj = lockObj;
    }

    public TesterUtil(ArrayList<Flow> config1List,ArrayList<Flow> config2List, String xmlSchemaFile, boolean violationType, final Object lockObj, boolean isViolation){
        this.config1Flows=config1List;
        this.config2Flows=config2List;
        this.xmlSchemaFile=xmlSchemaFile;
        this.soundness=violationType;
        this.lockObj=lockObj;
        this.isViolation=isViolation;
        System.out.println("THIS VIOLATION IS TYPE: " + this.soundness);
    }


    //this saves the compilation units to the correct files
    public void saveCompilationUnits(ArrayList<Pair<File,CompilationUnit>> compilationUnits, int positionChanged, CompilationUnit changedUnit) throws IOException {
        int i=0;
        for(Pair<File,CompilationUnit> x: compilationUnits){

            FileWriter fw = new FileWriter(x.getValue0());
            if(Runner.LOG_MESSAGES) {

                if(i==positionChanged)
                    System.out.println(changedUnit.toString());
                //else
                    //System.out.println("CompilationUnit: " + list.get(i).toString());
            }

            if(i==positionChanged){
                fw.write(changedUnit.toString());

                //this writes the unit we changed to the intermedateJavaDir and gives it a number telling the order
                if(Runner.LOG_MESSAGES) {
                    File intermediateFile = new File(Runner.intermediateJavaDir + "/" + candidateCountJava + x.getValue0().getName());
                    if (intermediateFile.exists())
                        intermediateFile.delete();
                    intermediateFile.createNewFile();
                    candidateCountJava++;
                    FileWriter writer = new FileWriter(intermediateFile);
                    writer.write(changedUnit.toString());
                    writer.flush();
                    writer.close();

                }

            }else {
                fw.write(x.getValue1().toString());
            }
            fw.flush();
            fw.close();
            i++;
        }
    }

    //save compilation units from list
    public void saveCompilationUnits(ArrayList<Pair<File,CompilationUnit>> saveThese) throws IOException {

        cleanseFiles();
        for (Pair<File,CompilationUnit> p : saveThese) {
            File x = p.getValue0();
            x.createNewFile();
            CompilationUnit changedUnit = p.getValue1();
            FileWriter fw = new FileWriter(x);
            fw.write(changedUnit.toString());
            fw.flush();
            fw.close();

        }

    }

    //create apk from list
    public void startApkCreation(String projectGradlewPath, String projectRootPath, ArrayList<Pair<File, CompilationUnit>> bestCUList) {

        //this method is for removing entire files, so we got to actually remove them
        cleanseFiles();
        createApk(projectGradlewPath,projectRootPath,bestCUList,bestCUList.size()+1,null);
    }

    public void cleanseFiles(){
        for(Pair<File,CompilationUnit> p : Runner.originalCUnits){
            File path = p.getValue0();
            if(path.exists())
                path.delete();
        }
    }




    //this just calls gradlew assembleDebug in the right directory
    //this needs the gradlew file path and the root directory of the project
    public void createApk(String gradlewFilePath, String rootDir, ArrayList<Pair<File,CompilationUnit>> list, int positionChanged, CompilationUnit changedUnit){
        Runner.performanceLog.startOneCompileRun();
        String[] command = {gradlewFilePath, "clean", "assembleDebug", "-p", rootDir};
        try {
            saveCompilationUnits(list,positionChanged, changedUnit);
            Process p = Runtime.getRuntime().exec(command);

            ProcessThread pThread = new ProcessThread(p,this,ProcessType.CREATE_APK_PROCESS, 300000);
            pThread.start();
            /*if(!out.contains("BUILD SUCCESSFUL") || oute.contains("BUILD: FAILURE")){
                //assembling project failed we don't care why
                if(Runner.LOG_MESSAGES)
                    System.out.println(out);
                compilationFailedCount++;
                Runner.performanceLog.endOneFailedCompileRun();
                return false;
            }*/
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void runAQL(String apk, String generatingConfig1, String generatingConfig2, String programConfigString) throws IOException {
        Runner.performanceLog.startOneAQLRun();

        //this bit runs and captures the output of the aql script
        String command1 = "python runaql.py "+generatingConfig1+" "+apk+" -f";
        String command2 = "python runaql.py "+generatingConfig2+" "+apk+" -f";


        boolean runaql1=true;
        boolean runaql2=true;
        //if its a violation always run both
        if(isViolation){
            runaql1=true;
            runaql2=true;
        }else{
            if(config1Flows.size()==0)
                runaql1=false;
            if(config2Flows.size()==0)
                runaql2=false;
        }

        //start these commands and then handle them somewhere else
        Process command1Run = null;
        Process command2Run = null;
        if(runaql1){
            command1Run=Runtime.getRuntime().exec(command1);
        }
        if(runaql2){
            command2Run= Runtime.getRuntime().exec(command2);
        }

        AQLThread aqlThread = new AQLThread(command1Run, command2Run,this);
        aqlThread.start();
        //File output1 = handleOutput("1",Long.toHexString(System.currentTimeMillis()), command1Out,programConfigString);
        //File output2 = handleOutput("2",Long.toHexString(System.currentTimeMillis()), command2Out,programConfigString);
        //Runner.performanceLog.endOneAQLRun();
        //return handleAQL(output1, output2);

    }


    private File handleOutput(String ID, String time, String outString, String programConfigString) throws IOException {

        String fp = "debugger/tempfiles/aqlfiles/"+programConfigString+time+"out"+ID+".xml";

        File f = Paths.get(fp).toFile();
        f.mkdirs();
        if(f.exists())
            f.delete();
        f.createNewFile();

        String xmlString ="";
        if(outString.contains("<answer/>")){
            xmlString ="<answer>\n</answer>";
        }else if(outString.contains("<answer>")){
            xmlString = outString.substring(outString.indexOf("<answer>"), outString.indexOf("</answer>") + 9);
        }else{
            return null;
        }
        String header = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n";
        if(Runner.LOG_MESSAGES){
            System.out.println("this run used this file:" +f.getName());
        }


        FileWriter fw = new FileWriter(f);
        fw.write(header);
        fw.write(xmlString);
        fw.flush();
        fw.close();

        return f;
    }

    private boolean handleAQL(File o1, File o2){


        if(o1==null||o2==null) {
            System.out.println("Aborted cause one of files was null");
            return false;
        }
        ArrayList<Flow> flowList = new ArrayList<>();

        boolean returnVal=false;
        //depending on if it is a precision or soundness error

        //for this new format. The first config is always the one that is more precise/sound. So, to check a violation
        //we get the results for the second aql, and subtract it from the results of the first.


        /*for(Flow x: flowList){


            //basically we need to compare this flowList to our targetFlowList

            if(x.equals(targetFlow)){
                if(Runner.LOG_MESSAGES) {
                    System.out.println("Flow Source: " + x.getSource().getStatement() + "  Flow Sink: " + x.getSink().getStatement());
                    System.out.println("Target Flow Source: " + targetFlow.getSource().getStatement() + "  Flow Sink: " + targetFlow.getSink().getStatement());
                }
                returnVal=true;
            }

        }*/


        //add all the flows we found
        if(isViolation){
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

    public ArrayList<Flow> getFlowStrings(File xmlFile){
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

    /*private String catchOutput(Process p) throws IOException {





        //this just reads the output of the command we just ran
        BufferedReader  input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader  error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String output="";
        String s;
        while((s=input.readLine())!=null){

            output+=s+"\n";
        }

        output+="\n\nError Messages from AQL:\n";
        while((s=error.readLine())!=null){

            output+=s+"\n";
        }
        input.close();
        error.close();
        p.destroy();
        //System.out.println("Output of AQL: "+output);
        return output;
    }*/

    public void startApkCreation(String projectGradlewPath, String projectRootPath, ArrayList<Pair<File, CompilationUnit>> bestCUList, int compPosition, CompilationUnit copiedu) {
        createApk(projectGradlewPath,projectRootPath,bestCUList,compPosition,copiedu);
    }

    public void startAQLProcess(String projectAPKPath, String config1, String config2, String thisRunName) {
        try {
            runAQL(projectAPKPath,config1,config2,thisRunName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runCCGCreator(String projectApkPath, String thisRunName){
        String[] command = {"java","-jar","/home/dakota/documents/AndroidTA_FaultLocalization/resources/modified_flowdroid/FlowDroid/soot-infoflow-cmd/target/soot-infoflow-cmd-jar-with-dependencies.jar"
            ,"-a",projectApkPath,"-p","/home/dakota/documents/Android/platforms/","-s","/home/dakota/documents/AndroidTA_FaultLocalization/resources/modified_flowdroid/FlowDroid/soot-infoflow-android/SourcesAndSinks.txt"};
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
            ProcessThread pThread = new ProcessThread(p,this,ProcessType.CALLGRAPH, 30000);
            pThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCCGProcess(String projectAPKPath, String thisRunName){
        runCCGCreator(projectAPKPath,thisRunName);
    }

    boolean threadResult=false;
    @Override
    public void handleThread(ProcessType t, String finalString, String finalString2) {

        //This new framework for handling threads should allow us to read Process output more elegantly

        switch(t){
            //create_apk_process is just one gradlew assembleDebug
            case CREATE_APK_PROCESS:
                synchronized(lockObj) {
                    threadResult=false;
                    //build failed
                    if (!finalString.contains("BUILD SUCCESSFUL") || finalString.contains("BUILD: FAILURE")) {
                        //assembling project failed we don't care why
                        if (Runner.LOG_MESSAGES)
                            System.out.println(finalString);
                        compilationFailedCount++;
                        Runner.performanceLog.endOneFailedCompileRun();
                        threadResult=false;
                        lockObj.notify();
                        return;
                    }
                    Runner.performanceLog.endOneCompileRun();
                    //build worked
                    threadResult=true;
                    lockObj.notify();
                }
                break;
            //aql_process is two ProcessThreads, so handle accordingly
            case AQL_RUN:
                synchronized(lockObj){
                    threadResult=false;
                    Runner.performanceLog.endOneAQLRun();
                    //final results of aql are in both finalString1 and finalString2 respectively
                    //order is config1, config2

                    try {
                        File o1 = handleOutput("1",Long.toHexString(System.currentTimeMillis()), finalString,Runner.thisRunName);
                        File o2 = handleOutput("2",Long.toHexString(System.currentTimeMillis()), finalString2,Runner.thisRunName);
                        threadResult=handleAQL(o1,o2);
                    } catch (IOException e) {

                        e.printStackTrace();
                        lockObj.notify();
                    }

                    lockObj.notify();
                }
                break;
            case CALLGRAPH:
                synchronized (lockObj){
                    threadResult=handleCallGraph(finalString);
                    lockObj.notify();
                }
                break;
        }

    }

    //dependency graph is like, static so this can just access it and change its data up accordingly
    public boolean handleCallGraph(String contents){
        //Okay so we have the exact string we want we just need to match it up to what we already have,
        //ClassNode and DependencyGraph
        String[] lines = contents.split("\n+");
        for(String x: lines){
            //much like DependencyGraph.parseGraphFromDot()
            String[] leftRight = x.split(" -> ");




            //one problem, there might be "Ghost methods"
            //methods that flowdroid created but aren't actually real, so before we add any particular line to DependencyGraph.methodGraph we need to make sure it is a real thing
            //so basically, we gonna have to do some magic
            //furthermore, i am ignoring anonymous class methods for this, they are too annoying to implement for now
            //also we want only internal methods, both in and out, so resolve both before making an edge

            //Okay lets find out the class for left side first, helps us get the right ast and verify this method exists
            String origin = leftRight[0];
            String[] originContents = convertNodeString(origin);
            String dependency = leftRight[1];
            String[] dependencyContents = convertNodeString(dependency);

            findASTNodeFromSignature(originContents);


        }

        return true;
    }

    public String[] convertNodeString(String nodeString){
        //'<' .* '>': <returnType> <methodName>(<>* parameterTypes)
        ArrayList<String> stringList = new ArrayList<>();
        //System.out.println("start nodeString: "+nodeString);
        //we are done with package/classname when we hit a :
        /*String packageClassName = nodeString.substring(1,nodeString.indexOf(": "));
        nodeString = nodeString.substring(0,packageClassName.length()+2);
        System.out.println("Changed nodeString: "+nodeString);*/

        //cut first and last
        nodeString = nodeString.substring(1,nodeString.length()-1).replace(": "," ");
        String[] elements = nodeString.split(" ");
        String classPackageName = elements[0];
        //System.out.println("ClassPackageName: "+classPackageName);
        String returnType = elements[1];
        //System.out.println("return type: "+returnType);
        String methodName = elements[2].substring(0,elements[2].indexOf("("));
        //System.out.println("method name: "+methodName);
        String[] parameterTypeStrings = elements[2].substring(elements[2].indexOf("(")+1, elements[2].lastIndexOf(")")).split(" ");
        //System.out.println("parameter types: "+Arrays.toString(parameterTypeStrings));
        String[] returnList = new String[3+parameterTypeStrings.length];
        returnList[0]=classPackageName;
        returnList[1]=returnType;
        returnList[2]=methodName;
        for(int i=3;i<returnList.length;i++){
            returnList[i]=parameterTypeStrings[i-3];
        }

        return returnList;
    }

    //if this returns null, then the method aint to be found in one our asts
    public Node findASTNodeFromSignature(String[] methodSig){
        //method sig is,
        //0 class/package name
        //1 return type
        //2 method name
        //N parameter Types

        ClassNode parent = Runner.dg.getClassNodeForFilePath(Runner.getFilePathForClass(methodSig[0]));
        if(null!=parent){
            System.out.println("it finds a class: " + Arrays.toString(methodSig) + " IS IN "+parent.getName());
        }

        return null;
    }
}
