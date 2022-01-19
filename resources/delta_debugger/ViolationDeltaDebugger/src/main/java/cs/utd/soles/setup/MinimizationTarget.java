package cs.utd.soles.setup;

import java.io.File;

public class MinimizationTarget{
    public String getProjectRootPath() {
        return projectRootPath;
    }

    public String getProjectGradlewPath() {
        return projectGradlewPath;
    }

    public String getProjectSrcPath() {
        return projectSrcPath;
    }

    public String getProjectAPKPath() {
        return projectAPKPath;
    }

    public String getProjectClassFiles() {
        return projectClassFiles;
    }

    public String getProjectJavaPath() {
        return projectJavaPath;
    }

    public File getDotFile() {
        return dotFile;
    }

    String projectRootPath;
    String projectGradlewPath;
    String projectSrcPath;
    String projectJavaPath;
    String projectAPKPath;
    String projectClassFiles;
    File dotFile;

    public MinimizationTarget(String actualAPK,String pathFile){
        createProjectPathVars(actualAPK,pathFile);
    }


    private void createProjectPathVars(String APKName, String pathFile) {

        projectRootPath=pathFile;
        projectGradlewPath=pathFile+"/gradlew";
        File f= new File(projectGradlewPath);
        f.setExecutable(true);
        projectAPKPath=pathFile+"/app/build/outputs/apk/debug/app-debug.apk";
        projectSrcPath=pathFile+"/app/src/";
        projectJavaPath=pathFile+"/app/src/main/java/";
        dealWithSpecialProjects(APKName, pathFile);
        projectClassFiles = pathFile+"/"+projectAPKPath.substring(pathFile.length()+1,projectAPKPath.indexOf("/",pathFile.length()+1))+"/build/intermediates/javac/debug/classes";
        dotFile = new File(projectClassFiles+"/dotfiles/classes.dot");
    }
    private void dealWithSpecialProjects(String name, String pathFile) {

        //some projects are weird
        switch(name){
            case "DynamicSink1": {
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File f = new File(projectGradlewPath);
                f.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicSink1/build/outputs/apk/debug/dynamicLoading_DynamicSink1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicSink1/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "Library2": {
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/androidSpecific_Library2/build/outputs/apk/debug/androidSpecific_Library2-debug.apk";
                projectSrcPath = pathFile + "/androidSpecific_Library2/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "DynamicBoth1":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicBoth1/build/outputs/apk/debug/dynamicLoading_DynamicBoth1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicBoth1/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "DynamicSource1":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicSource1/build/outputs/apk/debug/dynamicLoading_DynamicSource1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicSource1/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "DynamicLoadingTarget1":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/dynamicLoading_DynamicLoadingTarget1/build/outputs/apk/debug/dynamicLoading_DynamicLoadingTarget1-debug.apk";
                projectSrcPath = pathFile + "/dynamicLoading_DynamicLoadingTarget1/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "uk.co.yahoo.p1rpp.calendartrigger_7":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/app/build/outputs/apk/debug/CalendarTrigger-debug.apk";
                break;
            }
            case "com.nutomic.ensichat_17":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/android/build/outputs/apk/debug/android-debug.apk";
                projectSrcPath = pathFile + "/android/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "jackpal.androidterm_72":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/term/build/outputs/apk/debug/term-debug.apk";
                projectSrcPath = pathFile + "/term/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }
            case "trikita.talalarmo_19":{
                projectRootPath = pathFile;
                projectGradlewPath = pathFile + "/gradlew";
                File fw = new File(projectGradlewPath);
                fw.setExecutable(true);
                projectAPKPath = pathFile + "/build/outputs/apk/debug/trikita.talalarmo_19-debug.apk";
                projectSrcPath = pathFile + "/src/";
                projectJavaPath = projectSrcPath+"main/java/";
                break;
            }


        }
        //TODO:: add osmand and debian kit fossdroid projects
    }
}