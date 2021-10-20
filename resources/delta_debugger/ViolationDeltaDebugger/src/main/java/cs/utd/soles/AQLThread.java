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
    public int caller;
    public AQLThread(Process aql1, Process aql2, ThreadHandler t, int caller){

        this.aql1=aql1;
        this.aql2=aql2;
        this.t=t;
        lockObj = new Object();
        doneCount=0;
        this.caller=caller;
    }

    public void run(){

        //if either of the Process are null that means we didnt run that command, handle accordingly

        //keep track of when we stop, normally 2 but if we got 1 we arent running then we stop at 1 rather than 2.
        int nonNullCount=2;
        //timeout is like, two hours. (5 minutes * 24 = 2 hours)
        if(aql1 !=null) {
            //run like normal - its not null
            ProcessThread aql1Thread = new ProcessThread(aql1, this, ProcessType.AQL_PROCESS1, 300000 * 24,caller);
            aql1Thread.start();

        }else{
            //its null, lower the count and provide an empty answer.
            aql1FinalString="<answer/>";
            nonNullCount--;
        }

        if(aql2 !=null) {
            ProcessThread aql2Thread = new ProcessThread(aql2, this, ProcessType.AQL_PROCESS2, 300000 * 24,caller);
            aql2Thread.start();
        }else{
            aql2FinalString="<answer/>";
            nonNullCount--;
        }
        synchronized(lockObj){
            try {
                while(doneCount<nonNullCount)
                    lockObj.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t.handleThread(this,ProcessType.AQL_RUN, aql1FinalString, aql2FinalString);
        }

    }


    @Override
    public void handleThread(Thread thred, ProcessType type, String finalString,String  finalString2) {
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
