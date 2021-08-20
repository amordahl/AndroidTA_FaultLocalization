package cs.utd.soles;

public class LineObj {

    /*
    * class to represent a line of data
    * */

    String apk;
    String config1;
    String config2;
    String runtime;
    String violation_type;
    String avgRotation;
    String totalRotation;
    String avgAQL;
    String totalAQL;
    String avgCompile;
    String totalCompile;
    String totalProposed;
    String totalComplete;
    String numCandidate;
    String percentAQL;
    String percentCompile;
    String compFailed;
    String violation_or_not;

    @Override
    public String toString() {
        return "LineObj{" +
                "apk='" + apk + '\'' +
                ", config1='" + config1 + '\'' +
                ", config2='" + config2 + '\'' +
                ", runtime='" + runtime + '\'' +
                ", violation_type='" + violation_type + '\'' +
                ", violation_or_not='" + violation_or_not + '\'' +
                ", avgRotation='" + avgRotation + '\'' +
                ", totalRotation='" + totalRotation + '\'' +
                ", avgAQL='" + avgAQL + '\'' +
                ", totalAQL='" + totalAQL + '\'' +
                ", avgCompile='" + avgCompile + '\'' +
                ", totalCompile='" + totalCompile + '\'' +
                ", totalProposed='" + totalProposed + '\'' +
                ", totalComplete='" + totalComplete + '\'' +
                ", numCandidate='" + numCandidate + '\'' +
                ", percentAQL='" + percentAQL + '\'' +
                ", percentCompile='" + percentCompile + '\'' +
                ", compFailed='" + compFailed + '\'' +
                '}';
    }

    public LineObj(String apk, String config1, String config2, String runtime, String violation_type, String violation_or_not, String avgRotation, String totalRotation, String avgAQL, String totalAQL, String avgCompile, String totalCompile, String totalProposed, String totalComplete, String numCandidate, String percentAQL, String percentCompile) {
        this.apk = apk;
        this.config1 = config1;
        this.config2 = config2;
        this.runtime = runtime;
        this.violation_type = violation_type;
        this.violation_or_not = violation_or_not;
        this.avgRotation = avgRotation;
        this.totalRotation = totalRotation;
        this.avgAQL = avgAQL;
        this.totalAQL = totalAQL;
        this.avgCompile = avgCompile;
        this.totalCompile = totalCompile;
        this.totalProposed = totalProposed;
        this.totalComplete = totalComplete;
        this.numCandidate = numCandidate;
        this.percentAQL = percentAQL;
        this.percentCompile = percentCompile;
    }

    public LineObj(){

    }

}
