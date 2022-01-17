package cs.utd.soles;

public class CodeChange {
    double timeMade;
    int rot;
    int changenum;
    int linesRemoved;
    double percentProgram;

    @Override
    public String toString() {
        return "CodeChange{" +
                "timeMade=" + timeMade +
                ", rot=" + rot +
                ", changenum=" + changenum +
                ", linesRemoved=" + linesRemoved +
                ", percentProgram=" + percentProgram +
                '}'+"\n";
    }

    public CodeChange(double timeMade, int rot, int changeNum, int linesRemoved, double percentProgram){
        this.timeMade=timeMade;
        this.rot=rot;
        this.changenum=changeNum;
        this.linesRemoved=linesRemoved;
        this.percentProgram=percentProgram;
    }
}
