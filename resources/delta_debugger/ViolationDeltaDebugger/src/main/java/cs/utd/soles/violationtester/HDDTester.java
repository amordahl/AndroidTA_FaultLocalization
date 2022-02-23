package cs.utd.soles.violationtester;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.LineCounter;
import cs.utd.soles.Runner;
import cs.utd.soles.apkcreator.ApkCreator;
import cs.utd.soles.aqlrunner.AQLStringHandler;
import cs.utd.soles.aqlrunner.AqlRunner;
import cs.utd.soles.setup.SetupClass;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class HDDTester implements Tester {

    private ApkCreator apkCreator;
    private AqlRunner aqlRunner;
    private SetupClass projectInfo;


    @Override
    public boolean runTest(ArrayList<Object> requireds) {
        //for binary reduction we need 3 things, a ArrayList<Pair<File,CompilationUnit>> Culist, a int comp position, and CompilationUnit copied unit
        ArrayList<Pair<File,CompilationUnit>> cuList = (ArrayList<Pair<File, CompilationUnit>>) requireds.get(0);
        Integer compPosition = (Integer) requireds.get(1);
        CompilationUnit copiedUnit = (CompilationUnit) requireds.get(2);

        return checkChanges(cuList,compPosition,copiedUnit);
    }

    public HDDTester(SetupClass setupClass){
        this.projectInfo=setupClass;
        this.apkCreator=new ApkCreator(setupClass.getPerfTracker());
        this.aqlRunner=new AqlRunner(setupClass.getPerfTracker());

    }

    //for binary reduce we need some stuff but yeah

    private boolean checkChanges(ArrayList<Pair<File, CompilationUnit>> cuListToTest, int compPosition, CompilationUnit copiedu){

        boolean returnVal=false;
        projectInfo.getPerfTracker().addCount("ast_changes",1);
        try {
            //make apk with changes
            //see if compiled
            if (!apkCreator.createApk(projectInfo, cuListToTest, compPosition,copiedu,1)) {
                return false;
            }


            //see if aql worked
            //get the results
            if (!aqlRunner.runAql(projectInfo, 1, null, -1)) {
                return false;
            }
            //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!

            Runner.saveBestAPK(projectInfo);
            returnVal = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnVal;
    }
}
