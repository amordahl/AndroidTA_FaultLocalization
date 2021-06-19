package cs.utd.soles;

import java.util.ArrayList;

public class PerfTimer {


      long startLineCount=0;
      long endLineCount=0;
      int changeNum=0;
      int currentRotation=0;

      ArrayList<CodeChange> codeChanges = new ArrayList<>();

    public void addCodeChange(long currentLines){
        double timeMade=(System.currentTimeMillis()-programStartTime)/1000.0;
        long linesRemoved=startLineCount-currentLines;
        codeChanges.add(new CodeChange(timeMade, linesRemoved, currentRotation, changeNum));
    }



    private class CodeChange{

        //the time (measured from the start of the programs execution) of the change
        private double timeMade;
        //how many lines of code were removed
        private long linesRemoved;
        //what rotation are we in?
        private int currentRotation;
        //What iteration of the ast is this
        private int changeNumber;

        public CodeChange(double timeMade, long linesRemoved, int currentRot, int changeNumber){
            this.timeMade = timeMade;
            this.linesRemoved=linesRemoved;
            this.currentRotation=currentRot;
            this.changeNumber=changeNumber;
        }

    }


    public String writeCodeChanges(){
        String returnLine="\nSTARTCODECHANGES: \ntimeMade,rotation,changeNumber,linesRemoved,%ofProgramRemoved\n";
        for(CodeChange x: codeChanges){

            double percentChanged=(x.linesRemoved/((double)startLineCount))*100;
            returnLine+=x.timeMade+","+x.currentRotation+","+x.changeNumber+","+x.linesRemoved+","+percentChanged+"\n";
        }
        return returnLine;
    }

    public   long getTotalRotations() {
        return totalRotations;
    }

    public void addChangeNum(){
        changeNum++;
    }

    public   double getAverageOfRotations() {
        return ((double)totalOfRotations)/totalRotations;
    }

    public   long getTotalAQLRuns() {
        return totalAQLRuns;
    }

    public   double getAverageOfAQLRuns() {
        return ((double)totalOfAQLRuns)/totalAQLRuns;
    }

    public   long getTotalCompileRuns() {
        return totalCompileRuns;
    }

    public   double getAverageOfCompileRuns() {
        return ((double)totalOfCompileRuns)/totalCompileRuns;
    }

    public   void startOneRotation(){
        thisRotation=System.currentTimeMillis();
        currentRotation++;
    }

    public   void endOneRotation(){
        totalOfRotations += System.currentTimeMillis() - thisRotation;
        totalRotations++;
    }
    public   void startOneAQLRun(){
        thisAQLRun=System.currentTimeMillis();
    }
    public   void endOneAQLRun(){
        totalOfAQLRuns += System.currentTimeMillis() - thisAQLRun;
        totalAQLRuns++;
    }
    public   void startOneCompileRun(){
        thisCompileRun=System.currentTimeMillis();
    }
    public   void endOneCompileRun(){
        totalOfCompileRuns += System.currentTimeMillis() - thisCompileRun;
        totalCompileRuns++;
    }
    public   void endOneFailedCompileRun(){
        totalCompileRuns++;
    }
    public   void startProgramRunTime(){
        thisProgramRuntime=System.currentTimeMillis();
        programStartTime=thisProgramRuntime;
    }
    public   long getProgramRunTime(){
        totalProgramTime=System.currentTimeMillis()-thisProgramRuntime;
        return totalProgramTime;
    }

    private long programStartTime=0;
    private   long thisProgramRuntime=0;
    private   long thisRotation=0;
    private   long thisAQLRun=0;
    private   long thisCompileRun=0;
    private   long totalOfRotations=0;
    private   long totalRotations=0;
    private   long totalProgramTime=0;
    private   long totalOfAQLRuns=0;
    private   long totalAQLRuns=0;
    private   long totalOfCompileRuns=0;
    private   long totalCompileRuns=0;

    public   String getPercentages(){

        double totalRunTime = totalProgramTime;

        String returnString ="";
        returnString+="Percent_Of_Program_Time_Taken_By_AQL_Runs: "+((totalOfAQLRuns/totalRunTime)*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Compile_Runs: "+((totalOfCompileRuns/totalRunTime)*100)+"\n";
        return returnString;
    }



}
