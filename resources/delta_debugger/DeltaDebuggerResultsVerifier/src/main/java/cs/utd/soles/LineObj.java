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
    String avgAQL;
    String totalAQL;
    String avgCompile;
    String totalCompile;
    String percentAQL;
    String percentCompile;
    String numCandidate;
    String startLines;
    String endLines;
    String percentLines;

    public LineObj(String apk, String config1, String config2, String runtime, String violationType, String violation_or_not, String avgRotation, String totalRotation, String avgAQL, String totalAQL, String avgCompile, String totalCompile, String percentAQL, String percentCompile, String numCandidate, String startLines, String endLines, String percentLines) {
        this.apk = apk;
        this.config1 = config1;
        this.config2 = config2;
        this.runtime = runtime;
        this.violationType = violationType;
        this.violation_or_not = violation_or_not;
        this.avgRotation = avgRotation;
        this.totalRotation = totalRotation;
        this.avgAQL = avgAQL;
        this.totalAQL = totalAQL;
        this.avgCompile = avgCompile;
        this.totalCompile = totalCompile;
        this.percentAQL = percentAQL;
        this.percentCompile = percentCompile;
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
                ", avgAQL='" + avgAQL + '\'' +
                ", totalAQL='" + totalAQL + '\'' +
                ", avgCompile='" + avgCompile + '\'' +
                ", totalCompile='" + totalCompile + '\'' +
                ", percentAQL='" + percentAQL + '\'' +
                ", percentCompile='" + percentCompile + '\'' +
                ", numCandidate='" + numCandidate + '\'' +
                ", startLines='" + startLines + '\'' +
                ", endLines='" + endLines + '\'' +
                ", percentLines='" + percentLines + '\'' +
                '}';
    }
}
