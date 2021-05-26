package cs.utd.soles;

public class PerfTimer {


    static int totalChangesCount=0;
    static int proposedChangesCount=0;
    public static void addToTotalChanges(int x){totalChangesCount+=x;}
    public static void addToProposedChanges(int x){proposedChangesCount+=x;}

    public static long getTotalOfRotations() {
        return totalOfRotations;
    }

    public static long getTotalRotations() {
        return totalRotations;
    }

    public static double getAverageOfRotations() {
        return ((double)totalOfRotations)/totalRotations;
    }


    public static long getTotalOfASTChanges() {
        return totalOfASTChanges;
    }

    public static long getTotalASTChanges() {
        return totalASTChanges;
    }

    public static double getAverageOfASTChanges() {
        return ((double)totalOfASTChanges)/totalASTChanges;
    }

    public static long getTotalOfAQLRuns() {
        return totalOfAQLRuns;
    }

    public static long getTotalAQLRuns() {
        return totalAQLRuns;
    }

    public static double getAverageOfAQLRuns() {
        return ((double)totalOfAQLRuns)/totalAQLRuns;
    }

    public static long getTotalOfCompileRuns() {
        return totalOfCompileRuns;
    }

    public static long getTotalCompileRuns() {
        return totalCompileRuns;
    }

    public static double getAverageOfCompileRuns() {
        return ((double)totalOfCompileRuns)/totalCompileRuns;
    }


    public static void startOneRotation(){
        thisRotation=System.currentTimeMillis();
    }
    public static void endOneRotation(){
        totalOfRotations += System.currentTimeMillis() - thisRotation;
        totalRotations++;
    }
    public static void startOneAQLRun(){
        thisAQLRun=System.currentTimeMillis();
    }
    public static void endOneAQLRun(){
        totalOfAQLRuns += System.currentTimeMillis() - thisAQLRun;
        totalAQLRuns++;
    }
    public static void startOneCompileRun(){
        thisCompileRun=System.currentTimeMillis();
    }
    public static void endOneCompileRun(){
        totalOfCompileRuns += System.currentTimeMillis() - thisCompileRun;
        totalCompileRuns++;
    }
    public static void endOneFailedCompileRun(){
        totalCompileRuns++;
    }
    public static void startOneASTChange(){
        thisCompileRun=System.currentTimeMillis();
    }
    public static void endOneASTChange(){
        totalOfASTChanges += System.currentTimeMillis() - thisASTChange;
        totalASTChanges++;
    }

    public static void startProgramRunTime(){
        thisProgramRuntime=System.currentTimeMillis();
    }
    public static long getProgramRunTime(){
        totalProgramTime=System.currentTimeMillis()-thisProgramRuntime;
        return totalProgramTime;
    }

    private static long thisProgramRuntime=0;

    private static long thisRotation=0;

    private static long thisAQLRun=0;

    private static long thisASTChange=0;
    private static long thisCompileRun=0;


    private static long totalOfRotations=0;
    private static long totalRotations=0;

    private static long totalProgramTime=0;

    private static long totalOfASTChanges=0;
    private static long totalASTChanges=0;

    private static long totalOfAQLRuns=0;
    private static long totalAQLRuns=0;

    private static long totalOfCompileRuns=0;
    private static long totalCompileRuns=0;

    public static String getPercentages(){

        double totalRunTime = totalProgramTime;

        String returnString ="";

        returnString+="Percent Of Program Time Taken By AST Changes: "+((totalOfASTChanges/totalRunTime)*100)+"\n";
        returnString+="Percent Of Program Time Taken By AQL Runs: "+((totalOfAQLRuns/totalRunTime)*100)+"\n";
        returnString+="Percent Of Program Time Taken By Compile Runs: "+((totalOfCompileRuns/totalRunTime)*100)+"\n";
        return returnString;
    }



}
