package cs.utd.soles.apkcreator;

import com.github.javaparser.ast.CompilationUnit;
import cs.utd.soles.PerfTracker;
import cs.utd.soles.threads.ProcessThread;
import cs.utd.soles.Runner;
import cs.utd.soles.threads.ThreadHandler;
import cs.utd.soles.setup.SetupClass;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ApkCreator implements ThreadHandler {

    boolean threadResult;
    final Object lockObj;
    private PerfTracker pTracker;
    private static int posChanged=-1;
    private static ArrayList<Pair<File,CompilationUnit>> list;
    public ApkCreator(Object lock, PerfTracker performance){
        this.lockObj=lock;
        this.pTracker=performance;
    }

    public boolean getThreadResult(){
        return threadResult;
    }

    @Override
    public void handleThread(Thread thread, ProcessType type, String finalString, String finalString2) {
        switch(type){
            case CREATE_APK_PROCESS:
                synchronized(lockObj) {
                    threadResult=false;
                    pTracker.stopTimer("compile_timer");
                    //build failed
                    if (!finalString.contains("BUILD SUCCESSFUL") || finalString.contains("BUILD: FAILURE")) {
                        //assembling project failed we don't care why
                        incrementCorrectCount(thread,false);
                        pTracker.resetTimer("compile_timer");
                        threadResult=false;
                        lockObj.notify();
                        return;
                    }
                    else {
                        incrementCorrectCount(thread, true);
                        pTracker.resetTimer("compile_timer");
                        //build worked
                        threadResult = true;
                        lockObj.notify();
                    }
                }
                break;
        }
    }

    private void incrementCorrectCount(Thread thread, boolean passOrFail) {

        ProcessThread pT = (ProcessThread) thread;
        int caller = pT.getCaller();
        String correctName = passOrFail?"good_compile_runs_":"bad_compile_runs_";
        switch(caller){
            case 0:
                correctName+="binary";
                pTracker.addCount(correctName,1);
                correctName="time_"+correctName;
                pTracker.addTime(correctName,pTracker.getTimeForTimer("compile_timer"));
                break;
            case 1:
                correctName+="hdd";
                pTracker.addCount(correctName,1);
                correctName="time_"+correctName;
                pTracker.addTime(correctName,pTracker.getTimeForTimer("compile_timer"));

                if(posChanged>-1&&posChanged<list.size()) {
                    correctName=list.get(posChanged).getValue0().getName();
                    correctName="cucount_"+correctName+(passOrFail?"_good_compile":"_bad_compile");
                    pTracker.addCount(correctName, 1);
                }
                break;
            default:
                break;
        }

    }

    //calls gradlew assembledebug, makes a pthread.
    public void createApk(SetupClass projectNeeds, ArrayList<Pair<File, CompilationUnit>> list, int positionChanged, CompilationUnit changedUnit, int caller){
        String[] command = {projectNeeds.getTargetProject().getProjectGradlewPath(), "clean", "assembleDebug", "-p", projectNeeds.getTargetProject().getProjectRootPath()};
        threadResult=false;
        try{
            saveCompilationUnits(list, positionChanged, changedUnit);
            pTracker.startTimer("compile_timer");
            Process p = Runtime.getRuntime().exec(command);
            ProcessThread pThread = new ProcessThread(p,this,ProcessType.CREATE_APK_PROCESS, 300000, caller);
            pThread.start();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
    public static void saveCompilationUnits(ArrayList<Pair<File, CompilationUnit>> compilationUnits, int positionChanged, CompilationUnit changedUnit) throws IOException {
        list=compilationUnits;
        posChanged=positionChanged;
        int i=0;
        for(Pair<File,CompilationUnit> x: compilationUnits){

            FileWriter fw = new FileWriter(x.getValue0());

            if(i==positionChanged){
                fw.write(changedUnit.toString());
            }else {
                fw.write(x.getValue1().toString());
            }
            fw.flush();
            fw.close();
            i++;
        }
    }


    public void createApkFromList(SetupClass projectNeeds, ArrayList<Pair<File,CompilationUnit>> originalUnits, ArrayList<Pair<File,CompilationUnit>> newUnits, int caller){
        cleanseFiles(originalUnits);
        createApk(projectNeeds,newUnits,newUnits.size()+1,null,caller);

    }

   public static void cleanseFiles(ArrayList<Pair<File,CompilationUnit>> originalUnits) {
        for(Pair<File,CompilationUnit> p : originalUnits){
            File path = p.getValue0();
            if(path.exists())
                path.delete();
        }
    }

}
