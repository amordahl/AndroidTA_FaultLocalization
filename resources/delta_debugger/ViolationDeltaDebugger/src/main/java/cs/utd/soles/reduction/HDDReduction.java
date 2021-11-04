package cs.utd.soles.reduction;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.violationtester.HDDTester;
import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HDDReduction implements Reduction{

    long timeoutTime;
    SetupClass programInfo;
    HDDTester tester;
    final Object lock;
    public HDDReduction(SetupClass programInfo, long timeoutTime){
        this.programInfo=programInfo;
        lock=new Object();
        this.tester = new HDDTester(lock,programInfo);
        this.timeoutTime=timeoutTime+System.currentTimeMillis();
    }
    @Override
    public void reduce(ArrayList<Object> requireds) {
        ArrayList<Pair<File,CompilationUnit>> bestCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        hddReduction(bestCuList);
    }


    public void hddReduction(ArrayList<Pair<File, CompilationUnit>> bestCuList){

        programInfo.getPerfTracker().startTimer("hdd_timer");
        Boolean minimized=false;
        while(!minimized&&System.currentTimeMillis()<timeoutTime){
            minimized=true;
            programInfo.getPerfTracker().addCount("total_rotations",1);
            int i=0;
            for (Pair<File, CompilationUnit> compilationUnit : bestCuList) {
                //if we are under the time limit, traverse the tree
                if(System.currentTimeMillis()<timeoutTime)
                    traverseTree(i, compilationUnit.getValue1(), bestCuList, minimized);
                i++;
            }
        }
        programInfo.getPerfTracker().stopTimer("hdd_timer");
    }

    private void traverseTree(int currentCU, Node currentNode, ArrayList<Pair<File, CompilationUnit>> bestCuList, Boolean minimized){


        if(!currentNode.getParentNode().isPresent()&&!(currentNode instanceof CompilationUnit)||currentNode==null){
            return;
        }
        //no longer recur if we are past the time limit
        if(timeoutTime<System.currentTimeMillis())
            return;
        //process node
        process(currentCU, currentNode, bestCuList,minimized);
        //traverse children
        for(Node x: currentNode.getChildNodes()){

            traverseTree(currentCU, x, bestCuList, minimized);
        }

    }
    private void process(int currentCUPos, Node currentNode, ArrayList<Pair<File, CompilationUnit>> bestCuList, Boolean minimized){

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
            handleNodeList(currentCUPos,currentNode, childList, bestCuList,minimized);

        }
        if(currentNode instanceof BlockStmt) {

            BlockStmt node = ((BlockStmt) currentNode);
            List<Node> childList = new ArrayList<>();
            for(Node x: node.getChildNodes()){
                if(x instanceof Statement){
                    childList.add(x);
                }
            }
            handleNodeList(currentCUPos,currentNode, childList, bestCuList, minimized);
        }


    }

    private void handleNodeList(int compPosition, Node currentNode, List<Node> childList, ArrayList<Pair<File, CompilationUnit>> bestCuList, Boolean minimized){

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
                    if(copiedList.contains(x)){
                        copiedNode.remove(x);
                        removedNodes.add(x);
                        alterableRemoves.add(alterableList.get(index));
                    }
                    index++;
                }

                ArrayList<Object> requiredForTest = new ArrayList<>();
                requiredForTest.add(bestCuList);
                requiredForTest.add(compPosition);
                requiredForTest.add(copiedUnit);
                if(tester.runTest(requiredForTest)){
                    minimized=false;
                    //if changed, remove the nodes we removed from the original ast
                    for(Node x:alterableRemoves){
                        currentNode.remove(x);
                    }


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
