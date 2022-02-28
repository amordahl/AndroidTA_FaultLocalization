package cs.utd.soles.determinism;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import cs.utd.soles.setup.SetupClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class CheckDeterminism {


    public static boolean checkOrCreate(SetupClass programinfo, Node changedNode, String changeNum){

        String uniqueNameString = programinfo.getThisRunName().replace((String)programinfo.getArguments().getValueOfArg("RUN_PREFIX").get()+"_","");


        if(programinfo.getArguments().getValueOfArg("NO_OPTIMIZATION").isPresent()){
            uniqueNameString=uniqueNameString+"_noopt";
        }
        if(programinfo.getArguments().getValueOfArg("NO_ABSTRACT_METHODS").isPresent()){
            uniqueNameString=uniqueNameString+"_nam";
        }
        if(programinfo.getArguments().getValueOfArg("CLASS_REDUCTION").isPresent()){
            uniqueNameString=uniqueNameString+"_binary";
        }
        //either check or create
        String fp = "debugger/masterchange/"+uniqueNameString+"_"+changeNum+".java";

        File f = new File(fp);
        if(f.exists())
            return check(f,changedNode);
        else{
            return create(f,changedNode);
        }

    }

    private static boolean check(File f, Node changeNode) {
        try {
            Node x = StaticJavaParser.parse(f);
            return x.equals(changeNode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean create(File f, Node changeNode){

        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(changeNode.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
