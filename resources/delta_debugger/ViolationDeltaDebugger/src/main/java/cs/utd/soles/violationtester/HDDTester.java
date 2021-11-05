package cs.utd.soles.violationtester;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.LineCounter;
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

    final Object lockObject;
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

    public HDDTester(Object lock, SetupClass setupClass){
        this.lockObject=lock;
        this.projectInfo=setupClass;
        this.apkCreator=new ApkCreator(this.lockObject, setupClass.getPerfTracker());
        this.aqlRunner=new AqlRunner(this.lockObject,setupClass.getPerfTracker());

    }

    //for binary reduce we need some stuff but yeah

    private boolean checkChanges(ArrayList<Pair<File, CompilationUnit>> cuListToTest, int compPosition, CompilationUnit copiedu){

        boolean returnVal=false;
        projectInfo.getPerfTracker().addCount("ast_changes",1);
        try {
            //enter synchronized
            synchronized(lockObject) {
                //create the apk
                apkCreator.createApk(projectInfo,cuListToTest,compPosition,copiedu,1);

                //wait for createApk to be done
                lockObject.wait();
                //get the results
                //if this is true, then the apk created succesfully
                if(!apkCreator.getThreadResult()){
                    return false;
                }
                //start aql process
                try {
                    aqlRunner.runAql(projectInfo, 1,cuListToTest, compPosition);
                }catch(Exception e){
                    e.printStackTrace();
                    return false;
                }

                //wait for aqlprocess to be done
                lockObject.wait();
                //AQlRunner will have some cool strings in it, so give them to AQLStringHandler to interpret
                //get the results
                if(!aqlRunner.getThreadResult()){
                    return false;
                }

                //if we reach this statement, that means we did a succesful compile and aql run, so we made good changes!
                //TODO:: count lines, performance log add changes
                returnVal=true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return returnVal;
    }
}
