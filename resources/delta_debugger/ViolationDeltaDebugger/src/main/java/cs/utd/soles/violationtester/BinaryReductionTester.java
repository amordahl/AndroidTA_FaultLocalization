package cs.utd.soles.violationtester;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.apkcreator.ApkCreator;
import cs.utd.soles.aqlrunner.AQLStringHandler;
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
        this.apkCreator = new ApkCreator(lockObject, projectInfo.getPerfTracker());
        this.aqlRunner = new AqlRunner(lockObject);
        this.projectInfo = projectInfo;
    }

    @Override
    public boolean runTest(ArrayList<Object> requireds) {

        ArrayList<Pair<File,CompilationUnit>> originalUnits = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        ArrayList<Pair<File,CompilationUnit>> newUnits = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(1);
        return checkProposal(originalUnits,newUnits);
    }


    private boolean checkProposal(ArrayList<Pair<File, CompilationUnit>> originalUnits,ArrayList<Pair<File, CompilationUnit>> proposal){

        //TODO:: addChangeNum()
        boolean returnVal=false;

        try{
            //make apk with changes
            apkCreator.createApkFromList(projectInfo,originalUnits,proposal,0);
            lockObject.wait();
            //see if compiled
            if(!apkCreator.getThreadResult()){
                return false;
            }
            //try aql
            try {
                aqlRunner.runAql(projectInfo, 0);
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            lockObject.wait();
            //see if aql worked
            boolean result = AQLStringHandler.handleAQL(projectInfo,aqlRunner.getAqlString1(),aqlRunner.getAqlString2());
            //get the results
            if(!result){
                return false;
            }
            //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!
            //TODO:: count lines, performance log add changes
            returnVal=true;
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        return returnVal;
    }



}
