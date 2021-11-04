package cs.utd.soles.aqlrunner;

import cs.utd.soles.PerfTracker;
import cs.utd.soles.threads.AQLThread;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.ProcessThread;
import cs.utd.soles.threads.ThreadHandler;

import java.io.IOException;

public class AqlRunner implements ThreadHandler {

    public boolean getThreadResult() {
        return threadResult;
    }

    boolean threadResult;
    final Object lockObject;
    PerfTracker pTracker;
    private SetupClass setupInfo;

    public AqlRunner(Object lockObject, PerfTracker pT) {
        this.lockObject = lockObject;
        this.pTracker=pT;
    }

    @Override
    public void handleThread(Thread thread, ProcessType type, String finalString, String finalString2) {
        switch(type){
            case AQL_RUN:
                synchronized(lockObject){


                    //final results of aql are in both finalString1 and finalString2 respectively
                    //order is config1, config2
                    pTracker.stopTimer("aql_timer");
                    threadResult=AQLStringHandler.handleAQL(setupInfo,finalString,finalString2);
                    if(threadResult){
                        //good one
                    }else{
                        //failed one
                    }
                    setupInfo=null;
                    lockObject.notify();
                }
                break;
        }
    }

    public void runAql(SetupClass info, int caller) throws IOException {
        threadResult=false;
        setupInfo=info;
        //this bit runs and captures the output of the aql script
        String command1 = "python runaql.py "+info.getConfig1()+" "+info.getTargetProject().getProjectAPKPath()+" -f";
        String command2 = "python runaql.py "+info.getConfig2()+" "+info.getTargetProject().getProjectAPKPath()+" -f";


        boolean runaql1=true;
        boolean runaql2=true;
        //if its a violation always run both
        if(info.isViolationOrNot()){
            runaql1=true;
            runaql2=true;
        }else{
            if(info.getThisViolation().getConfig1_FlowList().size()==0)
                runaql1=false;
            if(info.getThisViolation().getConfig2_FlowList().size()==0)
                runaql2=false;
        }

        //start these commands and then handle them somewhere else
        Process command1Run = null;
        Process command2Run = null;
        pTracker.startTimer("aql_process");
        if(runaql1){
            command1Run=Runtime.getRuntime().exec(command1);
        }
        if(runaql2){
            command2Run= Runtime.getRuntime().exec(command2);
        }

        AQLThread aqlThread = new AQLThread(command1Run, command2Run,this, caller);
        aqlThread.start();
    }

    private void incrementCorrectCount(Thread thread, boolean passOrFail) {

        AQLThread pT = (AQLThread) thread;
        int caller = pT.getCaller();
        String correctName = passOrFail?"good_aql_runs_":"bad_aql_runs_";
        switch(caller){
            case 0:
                correctName+="binary";
                pTracker.addCount(correctName,1);
                correctName="time_"+correctName;
                pTracker.addTime(correctName,pTracker.getTimeForTimer("aql_timer"));
                break;
            case 1:
                correctName+="hdd";
                pTracker.addCount(correctName,1);
                correctName="time_"+correctName;
                pTracker.addTime(correctName,pTracker.getTimeForTimer("aql_timer"));
                break;
            default:
                break;
        }

    }

}
