package cs.utd.soles;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flowset;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Runner {

    static File intermediateJavaDir=null;
    public static boolean LOG_MESSAGES=false;
    static TesterUtil testerForThis=null;
    static PerfTimer performanceLog = new PerfTimer();
    private static boolean projectNeedsToBeMinimized=true;
    private static String projectClassFiles;
    public static String THIS_RUN_PREFIX="";
    public static DependencyGraph dg = null;
    private static long TIMEOUT_TIME_MINUTES=120;
    //1 minute is this long in millis
    private static final long M_TO_MILLIS=60000;
    private static long SYSTEM_TIMEOUT_TIME=0;


    public static void main(String[] args){
        performanceLog.startProgramRunTime();

        //generate schema file
        //maybe have the program wait until it finds the Schema??
        try {
            SchemaGenerator.generateSchema();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //handle the arguments
        handleArgs(args);

        //after we handl the args we gotta do a couple of things
        /*
        * 1. build the project file to minimize
        * 2. make the tester
        * 3. setup intermediate trash
        * 4. try to minimize
        *
        * */

        //build the project file
        createTargetProject();



        //HANDLE THE SOURCE DIRECTORY (THIS SHOULD JUST BE JAVA DIR IN PROJECT)
        try{
            handleSrcDirectory(projectSrcPath);
            if(LOG_MESSAGES) {
                String filePathName = "debugger/java_files/" + thisRunName + "/intermediate_java/";
                File f = new File(filePathName);
                f.mkdirs();
                intermediateJavaDir=f;
            }
            //get the lines count before any changes
            testerForThis.saveCompilationUnits(bestCUList);
            performanceLog.startLineCount=LineCounter.countLinesDir(projectSrcPath);
            performanceLog.lastCurrentLines=performanceLog.startLineCount;
        }catch(IOException e){
            e.printStackTrace();
        }
        //handle the projects that dont need to be minimized
        if(!projectNeedsToBeMinimized){
            //this should just run the gradlew assembleDebug and check if we reproduced the thing - since checklist should be 0 then yes we always get a apk
            checkChanges(bestCUList.size()+1,null);
            System.out.println("Saving APK, no flows given so no minimization to be done. Exiting program...");
            System.exit(0);
        }


        //associate classes to files
        fillNamesToPaths();

        //generate dot file
        //dependency graph
        try {
            //create the apk so we actually have something to work with.
            synchronized(lockObject) {
                testerForThis.startApkCreation(projectGradlewPath, projectRootPath, bestCUList);
                lockObject.wait();
                if(!testerForThis.threadResult){
                    System.out.println("BUILD FAILED, we didnt change anything so faulty project");
                    System.exit(-1);
                }
                saveBestAPK();

                //then make the nodes
                dg = makeDependencyNodes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        //Before we start delta debugging lets, work on finding the best files to deal with
        if(DO_CLASS_REDUCTION) {
            ArrayList<HashSet<ClassNode>> closures = dg.getTransitiveClosuresDifferent();
            System.out.println("CLOSURES: " + closures);
            try {

                reduceFromClosures(closures);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*
        * Method based reduction goes here, right after class based reduction. First, run our modified Flowdroid its in
        * /home/dakota/documents/AndroidTA_FaultLocalization/resources/modified_flowdroid/FlowDroid/soot-infoflow-cmd/target/soot-infoflow-cmd-jar-with-dependencies.jar
        * //TODO:: make this thing an argument, but hard coded works fine. Anyway,
        * */
        if(DO_METHOD_REDUCTION) {
            try {
                //create newest version of apk
                synchronized (lockObject) {
                    testerForThis.startApkCreation(projectGradlewPath, projectRootPath, bestCUList);
                    lockObject.wait();
                    if (!testerForThis.threadResult) {
                        System.out.println("BUILD FAILED, we didnt change anything so faulty project");
                        System.exit(-1);
                    }
                    saveBestAPK();

                    testerForThis.startCCGProcess(projectAPKPath, thisRunName);
                    lockObject.wait();
                    //our callgraph has now been created so I guess we just should call makeClosures,
                    //and then pass them to a method based reducer


                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }







        //before we start debugging, sort the pairs based on whos the most dependant

        Comparator<Pair<File,CompilationUnit>> cuListComp = new Comparator<Pair<File, CompilationUnit>>() {
            @Override
            public int compare(Pair<File, CompilationUnit> o1, Pair<File, CompilationUnit> o2) {

                ClassNode x1 = matchPair(o1);
                ClassNode x2 = matchPair(o2);

                if(x1 ==null)
                    return 1;
                if(x2==null)
                    return -1;
                if(matchPair(o1).getClosureSize()<matchPair(o2).getClosureSize()){
                    return -1;
                }
                else if(matchPair(o1).getClosureSize()>matchPair(o2).getClosureSize()){
                    return 1;
                }
                return 0;
            }
        };

        /**/

        Collections.sort(bestCUList,cuListComp);
        SYSTEM_TIMEOUT_TIME=System.currentTimeMillis()+(TIMEOUT_TIME_MINUTES*M_TO_MILLIS);

        //start the delta debugging process
        while(!minimized&&DO_HDD_REDUCTION&&System.currentTimeMillis()<SYSTEM_TIMEOUT_TIME){

            performanceLog.startOneRotation();
            //this is set here because if a change is made to ANY ast we want to say we haven't minimized yet
            minimized=true;
            int i=0;

            for (Pair<File,CompilationUnit> compilationUnit : bestCUList) {
                //if we are under the time limit, traverse the tree
                if(System.currentTimeMillis()<SYSTEM_TIMEOUT_TIME)
                    traverseTree(i, compilationUnit.getValue1());
                i++;
            }
            System.out.println("Done with 1 rotation");
            performanceLog.endOneRotation();
        }

        //log a bunch of information
        try {
            String filePathName = "debugger/java_files/" +thisRunName+"/";
            for (int i = 0; i < bestCUList.size(); i++) {
                File file = new File(filePathName +bestCUList.get(i).getValue0().getName() + ".java");
                file.mkdirs();
                if (file.exists())
                    file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                fw.write(bestCUList.get(i).toString());
                fw.flush();
                fw.close();
            }

            //get the lines count after all changes
            performanceLog.endLineCount=LineCounter.countLinesDir(projectSrcPath);

            filePathName = "debugger/"+thisRunName+"_time.txt";
            File file = new File(filePathName);
            file.mkdirs();
            if (file.exists())
                file.delete();
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            long finalRunTimeVar= performanceLog.getProgramRunTime()/1000;
            fw.write("program_runtime: "+finalRunTimeVar+"\n"+"\n");
            fw.write("violation_type: "+targetType+"\n");
            fw.write("violation_or_not: "+violationOrNot+"\n");
            fw.write("average_of_rotations: " + performanceLog.getAverageOfRotations()/1000+"\n");
            fw.write("total_rotations: "+ performanceLog.getTotalRotations()+"\n"+"\n");

            fw.write("average_of_good_runtime_aql: " + performanceLog.getAverageOfGoodAQLRuns()/1000+"\n");
            fw.write("total_good_aql_runs: "+performanceLog.getTotalAQLRuns()+"\n"+"\n");
            fw.write("average_of_good_runtime_compile: " +performanceLog.getAverageOfGoodCompileRuns()/1000+"\n");
            fw.write("total_good_compile_runs: "+ performanceLog.getTotalCompileRuns()+"\n"+"\n");

            fw.write("average_of_bad_runtime_aql: " + performanceLog.getAverageOfGoodAQLRuns()/1000+"\n");
            fw.write("total_bad_aql_runs: "+performanceLog.getTotalAQLRuns()+"\n"+"\n");
            fw.write("average_of_bad_runtime_compile: " +performanceLog.getAverageOfGoodCompileRuns()/1000+"\n");
            fw.write("total_bad_compile_runs: "+ performanceLog.getTotalCompileRuns()+"\n"+"\n");
            fw.write("\n"+performanceLog.getPercentages());
            fw.write("\nnum_candidate_ast: " + testerForThis.candidateCountJava);
            fw.write("\nStart_line_count: "+performanceLog.startLineCount);
            fw.write("\nEnd_line_count: "+performanceLog.endLineCount);
            fw.write("\n%Of_Lines_Removed: "+ ((1.0 - (performanceLog.endLineCount/((double)performanceLog.startLineCount)))*100));
            fw.write(performanceLog.writeCodeChanges());
            fw.flush();
            fw.close();


            //let the final version of the project_file be the minimized version so we dont have to replace java file manually
            //replace this trash
            testerForThis.saveCompilationUnits(bestCUList);
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    private static DependencyGraph makeDependencyNodes() throws IOException, InterruptedException {
        //grab our class files
        String[] command = {"jdeps","-R","-verbose","-dotoutput",projectClassFiles+"/dotfiles",projectClassFiles};

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

        DependencyGraph rg = new DependencyGraph();
        rg.parseGraphFromDot(dotFile);
        return rg;
    }

    public static void reduceFromClosures(List<HashSet<ClassNode>> closures) throws IOException, InterruptedException {
        HashSet<ClassNode> knownNodes = new HashSet<>();

        List<HashSet<ClassNode>> unknownNodes = new ArrayList<>(closures);

        Comparator<HashSet<ClassNode>> sorting = (o1, o2) -> {

            HashSet<ClassNode> u1 = new HashSet<>(o1);
            u1.addAll(knownNodes);
            HashSet<ClassNode> u2 = new HashSet<>(o2);
            u2.addAll(knownNodes);
            if(u1.size()<u2.size()){
                return -1;
            }
            else if(u1.size()>u2.size()){
                return 1;
            }
            return 0;
        };

        unknownNodes.sort(sorting);
        //System.out.println(unknownNodes);
        //ill have to ask Austin what the point of running this multiple times is, seems like the first closure we find is our answer?
        //Doesn't make sense that we would require multiple closures?
        int r= unknownNodes.size();
        int i=0;
        while(r>0&&i<=r){

            HashSet<ClassNode> proposal = new HashSet<>(knownNodes);
            if(proposal.size()==0&&i==0){
                i++;
            }
            int j=0;
            for(;j<i;j++){
                proposal.addAll(unknownNodes.get(j));
            }
            //match the proposal to the compilation units

            ArrayList<Pair<File,CompilationUnit>> newProgramConfig = matchProposal(proposal);
            //if this works then update namedBestCUS to be good else
            if(checkProposal(newProgramConfig)){
                //if this works then add to list of known nodes and re-sort
                r=j-1;
                //resort
                knownNodes.addAll(unknownNodes.get(j-1));
                System.out.println("Known nodes: ");
                for(ClassNode x: knownNodes){
                    System.out.print(x.getName()+ " ");
                }
                System.out.println();
                List<HashSet<ClassNode>> newList = new ArrayList<>();
                for(int k=0;k<r;k++){
                    newList.add(unknownNodes.get(k));
                }
                newList.sort(sorting);
                unknownNodes=newList;
                bestCUList=newProgramConfig;
                //restart our search
                i=0;
            }
            //revert, just write all the things from bestcus
            else{
                testerForThis.saveCompilationUnits(bestCUList);
            }
            i++;

        }

    }
    
    private static ArrayList<Pair<File,CompilationUnit>> matchProposal(HashSet<ClassNode> proposal){
        ArrayList<Pair<File,CompilationUnit>> matchedProposal = new ArrayList<>();

        for(ClassNode x: proposal){
            String filePath = x.getFilePath();
            for(Pair pir: bestCUList){
                if(((File)pir.getValue0()).getAbsolutePath().equals(filePath)){
                    matchedProposal.add(pir);
                    break;

                }
            }
            //System.out.println(filePath);
        }
        return matchedProposal;
    }

    private static ClassNode matchPair(Pair<File,CompilationUnit> pair){
        for(ClassNode x: dg.graph){
            if(pair.getValue0().getAbsolutePath().equals(x.getFilePath())){
                return x;
            }
        }
        return null;
    }
    public static CompilationUnit getASTForFile(String filePath){
            for(Pair<File,CompilationUnit> p:bestCUList){
                if(p.getValue0().getAbsolutePath().equals(filePath))
                    return p.getValue1();

            }
            return null;
    }

    private static boolean checkProposal(ArrayList<Pair<File,CompilationUnit>> proposal) throws IOException, InterruptedException {

        boolean returnVal=false;
        performanceLog.addChangeNum();
        try {
            //enter synchronized
            synchronized(lockObject) {
                //create the apk using the new list of compilation units
                testerForThis.startApkCreation(projectGradlewPath, projectRootPath, proposal);
                    /*if (testerForThis.createApk(projectGradlewPath, projectRootPath, bestCUList, javaFiles, compPosition, copiedu)) {

                        if (testerForThis.runAQL(projectAPKPath, config1, config2, thisRunName)) {

                            returnVal = true;
                            minimized = false;
                            //this is the best apk yet, save it.
                            saveBestAPK();
                            System.out.println("Successful One\n\n------------------------------------\n\n\n");
                            //for (CompilationUnit x : bestCUList) {
                            //    System.out.println(x);
                            //}
                            System.out.println("CopiedUnit:" + copiedu);
                        }
                    }*/
                //wait for createApk to be done
                lockObject.wait();
                //get the results
                //if this is true, then the apk created succesfully
                if(!testerForThis.threadResult){
                    return false;
                }
                //start aql process
                testerForThis.startAQLProcess(projectAPKPath, config1, config2, thisRunName);
                //testerUtil.startAQLProcess
                //wait for aqlprocess to be done
                lockObject.wait();
                //get the results
                if(!testerForThis.threadResult){
                    return false;
                }

                //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!
                try {
                    long currentLineCount = LineCounter.countLinesDir(projectSrcPath);
                    performanceLog.addCodeChange(currentLineCount);
                }catch(IOException e){
                    e.printStackTrace();
                }
                returnVal = true;
                minimized = false;
                //this is the best apk yet, save it.
                saveBestAPK();
                System.out.println("Successful One\n\n------------------------------------\n\n\n");
                //for (CompilationUnit x : bestCUList) {
                //    System.out.println(x);
                //}
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return returnVal;
    }

    //INPUT FOR THE DEBUGGER SHOULD BE APK NAME (droidbench), CONFIG 1, CONFIG 2, TRUE OR FALSE (type of violation), TRUE OR FALSE (violation or nonviolation), target_file??
    static String apkName;
    static String config1;
    static String config2;
    static boolean targetType;
    static boolean violationOrNot;
    static File dotFile;

    static final Object lockObject = new Object();

    //args should be one filepath that is the <violation xml file>
    private static void handleArgs(String[] args) {
        AQLFlowFileReader reader = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);


        //everything we need is in this here object
        Flowset thisViolation = reader.getFlowSet(Paths.get(args[0]).toFile());

        apkName="/"+thisViolation.getApk();
        config1="/home/dakota/documents/AndroidTAEnvironment/configurations/FlowDroid/1-way/config_FlowDroid_"+thisViolation.getConfig1()+".xml";
        config2="/home/dakota/documents/AndroidTAEnvironment/configurations/FlowDroid/1-way/config_FlowDroid_"+thisViolation.getConfig2()+".xml";
        targetType=thisViolation.getType().equalsIgnoreCase("soundness");
        violationOrNot=thisViolation.getViolation().toLowerCase().equals("true");;
        //the files with no flows we still need the apk info from so that we can save its apk, so figure out the apk from the filename
        //fix apkName
        if(apkName.equals("/")){

            String fileName = Paths.get(args[0]).toFile().getName();
            //split
            //flowset, violation-false(true), apk, split
            String[] split = fileName.split("_");
            //0, 1, 2,
            String concatApk=split[2];
            for(int i=3;i<split.length-1;i++){
                concatApk+="_"+split[i];
            }
            apkName="/"+concatApk;
            //this project shouldnt be minimized - nothing to minimize to.
            projectNeedsToBeMinimized=false;
        }

        //add stuff to the tester
        testerForThis = new TesterUtil(thisViolation.getConfig1_FlowList(), thisViolation.getConfig2_FlowList(), SchemaGenerator.SCHEMA_PATH,targetType, lockObject,violationOrNot);

        for(int i=1;i<args.length;i++) {

            if (args[i].equals("-l")) {
                LOG_MESSAGES = true;
            }
            if(args[i].equals("-c")){
                DO_CLASS_REDUCTION=true;
            }
            if(args[i].equals("-m")){
                DO_METHOD_REDUCTION=true;
            }
            if(args[i].equals("-hdd")){
                DO_HDD_REDUCTION=true;
            }
            if(args[i].equals("-p")){
                THIS_RUN_PREFIX=args[i+1];
                THIS_RUN_PREFIX = ""+thisViolation.getConfig1()+"_"+thisViolation.getConfig2()+"/"+THIS_RUN_PREFIX.replace("/","");
                i++;
            }
            if(args[i].equals("-t")){
                TIMEOUT_TIME_MINUTES=Integer.parseInt(args[i+1]);
                i++;
            }


            //add other args here if we want em
        }
    }

    public static boolean DO_METHOD_REDUCTION=false;
    public static boolean DO_CLASS_REDUCTION=false;
    public static boolean DO_HDD_REDUCTION=false;
    static boolean minimized=false;
    static ArrayList<Pair<File,CompilationUnit>> bestCUList = new ArrayList<>();
    static ArrayList<Pair<File,CompilationUnit>> originalCUnits = new ArrayList();

    //this method takes in the flow information and marks some parts of the ast un-removeable (like the source and sink of a flow)
    public static void turnFlowsIntoUnremovableNodes(){


        //get the flow, parse the info and guess which node is this flow
    }

    //main recursion that loops through all nodes
    //we process parents before children
    public static void traverseTree(int currentCU, Node currentNode){

        if(!currentNode.getParentNode().isPresent()&&!(currentNode instanceof CompilationUnit)||currentNode==null){
            return;
        }
        //no longer recur if we are past the time limit
        if(SYSTEM_TIMEOUT_TIME<System.currentTimeMillis())
            return;
        //process node
        process(currentCU, currentNode);
        //traverse children
        for(Node x: currentNode.getChildNodes()){

            traverseTree(currentCU, x);
        }

    }

    public static void handleNodeList(int compPosition, Node currentNode, List<Node> childList){

        //make a copy of the tree

        CompilationUnit copiedUnit = bestCUList.get(compPosition).getValue1().clone();
        Node copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
        ArrayList<Node> alterableList = new ArrayList<Node>(childList);
        ArrayList<Node> copiedList = getCurrentNodeList(copiedNode, alterableList);


        //change the copy
        for(int i=copiedList.size();i>0;i/=2){
            for(int j=0;j<copiedList.size();j+=i){
                List<Node> subList = new ArrayList<>(copiedList.subList(j,Math.min((j+i),copiedList.size())));

                List<Node> removedNodes = new ArrayList<>();
                List<Node> alterableRemoves = new ArrayList<>();
                int index=j;
                for(Node x: subList){
                    if(copiedList.contains(x)){
                        copiedNode.remove(x);
                        removedNodes.add(x);
                        alterableRemoves.add(alterableList.get(index));
                    }
                    index++;
                }

                if(checkChanges(compPosition, copiedUnit)){
                    //if changed remove the nodes we removed from the original ast
                    for(Node x:alterableRemoves){
                        currentNode.remove(x);
                    }


                    copiedList.removeAll(removedNodes);
                    alterableList.removeAll(alterableRemoves);


                    //make another copy and try to run the loop again
                    copiedUnit = bestCUList.get(compPosition).getValue1().clone();
                    copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
                    copiedList = getCurrentNodeList(copiedNode, alterableList);
                    i=copiedList.size()/2;
                    break;
                } else{
                    copiedUnit = bestCUList.get(compPosition).getValue1().clone();
                    copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
                    copiedList = getCurrentNodeList(copiedNode, alterableList);
                }
            }
        }
        //check changes
        //if they worked REMOVE THE SAME NODES FROM ORIGINAL DONT COPY ANYTHING
    }

    //matches the currentNode to what type it is and handles appropriately
    //this returns the currentNode (could be the node we gave it or it's equivalent copy whenever we copied and killed tons of trees)
    public static void process(int currentCUPos, Node currentNode){

        if(!currentNode.getParentNode().isPresent()&&!(currentNode instanceof CompilationUnit)){
            return;
        }
        if(currentNode instanceof ClassOrInterfaceDeclaration){
            ClassOrInterfaceDeclaration node = (ClassOrInterfaceDeclaration) currentNode;

            List<Node> childList = new ArrayList<Node>();
            for(Node x: node.getChildNodes()){
                if(x instanceof BodyDeclaration<?>){
                    childList.add(x);
                }
            }
            handleNodeList(currentCUPos,currentNode, childList);

        }

        if(currentNode instanceof BlockStmt) {

            BlockStmt node = ((BlockStmt) currentNode);
            List<Node> childList = new ArrayList<>();
            for(Node x: node.getChildNodes()){
                if(x instanceof  Statement){
                    childList.add(x);
                }
            }
            handleNodeList(currentCUPos,currentNode, childList);
        }


    }

    //finds an equivalent node in an equivalent tree
    public static Node findCurrentNode(Node currentNode, int compPosition, CompilationUnit copiedUnit){

        Node curNode = currentNode;
        List<Node> traverseList = new ArrayList<>();
        traverseList.add(curNode);
        while(!(curNode instanceof CompilationUnit)){
            curNode = curNode.getParentNode().get();
            traverseList.add(0, curNode);
        }

        curNode = copiedUnit;
        traverseList.remove(0);

        while(!traverseList.isEmpty()){
            for(Node x: curNode.getChildNodes()){
                if(x.equals(traverseList.get(0))){
                    if(traverseList.size()==1){
                        return x;
                    }
                    curNode=x;
                    //System.out.println("Found matching: "+ x.getClass().toGenericString()+"      "+traverseList.get(0).getClass().toGenericString());
                    break;
                }
            }
            traverseList.remove(0);
        }

        return null;

    }

    //just like how with currentNode we have to find it in the ast, we have to find the children we were working on as well
    public static ArrayList<Node> getCurrentNodeList(Node currentNode, List<Node> list){

        //if(LOG_MESSAGES){
           // System.out.println("Current Node in gCNL: " + currentNode);
      //  }
        List<Node> cloneList = currentNode.getChildNodes();

        ArrayList<Node> childrenWeCareAbout = new ArrayList<>(cloneList);

        childrenWeCareAbout.retainAll(list);
        return childrenWeCareAbout;

    }

    //this method is run our ast and see if the changes we made are good or bad (returning true or false) depending
    private static boolean checkChanges(int compPosition, CompilationUnit copiedu){

        boolean returnVal=false;
        performanceLog.addChangeNum();
        try {
            //enter synchronized
            synchronized(lockObject) {
                    //create the apk
                    testerForThis.startApkCreation(projectGradlewPath, projectRootPath, bestCUList, compPosition, copiedu);
                    /*if (testerForThis.createApk(projectGradlewPath, projectRootPath, bestCUList, javaFiles, compPosition, copiedu)) {

                        if (testerForThis.runAQL(projectAPKPath, config1, config2, thisRunName)) {

                            returnVal = true;
                            minimized = false;
                            //this is the best apk yet, save it.
                            saveBestAPK();
                            System.out.println("Successful One\n\n------------------------------------\n\n\n");
                            //for (CompilationUnit x : bestCUList) {
                            //    System.out.println(x);
                            //}
                            System.out.println("CopiedUnit:" + copiedu);
                        }
                    }*/
                    //wait for createApk to be done
                    lockObject.wait();
                    //get the results
                    //if this is true, then the apk created succesfully
                    if(!testerForThis.threadResult){
                        return false;
                    }
                    //start aql process
                    testerForThis.startAQLProcess(projectAPKPath, config1, config2, thisRunName);
                    //testerUtil.startAQLProcess
                    //wait for aqlprocess to be done
                    lockObject.wait();
                    //get the results
                    if(!testerForThis.threadResult){
                        return false;
                    }

                    //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!
                try {
                    long currentLineCount = LineCounter.countLinesDir(projectSrcPath);
                    performanceLog.addCodeChange(currentLineCount);
                }catch(IOException e){
                    e.printStackTrace();
                }
                returnVal = true;
                minimized = false;
                //this is the best apk yet, save it.
                saveBestAPK();
                System.out.println("Successful One\n\n------------------------------------\n\n\n");
                //for (CompilationUnit x : bestCUList) {
                //    System.out.println(x);
                //}
                System.out.println("CopiedUnit:" + copiedu);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return returnVal;
    }

    //this method should take in our java directory and feed all the .java files to our static java parser so we can modify their ASTs
    //it also saves the filenames so we can re-save the files
    //this also calls createInPlaceCopy so we can create an unmodified copy of the original files
    private static boolean handleSrcDirectory(String javadirpath) throws IOException {
        File f = Paths.get(javadirpath).toFile();

        if(!f.exists()){
            throw new FileNotFoundException(javadirpath + "not found");
        }

        String[] extensions = {"java"};
        List<File> allJFiles = ((List<File>) FileUtils.listFiles(f, extensions, true));


        int i=0;
        for(File x: allJFiles){
            //don't add the unmodified source files cause they will just duplicate endlessly
            if(!x.getAbsolutePath().contains("unmodified_src")) {
                i++;
                Pair<File, CompilationUnit> b = new Pair(x, StaticJavaParser.parse(x.getAbsoluteFile()));
                originalCUnits.add(b);
                bestCUList.add(b);
            }
        }

        return true;
    }
    //creates a copy of the unmodified source files because we are going to modify them in-place


    //this method needs to create the project we are going to be debugging by calling a library DroidbenchProjectCreator
    private static void createTargetProject(){

        //this pathfile needs to be unique so it will be apk_config1_config2

        String actualAPK = apkName.substring(apkName.lastIndexOf("/")+1,apkName.lastIndexOf(".apk"));
        String actualConfig1 = config1.substring(config1.lastIndexOf("/")+1,config1.lastIndexOf(".xml"));
        String actualConfig2 = config2.substring(config2.lastIndexOf("/")+1,config2.lastIndexOf(".xml"));
        thisRunName=THIS_RUN_PREFIX+"_"+actualAPK+actualConfig1+actualConfig2;
        String pathFile="debugger/project_files/"+thisRunName;
        System.out.println(pathFile);

        String[] args = {actualAPK, pathFile};
        DroidbenchProjectCreator.createProject(args);


        createProjectPathVars(actualAPK,pathFile);
    }

    //this method just sets up variables we need to do a variety of things
    private static void createProjectPathVars(String APKName, String pathFile) {

        projectRootPath=pathFile;
        projectGradlewPath=pathFile+"/gradlew";
        File f= new File(projectGradlewPath);
        f.setExecutable(true);
        projectAPKPath=pathFile+"/app/build/outputs/apk/debug/app-debug.apk";
        projectSrcPath=pathFile+"/app/src/";
        dealWithSpecialProjects(APKName, pathFile);
        projectClassFiles = pathFile+"/"+projectAPKPath.substring(pathFile.length()+1,projectAPKPath.indexOf("/",pathFile.length()+1))+"/build/intermediates/javac/debug/classes";
        dotFile = new File(projectClassFiles+"/dotfiles/classes.dot");
    }

    private static void dealWithSpecialProjects(String name, String pathFile) {

        //some projects are weird
        switch(name){
            case "DynamicSink1": {
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File f = new File(projectGradlewPath);
                f.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicSink1/build/outputs/apk/debug/dynamicLoading_DynamicSink1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicSink1/src/main/java/";
                break;
            }
            case "Library2": {
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/androidSpecific_Library2/build/outputs/apk/debug/androidSpecific_Library2-debug.apk";
                projectSrcPath = pathFile + "/androidSpecific_Library2/src/main/java/";
                break;
            }
            case "DynamicBoth1":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicBoth1/build/outputs/apk/debug/dynamicLoading_DynamicBoth1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicBoth1/src/main/java/";
                break;
            }
            case "DynamicSource1":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicSource1/build/outputs/apk/debug/dynamicLoading_DynamicSource1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicSource1/src/main/java/";
                break;
            }
            case "DynamicLoadingTarget1":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicLoadingTarget1/build/outputs/apk/debug/dynamicLoading_DynamicLoadingTarget1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicLoadingTarget1/src/main/java/";
                break;
            }
            case "uk.co.yahoo.p1rpp.calendartrigger_7":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/app/build/outputs/apk/debug/CalendarTrigger-debug.apk";
                break;
            }
            case "com.nutomic.ensichat_17":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/android/build/outputs/apk/debug/android-debug.apk";
                projectSrcPath = pathFile + "/android/src/";
                break;
            }
            case "jackpal.androidterm_72":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/term/build/outputs/apk/debug/term-debug.apk";
                projectSrcPath = pathFile + "/term/src/";
                break;
            }
            case "trikita.talalarmo_19":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/build/outputs/apk/debug/trikita.talalarmo_19-debug.apk";
                projectSrcPath = pathFile + "/src/";
                break;
            }


        }
        //TODO:: add osmand and debian kit fossdroid projects
    }

    //root project of the file
    static String projectRootPath;
    static String projectGradlewPath;
    static String projectSrcPath;
    static String projectAPKPath;
    static String thisRunName;
    //static String APKReductionPath="/home/dakota/AndroidTA/AndroidTAEnvironment/APKReductionDir";


    //this method updates the best apk for this run or creates it if it needs to, by the end of the run the best apk should be saved
    private static void saveBestAPK(){
        try {
            File f= new File("debugger/minimized_apks/" +thisRunName+".apk");
            f.mkdirs();
            if(f.exists()){
                f.delete();
            }
            f.createNewFile();
            File fA = new File(projectAPKPath);
            FileUtils.copyFile(fA, f);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static HashMap<String, String> classNamesToPaths;

    public static String getFilePathForClass(String name){
        return classNamesToPaths.get(name);
    }

    private static void fillNamesToPaths(){
        classNamesToPaths = new HashMap<>();

        for(Pair x: originalCUnits){
            findClasses((Node)x.getValue1(), ((File)x.getValue0()).getAbsolutePath());
        }
    }

    private static void findClasses(Node cur, String fileName){

        //this node is a class

        Optional<PackageDeclaration> fullName = ((CompilationUnit) cur).getPackageDeclaration();
        //either get fullName or just defualt to className
        String name = fullName.isPresent()? fullName.get().getNameAsString(): "";
        if(!name.isEmpty())
            name=name+"."+fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf(".java"));
        else{
            name=fileName.substring(fileName.lastIndexOf(File.separator)+1,fileName.lastIndexOf(".java"));
        }
        classNamesToPaths.put(name, fileName);

    }
}
