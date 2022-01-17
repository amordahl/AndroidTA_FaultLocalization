import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flow;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;

public class Runner {


    static String testFile="D:\\Local_androidTAEnvironment\\Droidbench\\DroidBench30\\DroidBench30\\benchmark\\groundtruth\\ArraysAndLists\\";
    static String groundTruthFile="D:\\Local_androidTAEnvironment\\local_AndroidTA_faultlocalize\\AndroidTA_FaultLocalization\\resources\\groundtruths\\droidbench_groundtruths.xml";
    public static void main(String[] args) throws FileNotFoundException {

        try{
            SchemaGenerator.generateSchema();
        }catch(Exception e){
            e.printStackTrace();
        }

        File[] fileArr = Paths.get(testFile).toFile().listFiles();
        for(File oneFile:fileArr) {
            System.out.println(oneFile.getName());
            ArrayList<Flow> flowsInFile = getFlowStrings(oneFile);
            for (Flow f : flowsInFile) {
                Flow classedFlow = classifyFlow(new String[]{groundTruthFile}, f);
                System.out.println(classedFlow != null ? classedFlow.getClassification() : null);
            }
        }

    }


    public static ArrayList<Flow> getFlowStrings(File xmlFile){
        AQLFlowFileReader aff = new AQLFlowFileReader(SchemaGenerator.SCHEMA_PATH);
        Iterator<Flow> flowIt = aff.getFlows(xmlFile);
        ArrayList<Flow> out = new ArrayList<>();
        while(flowIt.hasNext()){
            out.add(flowIt.next());

        }
        return out;
    }

    public static Flow classifyFlow(String[] groundtruthFilePaths, Flow x){

        ArrayList<Flow> groundtruthFlows = new ArrayList<>();
        for(String filePath: groundtruthFilePaths){
            groundtruthFlows.addAll(getFlowStrings(Paths.get(filePath).toFile()));
        }
        int index =groundtruthFlows.indexOf(x);
        if(index<0)
            return null;
        return groundtruthFlows.get(index);
    }


}
