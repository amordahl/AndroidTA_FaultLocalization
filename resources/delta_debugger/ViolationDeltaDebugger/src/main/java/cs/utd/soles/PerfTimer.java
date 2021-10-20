package cs.utd.soles;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;

public class PerfTimer {


    boolean firstAQLRun=true;
    boolean firstCompileRun=true;
    boolean firstRotationRun=true;

    public PerfTimer(){
        goodCompileTimer = new StopWatch();
        goodAQLTimer = new StopWatch();
        rotationTimer = new StopWatch();
        setupTimer = new StopWatch();
        programTimer = new StopWatch();
        binaryTimer = new StopWatch();
        methodRedTimer = new StopWatch();
    }

    long startLineCount=0;
    long endLineCount=0;
    int changeNum=0;
    int currentRotation=0;

    ArrayList<CodeChange> codeChanges = new ArrayList<>();
    public long lastCurrentLines=0;
    public double totalOfBadCompileRuns=0;
    public int totalBadCompileRuns = 0;
    public double totalOfBadAQLRuns = 0;
    public int totalBadAQLRuns = 0;

    public void addCodeChange(long currentLines){
        double timeMade=System.currentTimeMillis()-programTimer.getStartTime();
        //check against the last size of the program
        long linesRemoved=lastCurrentLines-currentLines;
        //update current lines
        lastCurrentLines=currentLines;
        codeChanges.add(new CodeChange(timeMade, linesRemoved, currentRotation, changeNum));
    }

    private StopWatch goodCompileTimer;
    //private StopWatch badCompileTimer;
    private StopWatch goodAQLTimer;
    //private StopWatch badAQLTimer;
    private StopWatch rotationTimer;

    private StopWatch setupTimer;
    private StopWatch programTimer;
    private StopWatch binaryTimer;
    private StopWatch methodRedTimer;

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

    public long getTotalRotations() {
        return totalRotations;
    }

    public void addChangeNum(){
        changeNum++;
    }

    public double getAverageOfRotations() {
        return rotationTimer.getTime()/((double)totalRotations);
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
    public long getTotalBadCompileRuns(){
        return totalBadCompileRuns;
    }
    public long getTotalBadAqlRuns(){
        return totalBadAQLRuns;
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

    public void startOneRotation(){
        if(firstRotationRun){
            firstRotationRun=false;
            rotationTimer.start();
        }
        else {
            rotationTimer.resume();

        }
        currentRotation++;
    }

    public void endOneRotation(){
        rotationTimer.suspend();
        totalRotations++;
    }

    public void endRotationTimer(){
        rotationTimer.stop();
    }

    public void startOneAQLRun(){
        if(firstAQLRun){
            firstAQLRun=false;
            goodAQLTimer.start();
        }else {
            goodAQLTimer.resume();
        }
        goodAQLTimer.split();
    }
    public void endOneAQLRun(boolean success){

        goodAQLTimer.suspend();
        if(success) {
            totalOfGoodAQLRuns+=goodAQLTimer.getSplitTime();
            totalGoodAQLRuns++;

        }else{
            totalOfBadAQLRuns+=goodAQLTimer.getSplitTime();
            totalBadAQLRuns++;
        }
        goodAQLTimer.unsplit();

    }
    public   void startOneCompileRun(){
        if(firstCompileRun) {
            firstCompileRun=false;
            goodCompileTimer.start();

        }else{
            goodCompileTimer.resume();
        }
        goodCompileTimer.split();
    }
    public   void endOneCompileRun(){
        goodCompileTimer.suspend();
        totalOfGoodCompileRuns+=goodCompileTimer.getSplitTime();
        totalGoodCompileRuns++;
        goodCompileTimer.unsplit();

    }
    public   void endOneFailedCompileRun(){

        goodCompileTimer.suspend();
        totalOfBadCompileRuns += goodCompileTimer.getSplitTime();
        totalBadCompileRuns++;
        goodCompileTimer.unsplit();
    }
    public void startProgramRunTime(){
        programTimer.start();
    }
    public void endOneProgramTime(){
        programTimer.stop();
    }
    public long getProgramRunTime(){
        return programTimer.getTime();
    }
    public void startSetupTime(){
        setupTimer.start();
    }
    public void endOneSetupTime(){
        setupTimer.stop();
    }
    public long getSetupTime(){
        return setupTimer.getTime();
    }
    public void startBinaryTime(){
        binaryTimer.start();
    }
    public void endBinaryTime(){
        binaryTimer.stop();
    }
    public long getBinaryTime(){
        return binaryTimer.getTime();
    }
    public void startMethodRedTime(){
        methodRedTimer.start();
    }
    public void endMethodRedTime(){
        methodRedTimer.stop();
    }
    public long getMethodRedTime(){
        return methodRedTimer.getTime();
    }



    private long programStartTime=0;
    private long thisCompileRun=0;
    private long totalRotations=0;
    public  long totalProgramTime=0;
    private long totalOfGoodAQLRuns=0;
    private long totalGoodAQLRuns=0;
    private long totalOfGoodCompileRuns=0;
    private long totalGoodCompileRuns=0;


    public   String getPercentages(){

        double totalRunTime = totalProgramTime;

        String returnString ="";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_AQL_Runs: "+((totalOfGoodAQLRuns/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_Compile_Runs: "+((totalOfGoodCompileRuns/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_AQL_Runs: "+((totalOfBadAQLRuns/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_Compile_Runs: "+((totalOfBadCompileRuns/getProgramRunTime())*100)+"\n";
        return returnString;
    }



}
