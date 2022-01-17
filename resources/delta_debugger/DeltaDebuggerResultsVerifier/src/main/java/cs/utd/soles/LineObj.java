package cs.utd.soles;

public class LineObj {

    /*
    * class to represent a line of data
    * */

    String apk;
    String config1;
    String config2;
    String runtime;
    String violationType;
    String violation_or_not;
    String setupTime;
    String binaryTime;
    String dependencyGraphTime;
    String avgGoodAQLBinary;
    String totalGoodAQLBinary;
    String avgGoodCompileBinary;
    String totalGoodCompileBinary;
    String avgBadAQLBinary;
    String totalBadAQLBinary;
    String avgBadCompileBinary;
    String totalBadCompileBinary;
    String percentBinary;
    String percentGoodAQLBinary;
    String percentGoodCompileBinary;
    String percentBadAQLBinary;
    String percentBadCompileBinary;

    String avgRotation;
    String totalRotation;
    String avgGoodAQLHDD;
    String totalGoodAQLHDD;
    String avgGoodCompileHDD;
    String totalGoodCompileHDD;
    String avgBadAQLHDD;
    String totalBadAQLHDD;
    String avgBadCompileHDD;
    String totalBadCompileHDD;
    String percentGoodAQLHDD;
    String percentGoodCompileHDD;
    String percentBadAQLHDD;
    String percentBadCompileHDD;

    String numCandidate;
    String startLines;
    String endLines;
    String percentLines;

    public LineObj(String apk, String config1, String config2, String runtime, String violationType, String violation_or_not, String setupTime, String binaryTime, String dependencyGraphTime, String avgGoodAQLBinary, String totalGoodAQLBinary, String avgGoodCompileBinary, String totalGoodCompileBinary, String avgBadAQLBinary, String totalBadAQLBinary, String avgBadCompileBinary, String totalBadCompileBinary, String percentBinary, String percentGoodAQLBinary, String percentGoodCompileBinary, String percentBadAQLBinary, String percentBadCompileBinary, String avgRotation, String totalRotation, String avgGoodAQLHDD, String totalGoodAQLHDD, String avgGoodCompileHDD, String totalGoodCompileHDD, String avgBadAQLHDD, String totalBadAQLHDD, String avgBadCompileHDD, String totalBadCompileHDD, String percentGoodAQLHDD, String percentGoodCompileHDD, String percentBadAQLHDD, String percentBadCompileHDD, String numCandidate, String startLines, String endLines, String percentLines) {
        this.apk = apk;
        this.config1 = config1;
        this.config2 = config2;
        this.runtime = runtime;
        this.violationType = violationType;
        this.violation_or_not = violation_or_not;
        this.setupTime = setupTime;
        this.binaryTime = binaryTime;
        this.dependencyGraphTime = dependencyGraphTime;
        this.avgGoodAQLBinary = avgGoodAQLBinary;
        this.totalGoodAQLBinary = totalGoodAQLBinary;
        this.avgGoodCompileBinary = avgGoodCompileBinary;
        this.totalGoodCompileBinary = totalGoodCompileBinary;
        this.avgBadAQLBinary = avgBadAQLBinary;
        this.totalBadAQLBinary = totalBadAQLBinary;
        this.avgBadCompileBinary = avgBadCompileBinary;
        this.totalBadCompileBinary = totalBadCompileBinary;
        this.percentBinary = percentBinary;
        this.percentGoodAQLBinary = percentGoodAQLBinary;
        this.percentGoodCompileBinary = percentGoodCompileBinary;
        this.percentBadAQLBinary = percentBadAQLBinary;
        this.percentBadCompileBinary = percentBadCompileBinary;
        this.avgRotation = avgRotation;
        this.totalRotation = totalRotation;
        this.avgGoodAQLHDD = avgGoodAQLHDD;
        this.totalGoodAQLHDD = totalGoodAQLHDD;
        this.avgGoodCompileHDD = avgGoodCompileHDD;
        this.totalGoodCompileHDD = totalGoodCompileHDD;
        this.avgBadAQLHDD = avgBadAQLHDD;
        this.totalBadAQLHDD = totalBadAQLHDD;
        this.avgBadCompileHDD = avgBadCompileHDD;
        this.totalBadCompileHDD = totalBadCompileHDD;
        this.percentGoodAQLHDD = percentGoodAQLHDD;
        this.percentGoodCompileHDD = percentGoodCompileHDD;
        this.percentBadAQLHDD = percentBadAQLHDD;
        this.percentBadCompileHDD = percentBadCompileHDD;
        this.numCandidate = numCandidate;
        this.startLines = startLines;
        this.endLines = endLines;
        this.percentLines = percentLines;
    }


}
