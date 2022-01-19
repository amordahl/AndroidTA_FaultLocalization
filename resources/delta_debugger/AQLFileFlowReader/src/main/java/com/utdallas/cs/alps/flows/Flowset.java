package com.utdallas.cs.alps.flows;

import java.util.ArrayList;

public class Flowset {
    private String config1="";
    private String config2="";
    private String type;
    private String violation;

    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }

    private String apk="";
    private ArrayList<Flow> config1_flowList;
    private ArrayList<Flow> config2_flowList;

    public Flowset(){
        config1_flowList = new ArrayList<>();
        config2_flowList = new ArrayList<>();
    }
    public Flowset(String config1, String config2, String type){
        this.config1=config1;
        this.config2=config2;
        this.type= type;
        config1_flowList = new ArrayList<>();
        config2_flowList = new ArrayList<>();
    }

    public String getViolation(){return violation;}
    public void setViolation(String n){this.violation=n;}
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

    public ArrayList<Flow> getConfig1_FlowList() {
        return config1_flowList;
    }

    public void setConfig1_FlowList(ArrayList<Flow> flowList) {
        this.config1_flowList= flowList;
    }

    public ArrayList<Flow> getConfig2_FlowList() {
        return config2_flowList;
    }

    public void setConfig2_FlowList(ArrayList<Flow> flowList) {
        this.config2_flowList= flowList;
    }
}
