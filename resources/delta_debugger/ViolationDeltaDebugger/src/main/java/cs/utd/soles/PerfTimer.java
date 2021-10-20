package cs.utd.soles;

import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;

public class PerfTimer {


    boolean firstRotationRun=true;

    public PerfTimer(){
        goodCompileTimer = new StopWatch();
        goodAQLTimer = new StopWatch();
        rotationTimer = new StopWatch();
        setupTimer = new StopWatch();
        programTimer = new StopWatch();
        binaryTimer = new StopWatch();
        methodRedTimer = new StopWatch();
        dependencyGraphTimer = new StopWatch();
    }

    long startLineCount=0;
    long endLineCount=0;
    int changeNum=0;
    int currentRotation=0;

    ArrayList<CodeChange> codeChanges = new ArrayList<>();
    public long lastCurrentLines=0;


    public double totalOfBadCompileRunsBinary=0;
    public int totalBadCompileRunsBinary = 0;
    public double totalOfBadAQLRunsBinary = 0;
    public int totalBadAQLRunsBinary = 0;


    public double totalOfBadCompileRunsHDD=0;
    public int totalBadCompileRunsHDD = 0;
    public double totalOfBadAQLRunsHDD = 0;
    public int totalBadAQLRunsHDD = 0;

    public double totalOfGoodCompileRunsBinary=0;
    public int totalGoodCompileRunsBinary = 0;
    public double totalOfGoodAQLRunsBinary = 0;
    public int totalGoodAQLRunsBinary = 0;


    public double totalOfGoodCompileRunsHDD=0;
    public int totalGoodCompileRunsHDD = 0;
    public double totalOfGoodAQLRunsHDD = 0;
    public int totalGoodAQLRunsHDD = 0;

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
    private StopWatch dependencyGraphTimer;

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

    public   long getTotalAQLRuns(int caller) {
        if(caller==0)
            return totalGoodAQLRunsBinary;
        else {
            return totalGoodAQLRunsHDD;
        }
    }

    public   double getAverageOfGoodAQLRuns(int caller) {
        if(caller==0) {
            if (totalGoodAQLRunsBinary == 0)
                return 0;
            return ((double) totalOfGoodAQLRunsBinary) / totalGoodAQLRunsBinary;
        }else {
            if (totalGoodAQLRunsHDD == 0)
                return 0;
            return ((double) totalOfGoodAQLRunsHDD) / totalGoodAQLRunsHDD;
        }
    }
    public   double getAverageOfBadAQLRuns(int caller) {
        if(caller==0) {
            if (totalBadAQLRunsBinary == 0)
                return 0;
            return ((double) totalOfBadAQLRunsBinary) / totalBadAQLRunsBinary;
        }else{
            if (totalBadAQLRunsHDD == 0)
                return 0;
            return ((double) totalOfBadAQLRunsHDD) / totalBadAQLRunsHDD;
        }
    }

    public long getTotalCompileRuns(int caller) {
        if(caller==0) {
            return totalGoodCompileRunsBinary;
        }else{
            return totalGoodCompileRunsHDD;
        }
    }
    public long getTotalBadCompileRuns(int caller){
        if(caller==0) {
            return totalBadCompileRunsBinary;
        }else{
            return totalBadCompileRunsHDD;
        }
    }
    public long getTotalBadAqlRuns(int caller){
        if(caller==0) {
            return totalBadAQLRunsBinary;
        }
        else{
            return totalBadAQLRunsHDD;
        }
    }

    public double getAverageOfGoodCompileRuns(int caller) {
        if(caller==0) {
            if (totalGoodCompileRunsBinary == 0)
                return 0;
            return ((double) totalOfGoodCompileRunsBinary) / totalGoodCompileRunsBinary;
        }else{
            if (totalGoodCompileRunsHDD == 0)
                return 0;
            return ((double) totalOfGoodCompileRunsHDD) / totalGoodCompileRunsHDD;
        }
    }
    public double getAverageOfBadCompileRuns(int caller) {
        if(caller==0) {
            if (totalBadCompileRunsBinary == 0)
                return 0;
            return ((double) totalOfBadCompileRunsBinary) / totalBadCompileRunsBinary;
        }else{
            if (totalBadCompileRunsHDD == 0)
                return 0;
            return ((double) totalOfBadCompileRunsHDD) / totalBadCompileRunsHDD;
        }
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
        goodAQLTimer.start();

    }
    public void endOneAQLRun(boolean success, int caller){

        goodAQLTimer.stop();

        if(caller==0) {
            if (success) {
                totalOfGoodAQLRunsBinary += goodAQLTimer.getTime();
                totalGoodAQLRunsBinary++;

            } else {
                totalOfBadAQLRunsBinary += goodAQLTimer.getTime();
                totalBadAQLRunsBinary++;
            }
        }
        else{
            if (success) {
                totalOfGoodAQLRunsHDD += goodAQLTimer.getTime();
                totalGoodAQLRunsHDD++;

            } else {
                totalOfBadAQLRunsHDD += goodAQLTimer.getTime();
                totalBadAQLRunsHDD++;
            }
        }
        goodAQLTimer.reset();


    }
    public   void startOneCompileRun(){
        goodCompileTimer.start();
    }
    public   void endOneCompileRun(int caller){
        goodCompileTimer.stop();
        if(caller==0) {
            totalOfGoodCompileRunsBinary += goodCompileTimer.getTime();
            totalGoodCompileRunsBinary++;

        }else{
            totalOfGoodCompileRunsHDD += goodCompileTimer.getTime();
            totalGoodCompileRunsHDD++;
        }
        goodCompileTimer.reset();
    }
    public   void endOneFailedCompileRun(int caller){

        goodCompileTimer.stop();
        if(caller==0) {
            totalOfBadCompileRunsBinary += goodCompileTimer.getTime();
            totalBadCompileRunsBinary++;

        }else{
            totalOfBadCompileRunsHDD += goodCompileTimer.getTime();
            totalBadCompileRunsHDD++;
        }
        goodCompileTimer.reset();
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
    public void startDependencyGraphTime(){
        dependencyGraphTimer.start();
    }
    public void endDependencyGraphTime(){
        dependencyGraphTimer.stop();
    }
    public long getDependencyGraphTime(){
        return dependencyGraphTimer.getTime();
    }


    private long programStartTime=0;
    private long thisCompileRun=0;
    private long totalRotations=0;



    public   String getPercentagesBinary(){



        String returnString ="";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_AQL_Runs_Binary: "+((totalOfGoodAQLRunsBinary/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_Compile_Runs_Binary: "+((totalOfGoodCompileRunsBinary/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_AQL_Runs_Binary: "+((totalOfBadAQLRunsBinary/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_Compile_Runs_Binary: "+((totalOfBadCompileRunsBinary/getProgramRunTime())*100)+"\n";
        return returnString;
    }
    public   String getPercentagesHDD(){



        String returnString ="";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_AQL_Runs_HDD: "+((totalOfGoodAQLRunsHDD/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Good_Compile_Runs_HDD: "+((totalOfGoodCompileRunsHDD/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_AQL_Runs_HDD: "+((totalOfBadAQLRunsHDD/getProgramRunTime())*100)+"\n";
        returnString+="Percent_Of_Program_Time_Taken_By_Bad_Compile_Runs_HDD: "+((totalOfBadCompileRunsHDD/getProgramRunTime())*100)+"\n";
        return returnString;
    }



}
