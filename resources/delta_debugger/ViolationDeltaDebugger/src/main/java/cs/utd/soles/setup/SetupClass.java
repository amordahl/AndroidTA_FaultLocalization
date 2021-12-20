package cs.utd.soles.setup;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flowset;
import cs.utd.soles.DroidbenchProjectCreator;
import cs.utd.soles.PerfTracker;
import cs.utd.soles.schema.SchemaGenerator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class SetupClass {

    public String getApkName() {
        return apkName;
    }

    public String getConfig1() {
        return config1;
    }

    public String getConfig2() {
        return config2;
    }

    public boolean isTargetType() {
        return targetType;
    }

    public boolean isViolationOrNot() {
        return violationOrNot;
    }

    public Flowset getThisViolation() {
        return thisViolation;
    }

    public boolean isNeedsToBeMinimized() {
        return needsToBeMinimized;
    }

    public String getThisRunName() {
        return thisRunName;
    }

    public PerfTracker getPerfTracker(){
        return this.performance;
    }

    public CombinedTypeSolver getTypeSolver() {
        return typeSolver;
    }

    public ParserConfiguration getParserConfig() {
        return parserConfig;
    }

    public JavaParser getJavaParseInst() {
        return javaParseInst;
    }

    String apkName;
    String config1;
    String config2;
    boolean targetType;
    boolean violationOrNot;
    Flowset thisViolation;
    boolean needsToBeMinimized=true;
    ArgsHandler arguments;
    MinimizationTarget targetProject;
    String thisRunName;
    PerfTracker performance;
    CombinedTypeSolver typeSolver;
    ParserConfiguration parserConfig;
    JavaParser javaParseInst;
    /*
    * Setup is a couple of steps.
    * 1. we need a schema
    * 2. we need a project to work on
    * 3. we need to know where that project is
    * */

    public SetupClass(){
        performance = new PerfTracker();
        parserConfig = new ParserConfiguration();
        javaParseInst = new JavaParser(parserConfig);
    }
    public boolean doSetup(String[] args) throws IOException {

        SchemaGenerator.generateSchema();

        arguments = handleArgs(args);

        targetProject = createTargetProject((String)arguments.getValueOfArg("RUN_PREFIX").get());

        //TODO:: make this optional
        typeSolver=new CombinedTypeSolver();
        createAndAddLibsToSolver();
        parserConfig.setSymbolResolver(new JavaSymbolSolver(typeSolver));

        return true;
    }

    private void createAndAddLibsToSolver() {

        typeSolver.add(new ReflectionTypeSolver());

        // its in .gradle\\caches file
        String home = System.getProperty("user.home");
        String path = home+"/.gradle/caches";
        System.out.println("Gradle path: "+path);
        String[] extensions = {"jar"};
        List<File> jarLibs = ((List<File>) FileUtils.listFiles(Paths.get(path).toFile(),extensions,true));


        Map<String,String> env = System.getenv();
        String androidPlatforms = env.get("ANDROID_SDK_ROOT");
        if(androidPlatforms==null){
            System.out.println("ANDROID_SDK_ROOT not an environment variable... exiting...");
            System.exit(-1);
        }


        jarLibs.addAll(((List<File>) FileUtils.listFiles(Paths.get(androidPlatforms).toFile(),extensions,true)));

        for(File x: jarLibs){
            try {
                typeSolver.add(new JarTypeSolver(x));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    private ArgsHandler handleArgs(String[] args) {
        AQLFlowFileReader reader = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);


        //everything we need is in this here object
        thisViolation = reader.getFlowSet(Paths.get(args[0]).toFile());

        apkName="/"+thisViolation.getApk();
        config1="/home/dakota/documents/AndroidTAEnvironment/configurations/FlowDroid/1-way/config_FlowDroid_"+thisViolation.getConfig1()+".xml";
        config2="/home/dakota/documents/AndroidTAEnvironment/configurations/FlowDroid/1-way/config_FlowDroid_"+thisViolation.getConfig2()+".xml";
        targetType=thisViolation.getType().equalsIgnoreCase("soundness");
        violationOrNot=thisViolation.getViolation().toLowerCase().equals("true");;
        //the files with no flows we still need the apk info from so that we can save its apk, so figure out the apk from the filename
        //fix apkName
        if(apkName.equals("/")){

            String fileName = Paths.get(args[0]).toFile().getName();
            //split
            //flowset, violation-false(true), apk, split
            String[] split = fileName.split("_");
            //0, 1, 2,
            String concatApk=split[2];
            for(int i=3;i<split.length-1;i++){
                concatApk+="_"+split[i];
            }
            apkName="/"+concatApk;
            //this project shouldnt be minimized - nothing to minimize to.
            needsToBeMinimized=false;
        }

        return new ArgsHandler(thisViolation, args);
    }

    private MinimizationTarget createTargetProject(String prefix){

        //this pathfile needs to be unique so it will be apk_config1_config2

        String actualAPK = apkName.substring(apkName.lastIndexOf("/")+1,apkName.lastIndexOf(".apk"));
        String actualConfig1 = config1.substring(config1.lastIndexOf("/")+1,config1.lastIndexOf(".xml"));
        String actualConfig2 = config2.substring(config2.lastIndexOf("/")+1,config2.lastIndexOf(".xml"));
        thisRunName=prefix+"_"+actualAPK+actualConfig1+actualConfig2;
        String pathFile="debugger/project_files/"+thisRunName;
        System.out.println(pathFile);

        String[] args = {actualAPK, pathFile};
        DroidbenchProjectCreator.createProject(args);
        return new MinimizationTarget(actualAPK,pathFile);
    }

    public ArgsHandler getArguments() {
        return arguments;
    }

    public MinimizationTarget getTargetProject() {
        return targetProject;
    }
}



