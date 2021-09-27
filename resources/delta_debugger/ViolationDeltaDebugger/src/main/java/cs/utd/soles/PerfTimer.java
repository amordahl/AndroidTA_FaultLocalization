package cs.utd.soles;

import java.util.ArrayList;

public class PerfTimer {


      long startLineCount=0;
      long endLineCount=0;
      int changeNum=0;
      int currentRotation=0;

      ArrayList<CodeChange> codeChanges = new ArrayList<>();

      public long lastCurrentLines=0;
    public long totalOfBadCompileRuns=0;
    public int totalBadCompileRuns = 0;
    public double totalOfBadAQLRuns = 0;
    public int totalBadAQLRuns = 0;

    public void addCodeChange(long currentLines){
        double timeMade=(System.currentTimeMillis()-programStartTime)/1000.0;
        //check against the last size of the program
        long linesRemoved=lastCurrentLines-currentLines;
        //update current lines
        lastCurrentLines=currentLines;
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
            String percentChangedS= String.format("%.2f",percentChanged);
            returnLine+=x.timeMade+","+x.currentRotation+","+x.changeNumber+","+x.linesRemoved+","+percentChangedS+"\n";
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
        if(totalRotations==0)
            return 0;
        return ((double)totalOfRotations)/totalRotations;
    }

    public   long getTotalAQLRuns() {
        return totalGoodAQLRuns;
    }

    public   double getAverageOfGoodAQLRuns() {
        if(totalGoodAQLRuns==0)
            return 0;
        return ((double)totalOfGoodAQLRuns)/totalGoodAQLRuns;
    }
    public   double getAverageOfBadAQLRuns() {
        if(totalBadAQLRuns==0)
            return 0;
        return ((double)totalOfBadAQLRuns)/totalBadAQLRuns;
    }

    public   long getTotalCompileRuns() {
        return totalGoodCompileRuns;
    }

    public   double getAverageOfGoodCompileRuns() {
        if(totalGoodCompileRuns==0)
            return 0;
        return ((double)totalOfGoodCompileRuns)/totalGoodCompileRuns;
    }
    public   double getAverageOfBadCompileRuns() {
        if(totalBadCompileRuns==0)
            return 0;
        return ((double)totalOfBadCompileRuns)/totalBadCompileRuns;
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
    public   void endOneAQLRun(boolean success){
        if(success) {
            totalOfGoodAQLRuns += System.currentTimeMillis() - thisAQLRun;
            totalGoodAQLRuns++;

        }else{
            totalOfBadAQLRuns += System.currentTimeMillis() - thisAQLRun;
            totalBadAQLRuns++;
        }
    }
    public   void startOneCompileRun(){
        thisCompileRun=System.currentTimeMillis();
    }
    public   void endOneCompileRun(){

            totalOfGoodCompileRuns += System.currentTimeMillis() - thisCompileRun;
            totalBadCompileRuns++;

    }
    public   void endOneFailedCompileRun(){
        totalOfBadCompileRuns = System.currentTimeMillis() -thisCompileRun;
        totalGoodCompileRuns++;
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
    private   long totalOfGoodAQLRuns=0;
    private   long totalGoodAQLRuns=0;
    private   long totalOfGoodCompileRuns=0;
    private   long totalGoodCompileRuns=0;

    public   String getPercentages(){

        double totalRunTime = totalProgramTime;

        String returnString ="";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_AQL_Runs: "+((totalOfGoodAQLRuns/totalRunTime)*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_Compile_Runs: "+((totalOfGoodCompileRuns/totalRunTime)*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_AQL_Runs: "+((totalOfBadAQLRuns/totalRunTime)*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_Compile_Runs: "+((totalOfBadCompileRuns/totalRunTime)*100)+"\n";
        return returnString;
    }



}
