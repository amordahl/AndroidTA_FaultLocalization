package com.utdallas.cs.alps.flows;

public class ClassifiedFlow extends Flow{
    private boolean classification;
    public ClassifiedFlow(){
        super();
    }
    public void setClassification(boolean n){
        this.classification=n;
    }
    public boolean getClassification(){
        return classification;
    }
}
