package cs.utd.soles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ProcessThread extends Thread{

    boolean threadDone=false;
    Process thisProc;
    String finalString;
    ThreadHandler handler;
    TesterUtil.ProcessType type;
    public ProcessThread(Process aqlProcess, ThreadHandler t, TesterUtil.ProcessType type){
        thisProc=aqlProcess;
        handler=t;
        finalString="";
        this.type=type;

    }

    public void run(){

        ProcessIThread ithread = new ProcessIThread(new BufferedReader(new InputStreamReader(thisProc.getInputStream())), thisProc);
        ProcessEThread ethread = new ProcessEThread(new BufferedReader(new InputStreamReader(thisProc.getErrorStream())), thisProc);

        ithread.start();
        ethread.start();
        try {
            thisProc.waitFor(5, TimeUnit.MINUTES);
            thisProc.destroy();
            while(!ithread.isDone && !ethread.isDone){}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        finalString=ithread.doneString+" "+ethread.doneString;
        handler.handleThread(type,finalString);
        threadDone=true;
    }

    private class ProcessIThread extends Thread{

        boolean isDone=false;
        BufferedReader reader;
        String doneString="";
        Process p;
        ProcessIThread(BufferedReader r, final Process p){
            reader=r;
            this.p=p;
        }

        public void run(){
            try {

                while(p.isAlive()) {
                    String s;
                    while ((s = reader.readLine()) != null) {

                        doneString += s + "\n";
                        //System.out.println(doneString);
                    }
                }
            }catch(Exception e){e.printStackTrace();}

            isDone=true;
        }
    }
    private class ProcessEThread extends Thread{

        boolean isDone=false;
        BufferedReader reader;
        String doneString="";
        Process p;
        ProcessEThread(BufferedReader r, final Process p){
            reader=r;
            this.p=p;
        }
        public void run(){
            try {
                while(p.isAlive()) {
                    String s;
                    while ((s = reader.readLine()) != null) {

                        doneString += s + "\n";
                        //System.out.println(doneString);
                    }
                }
            }catch(Exception e){e.printStackTrace();}

            isDone=true;
        }
    }
}
