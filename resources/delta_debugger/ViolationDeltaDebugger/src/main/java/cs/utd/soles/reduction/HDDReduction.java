package cs.utd.soles.reduction;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.utdallas.cs.alps.flows.Flow;
import com.utdallas.cs.alps.flows.Flowset;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.util.JavaByteReader;
import cs.utd.soles.violationtester.HDDTester;
import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HDDReduction implements Reduction{

    long timeoutTime;
    SetupClass programInfo;
    HDDTester tester;
    final Object lock;
    private boolean namValue;
    public HDDReduction(SetupClass programInfo, long timeoutTime){
        this.programInfo=programInfo;
        lock=new Object();
        this.tester = new HDDTester(lock,programInfo);
        this.timeoutTime=timeoutTime+System.currentTimeMillis();
        this.foundUnremoveables=new HashSet<>();
        namValue = programInfo.getArguments().getValueOfArg("NO_ABSTRACT_METHODS").isPresent()? (boolean)programInfo.getArguments().getValueOfArg("NO_ABSTRACT_METHODS").get():false;
    }

    @Override
    public void reduce(ArrayList<Object> requireds) {
        ArrayList<Pair<File,CompilationUnit>> bestCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        Flowset thisViolation = (Flowset) requireds.get(1);
        boolean violationType = (boolean) requireds.get(2);
        boolean isViolation = (boolean) requireds.get(3);
        markNodesUnremoveable(bestCuList,thisViolation, violationType, isViolation);
        hddReduction(bestCuList);
    }

    private void markNodesUnremoveable(ArrayList<Pair<File,CompilationUnit>> bestCuList, Flowset violation, boolean violationType, boolean isViolation){

        //basically, perform operation that tells us if we preserved violatione
        // xcept this time read the flow information and convert that ast, mark those nodes as unremoveable
        ArrayList<Flow> flowsWeWant = getFlowsWeWant(violation,violationType,isViolation);

        ArrayList<com.utdallas.cs.alps.flows.Statement> allStatements = new ArrayList<>();
        for(Flow x: flowsWeWant){
            allStatements.add(x.getSource());
            allStatements.add(x.getSink());
        }
        ArrayList<Node> foundInterfaceAbstractMethods = new ArrayList<>();
        for(Pair<File,CompilationUnit> pair: bestCuList){
            traverseTreeAndMark(pair.getValue1(),allStatements, foundInterfaceAbstractMethods);
        }

        ArrayList<Node> addSet = new ArrayList<>(foundUnremoveables);
        //mark all node parents
        for(Node x: addSet){
            markAllParentNodes(x);
        }
        //readd this after markAllParentNodes(x), so that we dont go marking alot of node as unremoveable for no reason.
        foundUnremoveables.addAll(foundInterfaceAbstractMethods);
        for(Node x: foundUnremoveables){
            System.out.println("\n\nNode that is unremoveable: "+x);
        }

    }

    private void markAllParentNodes(Node x) {

        Node parent = x.getParentNode().isPresent()? x.getParentNode().get() :null;
        while(parent!=null){
            foundUnremoveables.add(parent);
            parent = parent.getParentNode().isPresent()? parent.getParentNode().get() :null;
        }
    }

    private ArrayList<Flow> getFlowsWeWant(Flowset violation, boolean type, boolean isViolation){
        ArrayList<Flow> returnList = new ArrayList<>();

        if(isViolation){
            //this is a violation
            ArrayList<Flow> config1Flows = new ArrayList<>(violation.getConfig1_FlowList());
            ArrayList<Flow> config2Flows = new ArrayList<>(violation.getConfig2_FlowList());
            //do our operation
            config2Flows.removeAll(config1Flows);
            returnList.addAll(config2Flows);

        }else{
            //this is not a violation
            //preserve everything
            returnList.addAll(violation.getConfig1_FlowList());
            returnList.addAll(violation.getConfig2_FlowList());
        }

        return returnList;
    }
    private void traverseTreeAndMark(Node cur, ArrayList<com.utdallas.cs.alps.flows.Statement> flowsWeWant, ArrayList<Node> foundInterfaceAbstractMethods){

        if(flowsWeWant.size()==0){
            return;
        }
        //start process for source/sink non removal
        markNode(cur,flowsWeWant, foundInterfaceAbstractMethods);

        for(Node x: cur.getChildNodes()){
            traverseTreeAndMark(x,flowsWeWant, foundInterfaceAbstractMethods);
        }
    }




    //this method checks our current node to a source/sink signature, also calls our other removal if wanted
    private void markNode(Node cur, ArrayList<com.utdallas.cs.alps.flows.Statement> flowsWeWant, ArrayList<Node> foundInterfaceAbstractMethods) {



        if(cur instanceof ClassOrInterfaceDeclaration){
            markNodeC((ClassOrInterfaceDeclaration) cur, flowsWeWant);
        }else if(cur instanceof MethodDeclaration){
            //when we get to a method that has same signature in same class as source/sink, lets just look through the statements and find one that looks good
            markNodeM((MethodDeclaration) cur, flowsWeWant);

            //check if this method if from an interface or superclass
            if(namValue&&checkInterfaceOrAbstractMethod((MethodDeclaration) cur)){
                //it is, so add it another list that we will reincorporate into flowsUnremoveable later.
                foundInterfaceAbstractMethods.add(cur);
            }

        }else{

        }

    }
    //part of source/sink
    private void markNodeC(ClassOrInterfaceDeclaration cur, ArrayList<com.utdallas.cs.alps.flows.Statement> flowsWeWant){
        //this node is a class, lets see if it matches our cool flows

        for(com.utdallas.cs.alps.flows.Statement x: flowsWeWant){

            String className = x.getClassname();
            String nameScope = cur.getFullyQualifiedName().isPresent()? cur.getFullyQualifiedName().get():cur.getNameAsString();
            if(className.contains(nameScope)){
                //prob right
                System.out.println("Found unremoveable class : "+cur);
                foundUnremoveables.add(cur);
            }
        }
    }
    //part of source/sink
    private void markNodeM(MethodDeclaration cur, ArrayList<com.utdallas.cs.alps.flows.Statement> flowsWeWant){
        //ok so this is a method, we can mark this method as unremoveable, but also we go through it and mark a specific line as unremoveable;

        ArrayList<com.utdallas.cs.alps.flows.Statement> removeStatement = new ArrayList<>();
        for(com.utdallas.cs.alps.flows.Statement x: flowsWeWant){

            //are we in class
            if(checkIfNodeInClass(x.getClassname(), cur)){
                //are we in method
                if(checkMethodSig(cur,x)){
                    System.out.println("Found unremoveable method: "+cur);
                    foundUnremoveables.add(cur);

                    findAndMarkStatement(cur, x);
                    removeStatement.add(x);
                }
            }

        }
        flowsWeWant.removeAll(removeStatement);
    }
    //part of source/sink
    //TODO:: this stuff isn't gonnna work for anonymous classes, so we need to fix them eventually.
    private boolean findAndMarkStatement(MethodDeclaration cur, com.utdallas.cs.alps.flows.Statement thisFlow) {

        ArrayList<Node> checkList = new ArrayList<>();
        checkList.add(cur);
        while(!checkList.isEmpty()){
            Node curNode = checkList.remove(0);
            boolean add=true;
            //filter out anonymous classes expressions
            if(curNode instanceof ObjectCreationExpr){
                if(((ObjectCreationExpr) curNode).getAnonymousClassBody().isPresent()){
                    add=false;
                }
            }

            if(add) {
                checkList.addAll(curNode.getChildNodes());
                if(curNode instanceof MethodCallExpr){
                    if(thisFlow.getStatement().contains(((MethodCallExpr)curNode).getNameAsString())){
                        System.out.println("Found unremoveable statement: "+curNode);
                        foundUnremoveables.add(curNode);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkMethodSig(MethodDeclaration cur, com.utdallas.cs.alps.flows.Statement x) {

        String name = cur.getSignature().asString();

        String methodSig = convertNodeString(x.getMethod());

        if(name.trim().equals(methodSig.trim())){
            return true;
        }

        return false;
    }

    private static String convertNodeString(String nodeString){
        // <<returnType> <methodName>(<>* parameterTypes)>

        //cut first and last
        nodeString = nodeString.substring(1,nodeString.length()-1).replace(": "," ");
        String[] elements = nodeString.split(" ");
        String classPackageName = elements[0];
       // System.out.println("ClassPackageName: "+classPackageName);
        String returnType = elements[1];
       // System.out.println("return type: "+returnType);
        String methodName = elements[2].substring(0,elements[2].indexOf("("));
       // System.out.println("method name: "+methodName);
        String[] parameterTypeStrings=null;

        int startParam=elements[2].indexOf("(");
        int endParam=elements[2].lastIndexOf(")");

        if(endParam==startParam+1){
            parameterTypeStrings=new String[0];
        }
        else if(elements[2].contains(",")) {
            parameterTypeStrings= elements[2].substring(startParam + 1, endParam).trim().split(",");
        }else{
            parameterTypeStrings=new String[1];
            parameterTypeStrings[0]=elements[2].substring(startParam+1,endParam);
        }

       // System.out.println("parameter types: "+ Arrays.toString(parameterTypeStrings));
        String[] returnList = new String[3+parameterTypeStrings.length];
        returnList[0]=classPackageName;
        returnList[1]=returnType;
        returnList[2]=methodName;
        for(int i=3;i<returnList.length;i++){
            returnList[i]=parameterTypeStrings[i-3].substring(parameterTypeStrings[i-3].lastIndexOf(".")+1);
        }
      //  System.out.println(Arrays.toString(returnList));

        String returnString = "";

        returnString+=methodName+"(";
        for(int i=3;i<returnList.length;i++){
            returnString+=returnList[i]+",";
        }
        if(returnString.charAt(returnString.length()-1)==',')
            returnString=returnString.substring(0,returnString.length()-1);
        returnString=returnString+")";


        return returnString;
    }

    private boolean checkIfNodeInClass(String className, MethodDeclaration cur) {

        ArrayList<Node> parents = new ArrayList<>();
        parents.add(cur);
        while(!parents.isEmpty()){
            Node curP = parents.remove(0);
            if(curP.getParentNode().isPresent()){
                parents.add(curP.getParentNode().get());
            }
            if(curP instanceof ClassOrInterfaceDeclaration){
                String nameScope = ((ClassOrInterfaceDeclaration) curP).getFullyQualifiedName().isPresent()?
                        ((ClassOrInterfaceDeclaration) curP).getFullyQualifiedName().get()
                        : ((ClassOrInterfaceDeclaration) curP).getNameAsString();
                if(className.contains(nameScope)){
                    //prob right
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkInterfaceOrAbstractMethod(MethodDeclaration methodDec){
        //get parent
        Node parentC =  methodDec.getParentNode().get();
        CombinedTypeSolver solver = programInfo.getTypeSolver();
        System.out.println("checking interface or abstract method");
        //check
        if(parentC instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration parent = (ClassOrInterfaceDeclaration) parentC;

            /*if(!parent.isInterface()&&!parent.isAbstract()){
                //dont care about methods that are removeable but can be overriden from superclass.
                return false;
            }*/

            NodeList<ClassOrInterfaceType> extendedClassTypes = parent.getExtendedTypes();
            extendedClassTypes.addAll(parent.getImplementedTypes());
            //if(!parent.isAbstract()) {
            //    return false;
            //}
            for (ClassOrInterfaceType t : extendedClassTypes) {
                try {
                    SymbolReference<ResolvedReferenceTypeDeclaration> resType = solver.tryToSolveType(t.getNameWithScope());
                    //check sig in abstract class method, for abstract methods

                    ResolvedReferenceType rrt = t.resolve();
                    if (rrt != null && !resType.isSolved()) {
                        resType = solver.tryToSolveType(rrt.getQualifiedName());
                        //System.out.println("new try: "+resType.isSolved());
                    }



                    if (resType.isSolved()) {

                        if (resType.getCorrespondingDeclaration() instanceof ResolvedClassDeclaration) {
                            ResolvedClassDeclaration classDec = resType.getCorrespondingDeclaration().asClass();

                            Set<ResolvedMethodDeclaration> resMethods = classDec.getDeclaredMethods();
                            String footPrint = "";
                            String name = methodDec.getNameAsString();
                            footPrint += name;

                            for (Parameter p : methodDec.getParameters()) {
                                footPrint += " " + p.getType().resolve().describe();
                            }
                            //System.out.println(footPrint);

                            //tryToResolveMethods(resMethods,methodSignatures,solver);
                            for (ResolvedMethodDeclaration methodDecX : resMethods) {
                                if (methodDecX instanceof JavassistMethodDeclaration) {
                                    JavassistMethodDeclaration yaboy = (JavassistMethodDeclaration) methodDecX;
                                    String things = yaboy.toString();

                                    things = things.substring(things.indexOf("[") + 1, things.lastIndexOf("]"));


                                    String methodSig = JavaByteReader.getMethodSigFromString(things);
                                    if (methodSig.equals(footPrint) && yaboy.isAbstract()) {
                                        return true;
                                    }
                                }
                                if (methodDecX instanceof JavaParserMethodDeclaration){
                                    JavaParserMethodDeclaration yaboy = (JavaParserMethodDeclaration) methodDecX;
                                    String things = yaboy.getSignature();


                                    String methodSig = JavaByteReader.getMethodSigFromGetSig(things);
                                    if (methodSig.equals(footPrint) && yaboy.isAbstract()) {
                                        return true;
                                    }
                                }
                            }
                        }
                        else if(resType.getCorrespondingDeclaration() instanceof ResolvedInterfaceDeclaration){
                            ResolvedInterfaceDeclaration classDec = resType.getCorrespondingDeclaration().asInterface();

                            Set<ResolvedMethodDeclaration> resMethods = classDec.getDeclaredMethods();
                            String footPrint = "";
                            String name = methodDec.getNameAsString();
                            footPrint += name;

                            for (Parameter p : methodDec.getParameters()) {
                                footPrint += " " + p.getType().resolve().describe();
                            }
                            //System.out.println(footPrint);

                            for (ResolvedMethodDeclaration methodDecX : resMethods) {
                                if (methodDecX instanceof JavassistMethodDeclaration) {
                                    JavassistMethodDeclaration yaboy = (JavassistMethodDeclaration) methodDecX;
                                    String things = yaboy.toString();

                                    things = things.substring(things.indexOf("[") + 1, things.lastIndexOf("]"));


                                    String methodSig = JavaByteReader.getMethodSigFromString(things);
                                    if (methodSig.equals(footPrint)) {
                                        return true;
                                    }

                                }
                                if (methodDecX instanceof JavaParserMethodDeclaration) {
                                    JavaParserMethodDeclaration yaboy = (JavaParserMethodDeclaration) methodDecX;
                                    String things = yaboy.getSignature();

                                    String methodSig = JavaByteReader.getMethodSigFromGetSig(things);
                                    if (methodSig.equals(footPrint)) {
                                        return true;
                                    }

                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
        return false;
    }

    private boolean minimized=false;
    public void hddReduction(ArrayList<Pair<File, CompilationUnit>> bestCuList){

        minimized=false;
        programInfo.getPerfTracker().startTimer("hdd_timer");
        while(!minimized&&System.currentTimeMillis()<timeoutTime){
            minimized=true;
            programInfo.getPerfTracker().addCount("total_rotations",1);
            int i=0;
            for (Pair<File, CompilationUnit> compilationUnit : bestCuList) {
                //if we are under the time limit, traverse the tree
                if(System.currentTimeMillis()<timeoutTime)
                    traverseTree(i, compilationUnit.getValue1(), bestCuList);
                i++;
            }
        }
        programInfo.getPerfTracker().stopTimer("hdd_timer");
    }

    private void traverseTree(int currentCU, Node currentNode, ArrayList<Pair<File, CompilationUnit>> bestCuList){


        if(!currentNode.getParentNode().isPresent()&&!(currentNode instanceof CompilationUnit)||currentNode==null){
            return;
        }
        //no longer recur if we are past the time limit
        if(timeoutTime<System.currentTimeMillis())
            return;
        //process node
        process(currentCU, currentNode, bestCuList);
        //traverse children
        for(Node x: currentNode.getChildNodes()){

            traverseTree(currentCU, x, bestCuList);
        }

    }

    private void process(int currentCUPos, Node currentNode, ArrayList<Pair<File, CompilationUnit>> bestCuList){

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
            handleNodeList(currentCUPos,currentNode, childList, bestCuList);

        }
        if(currentNode instanceof BlockStmt) {

            BlockStmt node = ((BlockStmt) currentNode);
            List<Node> childList = new ArrayList<>();
            for(Node x: node.getChildNodes()){
                if(x instanceof Statement){
                    childList.add(x);
                }
            }
            handleNodeList(currentCUPos,currentNode, childList, bestCuList);
        }


    }

    private void handleNodeList(int compPosition, Node currentNode, List<Node> childList, ArrayList<Pair<File, CompilationUnit>> bestCuList){

        //make a copy of the tree
        CompilationUnit copiedUnit = bestCuList.get(compPosition).getValue1().clone();
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
                    if(copiedList.contains(x)&&nodeIsRemoveable(x)){
                        copiedNode.remove(x);
                        removedNodes.add(x);
                        alterableRemoves.add(alterableList.get(index));
                        programInfo.getPerfTracker().addCount("rejected_changes",1);
                    }
                    index++;
                }

                ArrayList<Object> requiredForTest = new ArrayList<>();
                requiredForTest.add(bestCuList);
                requiredForTest.add(compPosition);
                requiredForTest.add(copiedUnit);
                if(removedNodes.size()>0&&tester.runTest(requiredForTest)){
                    //if changed, remove the nodes we removed from the original ast
                    for(Node x:alterableRemoves){
                        currentNode.remove(x);
                    }

                    minimized=false;
                    copiedList.removeAll(removedNodes);
                    alterableList.removeAll(alterableRemoves);

                    //make another copy and try to run the loop again
                    copiedUnit = bestCuList.get(compPosition).getValue1().clone();
                    copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
                    copiedList = getCurrentNodeList(copiedNode, alterableList);
                    i=copiedList.size()/2;
                    break;
                } else{
                    copiedUnit = bestCuList.get(compPosition).getValue1().clone();
                    copiedNode = findCurrentNode(currentNode, compPosition, copiedUnit);
                    copiedList = getCurrentNodeList(copiedNode, alterableList);
                }
            }
        }
        //check changes
        //if they worked REMOVE THE SAME NODES FROM ORIGINAL DONT COPY ANYTHING
    }

    private Node findCurrentNode(Node currentNode, int compPosition, CompilationUnit copiedUnit){

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


    private HashSet<Node> foundUnremoveables;
    //special rules to ignore removing dumb things, like the source/sink
    private boolean nodeIsRemoveable(Node node){

        //first check if we seen this before.
        if(foundUnremoveables.contains(node)){
            return false;
        }
        return true;
    }

    private ArrayList<Node> getCurrentNodeList(Node currentNode, List<Node> list){

        //if(LOG_MESSAGES){
        // System.out.println("Current Node in gCNL: " + currentNode);
        //  }
        List<Node> cloneList = currentNode.getChildNodes();

        ArrayList<Node> childrenWeCareAbout = new ArrayList<>(cloneList);

        childrenWeCareAbout.retainAll(list);
        return childrenWeCareAbout;

    }
}
