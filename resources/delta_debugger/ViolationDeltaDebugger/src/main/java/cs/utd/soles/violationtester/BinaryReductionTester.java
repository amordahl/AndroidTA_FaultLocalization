package cs.utd.soles.violationtester;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.apkcreator.ApkCreator;
import cs.utd.soles.aqlrunner.AqlRunner;
import cs.utd.soles.setup.SetupClass;
import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;

public class BinaryReductionTester implements Tester {

    final Object lockObject;
    private ApkCreator apkCreator;
    private AqlRunner aqlRunner;
    private SetupClass projectInfo;

    public BinaryReductionTester(Object lockObject, SetupClass projectInfo) {
        this.lockObject = lockObject;
        this.apkCreator = new ApkCreator(this.lockObject, projectInfo.getPerfTracker());
        this.aqlRunner = new AqlRunner(this.lockObject,projectInfo.getPerfTracker());
        this.projectInfo = projectInfo;
    }

    @Override
    public boolean runTest(ArrayList<Object> requireds) {

        ArrayList<Pair<File,CompilationUnit>> originalUnits = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        ArrayList<Pair<File,CompilationUnit>> newUnits = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(1);
        return checkProposal(originalUnits,newUnits);
    }


    private boolean checkProposal(ArrayList<Pair<File, CompilationUnit>> originalUnits,ArrayList<Pair<File, CompilationUnit>> proposal){

        projectInfo.getPerfTracker().addCount("ast_changes",1);
        boolean returnVal=false;

        try{
            synchronized(lockObject) {
                //make apk with changes
                apkCreator.createApkFromList(projectInfo, originalUnits, proposal, 0);
                lockObject.wait();
                //see if compiled
                if (!apkCreator.getThreadResult()) {
                    return false;
                }
                //try aql
                try {
                    aqlRunner.runAql(projectInfo, 0, null, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                lockObject.wait();
                //see if aql worked
                //get the results
                if (!aqlRunner.getThreadResult()) {
                    return false;
                }
                //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!
                //TODO:: count lines, performance log add changes
                returnVal = true;
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        return returnVal;
    }



}
