package cs.utd.soles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

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
    public ProcessThread(Process aqlProcess, ThreadHandler t, ThreadHandler.ProcessType type, long timeOutLong){
        thisProc=aqlProcess;
        handler=t;
        finalString="";
        this.type=type;
        lockObj= new Object();
        doneCount=0;
        this.timeOutLong=timeOutLong;

    }

    public void run(){
        startTime=System.currentTimeMillis();
        ProcessIThread ithread = new ProcessIThread(new BufferedReader(new InputStreamReader(thisProc.getInputStream())), thisProc, lockObj);
        ProcessEThread ethread = new ProcessEThread(new BufferedReader(new InputStreamReader(thisProc.getErrorStream())), thisProc, lockObj);

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
                //kill this process
                thisProc.destroyForcibly();
                if(doWriteProcess&&Runner.LOG_MESSAGES&& this.type == ThreadHandler.ProcessType.CREATE_APK_PROCESS){
                    try {
                        String fp = "debugger/tempfiles/gradleproc/" + Runner.thisRunName + System.currentTimeMillis() + "_log.txt";
                        File f = new File(fp);
                        f.mkdirs();
                        if(f.exists())
                            f.delete();
                        f.createNewFile();
                        FileWriter fw = new FileWriter(f);
                        fw.write(finalString);
                        fw.flush();
                        fw.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                handler.handleThread(type,finalString,null);
                threadDone=true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private class ProcessIThread extends Thread{

        BufferedReader reader;
        String doneString="";
        Process p;
        final Object lockObj;
        ProcessIThread(BufferedReader r, final Process p, final Object lockObj){
            reader=r;
            this.p=p;
            this.lockObj=lockObj;
        }

        public void run(){
            try {

                while(p.isAlive()&&(System.currentTimeMillis()<startTime+timeOutLong)) {
                    String s;
                    while ((s = reader.readLine()) != null) {

                        doneString += s + "\n";
                        //System.out.println(doneString);
                    }
                }
                if(System.currentTimeMillis()>startTime+timeOutLong)
                    doWriteProcess=true;
                reader.close();
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
        Process p;
        final Object lockObj;
        ProcessEThread(BufferedReader r, final Process p,final  Object lockObj){
            reader=r;
            this.p=p;
            this.lockObj=lockObj;
        }
        public void run(){
            try {
                while(p.isAlive()&&(System.currentTimeMillis()<startTime+timeOutLong)) {
                    String s;
                    while ((s = reader.readLine()) != null) {

                        doneString += s + "\n";
                        //System.out.println(doneString);
                    }
                }
                if(System.currentTimeMillis()>startTime+timeOutLong)
                    doWriteProcess=true;
                reader.close();
            }catch(Exception e){e.printStackTrace();}
            synchronized (lockObj) {
                doneCount++;
                lockObj.notify();
            }
        }
    }
}
