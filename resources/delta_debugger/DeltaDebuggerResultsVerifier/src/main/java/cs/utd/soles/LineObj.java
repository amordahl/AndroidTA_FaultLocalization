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
    String avgRotation;
    String totalRotation;
    String avgGoodAQL;
    String totalGoodAQL;
    String avgGoodCompile;
    String totalGoodCompile;
    String avgBadAQL;
    String totalBadAQL;
    String avgBadCompile;
    String totalBadCompile;
    String percentBinary;
    String percentGoodAQL;
    String percentGoodCompile;
    String percentBadAQL;
    String percentBadCompile;
    String numCandidate;
    String startLines;
    String endLines;
    String percentLines;

    public LineObj(String apk, String config1, String config2, String runtime, String violationType, String violation_or_not, String avgRotation, String totalRotation, String avgGoodAQL, String totalGoodAQL, String avgGoodCompile, String totalGoodCompile, String avgBadAQL, String totalBadAQL, String avgBadCompile, String totalBadCompile, String percentBinary, String percentGoodAQL, String percentGoodCompile, String percentBadAQL, String percentBadCompile, String numCandidate, String startLines, String endLines, String percentLines) {
        this.apk = apk;
        this.config1 = config1;
        this.config2 = config2;
        this.runtime = runtime;
        this.violationType = violationType;
        this.violation_or_not = violation_or_not;
        this.avgRotation = avgRotation;
        this.totalRotation = totalRotation;
        this.avgGoodAQL = avgGoodAQL;
        this.totalGoodAQL = totalGoodAQL;
        this.avgGoodCompile = avgGoodCompile;
        this.totalGoodCompile = totalGoodCompile;
        this.avgBadAQL = avgBadAQL;
        this.totalBadAQL = totalBadAQL;
        this.avgBadCompile = avgBadCompile;
        this.totalBadCompile = totalBadCompile;
        this.percentBinary = percentBinary;
        this.percentGoodAQL = percentGoodAQL;
        this.percentGoodCompile = percentGoodCompile;
        this.percentBadAQL = percentBadAQL;
        this.percentBadCompile = percentBadCompile;
        this.numCandidate = numCandidate;
        this.startLines = startLines;
        this.endLines = endLines;
        this.percentLines = percentLines;
    }

    @Override
    public String toString() {
        return "LineObj{" +
                "apk='" + apk + '\'' +
                ", config1='" + config1 + '\'' +
                ", config2='" + config2 + '\'' +
                ", runtime='" + runtime + '\'' +
                ", violationType='" + violationType + '\'' +
                ", violation_or_not='" + violation_or_not + '\'' +
                ", avgRotation='" + avgRotation + '\'' +
                ", totalRotation='" + totalRotation + '\'' +
                ", avgGoodAQL='" + avgGoodAQL + '\'' +
                ", totalGoodAQL='" + totalGoodAQL + '\'' +
                ", avgGoodCompile='" + avgGoodCompile + '\'' +
                ", totalGoodCompile='" + totalGoodCompile + '\'' +
                ", avgBadAQL='" + avgBadAQL + '\'' +
                ", totalBadAQL='" + totalBadAQL + '\'' +
                ", avgBadCompile='" + avgBadCompile + '\'' +
                ", totalBadCompile='" + totalBadCompile + '\'' +
                ", percentBinary='" + percentBinary + '\'' +
                ", percentGoodAQL='" + percentGoodAQL + '\'' +
                ", percentGoodCompile='" + percentGoodCompile + '\'' +
                ", percentBadAQL='" + percentBadAQL + '\'' +
                ", percentBadCompile='" + percentBadCompile + '\'' +
                ", numCandidate='" + numCandidate + '\'' +
                ", startLines='" + startLines + '\'' +
                ", endLines='" + endLines + '\'' +
                ", percentLines='" + percentLines + '\'' +
                '}';
    }
}
