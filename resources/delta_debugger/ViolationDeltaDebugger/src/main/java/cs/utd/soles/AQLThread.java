package cs.utd.soles;

public class AQLThread extends Thread implements ThreadHandler{

    ThreadHandler t;
    Process aql1;
    Process aql2;
    //result of an aql thread is the strings of the two processes
    String aql1FinalString;
    String aql2FinalString;

    final Object lockObj;
    int doneCount;
    public AQLThread(Process aql1, Process aql2, ThreadHandler t){

        this.aql1=aql1;
        this.aql2=aql2;
        this.t=t;
        lockObj = new Object();
        doneCount=0;
    }

    public void run(){
        //timeout is like, two hours. (5 minutes * 24 = 2 hours)
        ProcessThread aql1Thread = new ProcessThread(aql1,this,ProcessType.AQL_PROCESS1, 300000*24);
        aql1Thread.start();

        ProcessThread aql2Thread = new ProcessThread(aql2,this,ProcessType.AQL_PROCESS2, 300000*24);
        aql2Thread.start();
        synchronized(lockObj){
            try {
                while(doneCount<2)
                    lockObj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t.handleThread(ProcessType.AQL_RUN, aql1FinalString, aql2FinalString);
        }

    }


    @Override
    public void handleThread(ProcessType type, String finalString,String  finalString2) {
        switch(type){
            case AQL_PROCESS1:
                synchronized (lockObj){

                    //AQL 1 is done, handle
                    aql1FinalString=finalString;

                    doneCount++;
                    lockObj.notify();
                }

                break;
            case AQL_PROCESS2:
                synchronized (lockObj){
                    //aql2 is done, handle
                    aql2FinalString=finalString;

                    doneCount++;
                    lockObj.notify();
                }
                break;
        }
    }
}
