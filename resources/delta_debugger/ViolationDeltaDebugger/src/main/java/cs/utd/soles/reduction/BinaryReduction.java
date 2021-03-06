package cs.utd.soles.reduction;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import cs.utd.soles.classgraph.DependencyGraph;
import cs.utd.soles.apkcreator.ApkCreator;
import cs.utd.soles.dotfilecreator.DotFileCreator;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.classgraph.ClassNode;
import cs.utd.soles.violationtester.BinaryReductionTester;
import org.javatuples.Pair;

import java.io.File;
import java.util.*;

public class BinaryReduction implements Reduction{

    SetupClass programInfo;
    HashMap<String, String> classNamesToPaths;
    final Object lock;
    BinaryReductionTester tester;

    long timeout_time;

    public BinaryReduction(SetupClass programInfo, ArrayList<Pair<File, CompilationUnit>> originalUnits, long timeOutTime){
        this.programInfo=programInfo;
        fillNamesToPaths(originalUnits);
        lock = new Object();
        tester = new BinaryReductionTester(lock,programInfo);
        timeout_time = timeOutTime+System.currentTimeMillis();
    }

    @Override
    public void reduce(ArrayList<Object> requireds) {
        ArrayList<Pair<File,CompilationUnit>> originalCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        ArrayList<Pair<File,CompilationUnit>> bestCuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(1);

        programInfo.getPerfTracker().startTimer("binary_timer");

        DependencyGraph graph = createDependencyNodes(originalCuList);
        ArrayList<HashSet<ClassNode>> closures = graph.getTransitiveClosuresDifferent();
        binaryReduction(closures, originalCuList, bestCuList);

        programInfo.getPerfTracker().stopTimer("binary_timer");
    }

    private DependencyGraph createDependencyNodes(ArrayList<Pair<File, CompilationUnit>> bestCuList) {
        synchronized(lock){
            try {
                ApkCreator creator = new ApkCreator(lock,programInfo.getPerfTracker());
                creator.createApkFromList(programInfo, bestCuList, bestCuList, -1);
                lock.wait();

                if(!creator.getThreadResult()){
                    System.out.println("BUILD FAILED, FAULTY PROJECT");
                    System.exit(-1);
                }
                File dotFile = DotFileCreator.createDotForProject(programInfo);
                DependencyGraph rg = new DependencyGraph();
                rg.parseGraphFromDot(dotFile, classNamesToPaths);
                return rg;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public void binaryReduction(ArrayList<HashSet<ClassNode>> closures, ArrayList<Pair<File, CompilationUnit>> originalCuList, ArrayList<Pair<File, CompilationUnit>> bestCuList){
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
        while(r>0&&i<=r && (System.currentTimeMillis() < timeout_time)){

            HashSet<ClassNode> proposal = new HashSet<>(knownNodes);
            if(proposal.size()==0&&i==0){
                i++;
            }
            int j=0;
            for(;j<i;j++){
                proposal.addAll(unknownNodes.get(j));
            }
            //match the proposal to the compilation units

            ArrayList<Pair<File,CompilationUnit>> newProgramConfig = matchProposal(proposal,bestCuList);

            ArrayList<Object> requiredForTest = new ArrayList<>();
            requiredForTest.add(originalCuList);
            requiredForTest.add(newProgramConfig);
            //if this works then update namedBestCUS to be good else
            if(tester.runTest(requiredForTest)){
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
                bestCuList=newProgramConfig;
                //restart our search
                i=0;
            }
            //revert, just write all the things from bestcus
            else{
                ApkCreator.cleanseFiles(originalCuList);
                try {
                    ApkCreator.saveCompilationUnits(bestCuList, bestCuList.size() + 1, null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            i++;

        }
    }



    private void findClasses(Node cur, String fileName){

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
    private void fillNamesToPaths(ArrayList<Pair<File,CompilationUnit>> originalCUnits){
        classNamesToPaths = new HashMap<>();

        for(Pair x: originalCUnits){
            findClasses((Node)x.getValue1(), ((File)x.getValue0()).getAbsolutePath());
        }
    }
    public String getFilePathForClass(String name){
        return classNamesToPaths.get(name);
    }
    private static ArrayList<Pair<File,CompilationUnit>> matchProposal(HashSet<ClassNode> proposal, ArrayList<Pair<File,CompilationUnit>> bestCuList){
        ArrayList<Pair<File,CompilationUnit>> matchedProposal = new ArrayList<>();

        for(ClassNode x: proposal){
            String filePath = x.getFilePath();
            for(Pair pir: bestCuList){
                if(((File)pir.getValue0()).getAbsolutePath().equals(filePath)){
                    matchedProposal.add(pir);
                    break;

                }
            }
            //System.out.println(filePath);
        }
        return matchedProposal;
    }
}
