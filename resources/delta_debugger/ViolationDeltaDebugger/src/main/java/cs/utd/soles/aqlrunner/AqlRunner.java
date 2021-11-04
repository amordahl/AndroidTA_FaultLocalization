package cs.utd.soles.aqlrunner;

import cs.utd.soles.threads.AQLThread;
import cs.utd.soles.setup.SetupClass;
import cs.utd.soles.threads.ThreadHandler;

import java.io.IOException;

public class AqlRunner implements ThreadHandler {

    public String getAqlString1() {
        return aqlString1;
    }

    public String getAqlString2() {
        return aqlString2;
    }

    public boolean isThreadDone() {
        return threadDone;
    }

    String aqlString1;
    String aqlString2;
    boolean threadDone;
    final Object lockObject;

    public AqlRunner(Object lockObject) {
        this.lockObject = lockObject;
    }

    @Override
    public void handleThread(Thread thread, ProcessType type, String finalString, String finalString2) {
        switch(type){
            case AQL_RUN:
                synchronized(lockObject){


                    //final results of aql are in both finalString1 and finalString2 respectively
                    //order is config1, config2
                    aqlString1=finalString;
                    aqlString2=finalString2;
                    //TODO::end on aql process timer
                    threadDone=true;
                    lockObject.notify();
                }
                break;
        }
    }

    public void runAql(SetupClass info, int caller) throws IOException {
        aqlString1="";
        aqlString2="";
        threadDone=false;
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
        //TODO:: time start the aql processes
        if(runaql1){
            command1Run=Runtime.getRuntime().exec(command1);
        }
        if(runaql2){
            command2Run= Runtime.getRuntime().exec(command2);
        }

        AQLThread aqlThread = new AQLThread(command1Run, command2Run,this, caller);
        aqlThread.start();
    }

}
