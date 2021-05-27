package com.utdallas.cs.alps.flows;

import java.util.ArrayList;

public class Violation {
    private String config1="";
    private String config2="";
    private String type;

    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }

    private String apk="";
    private ArrayList<Flow> flowList;

    public Violation(){
        flowList = new ArrayList<>();
    }
    public Violation(String config1, String config2, String type){
        this.config1=config1;
        this.config2=config2;
        this.type= type;
        flowList = new ArrayList<>();
    }

    public String getConfig1() {
        return config1;
    }

    public void setConfig1(String config1) {
        this.config1 = config1;
    }

    public String getConfig2() {
        return config2;
    }

    public void setConfig2(String config2) {
        this.config2 = config2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Flow> getFlowList() {
        return flowList;
    }

    public void setFlowList(ArrayList<Flow> flowList) {
        this.flowList = flowList;
    }
}
