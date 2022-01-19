package cs.utd.soles.threads;

import cs.utd.soles.threads.ThreadHandler;

import java.io.*;

public class ProcessThread extends Thread{




    boolean threadDone=false;
    Process thisProc;
    String finalString;
    ThreadHandler handler;
    ThreadHandler.ProcessType type;
    final Object lockObj;
    int doneCount;
    private long startTime;
    private long timeOutLong;
    private boolean doWriteProcess=false;
    int caller;
    public ProcessThread(Process aqlProcess, ThreadHandler t, ThreadHandler.ProcessType type, long timeOutLong, int caller){
        thisProc=aqlProcess;
        handler=t;
        finalString="";
        this.type=type;
        lockObj= new Object();
        doneCount=0;
        this.timeOutLong=timeOutLong;
        this.caller=caller;

    }

    public int getCaller(){
        return caller;
    }

    public void run(){
        startTime=System.currentTimeMillis();
        ProcessIThread ithread = new ProcessIThread(new BufferedReader(new InputStreamReader(thisProc.getInputStream())));
        ProcessEThread ethread = new ProcessEThread(new BufferedReader(new InputStreamReader(thisProc.getErrorStream())));

        ithread.start();
        ethread.start();
        try {
            //in synchronized block, wait for thing to be done
            synchronized(lockObj){

                while (doneCount < 2) {
                    lockObj.wait();
                }
                finalString=ithread.doneString+" "+ethread.doneString;
                if(type == ThreadHandler.ProcessType.AQL_PROCESS1 || type == ThreadHandler.ProcessType.AQL_PROCESS2){
                    finalString=ithread.doneString+"\n\nError Messages from AQL:\n"+ethread.doneString;
                }
                if(type == ThreadHandler.ProcessType.CALLGRAPH){
                    finalString=ithread.doneString;
                }
                //kill this process
                thisProc.destroyForcibly();
                handler.handleThread(this,type,finalString,null);
                threadDone=true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ProcessIThread extends Thread{

        BufferedReader reader;
        String doneString="";
        ProcessIThread(BufferedReader r){
            reader=r;
        }

        public void run(){
            try {

                while((System.currentTimeMillis()<startTime+timeOutLong)&&thisProc.isAlive()) {

                    while (reader.ready()) {
                        String s="";
                        s = reader.readLine();
                        doneString += s + "\n";
                    }
                }
                if(System.currentTimeMillis()>startTime+timeOutLong) {
                    doWriteProcess = true;
                }
            }catch(Exception e){e.printStackTrace();}

            synchronized (lockObj) {
                doneCount++;
                lockObj.notify();
            }
        }
    }
    private class ProcessEThread extends Thread{

        BufferedReader reader;
        String doneString="";
        ProcessEThread(BufferedReader r){
            reader=r;
        }
        public void run(){
            try {

                while((System.currentTimeMillis()<startTime+timeOutLong)&&thisProc.isAlive()) {

                    while (reader.ready()) {
                        String s="";
                        s = reader.readLine();
                        doneString += s + "\n";
                    }
                }
                if(System.currentTimeMillis()>startTime+timeOutLong) {
                    doWriteProcess = true;
                }
            }catch(Exception e){e.printStackTrace();}

            synchronized (lockObj) {
                doneCount++;
                lockObj.notify();
            }
        }
    }
}
