package cs.utd.soles.violationtester;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.Runner;
import cs.utd.soles.apkcreator.ApkCreator;
import cs.utd.soles.aqlrunner.AqlRunner;
import cs.utd.soles.setup.SetupClass;
import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;

public class BinaryReductionTester implements Tester {

    private ApkCreator apkCreator;
    private AqlRunner aqlRunner;
    private SetupClass projectInfo;
    private int proposalNum =0;
    public BinaryReductionTester(SetupClass projectInfo) {
        this.apkCreator = new ApkCreator(projectInfo.getPerfTracker());
        this.aqlRunner = new AqlRunner(projectInfo.getPerfTracker());
        this.projectInfo = projectInfo;
    }

    @Override
    public boolean runTest(ArrayList<Object> requireds) {

        ArrayList<Pair<File,CompilationUnit>> originalUnits = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        ArrayList<Pair<File,CompilationUnit>> newUnits = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(1);
        return checkProposal(originalUnits,newUnits);
    }


    private boolean checkProposal(ArrayList<Pair<File, CompilationUnit>> originalUnits,ArrayList<Pair<File, CompilationUnit>> proposal){
        proposalNum++;
        projectInfo.getPerfTracker().addCount("ast_changes",1);
        boolean returnVal=false;

        try{

            //make apk with changes
            //see if compiled
            if (!apkCreator.createApkFromList(projectInfo, originalUnits, proposal, 0)) {
                return false;
            }


            //see if aql worked
            //get the results
            if (!aqlRunner.runAql(projectInfo, 0, null, -1, "Binary-"+proposalNum)) {
                return false;
            }
            //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!
            returnVal = true;
        }catch(Exception e){
            e.printStackTrace();
        }

        return returnVal;
    }



}
