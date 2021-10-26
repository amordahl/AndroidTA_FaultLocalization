package cs.utd.soles;

import com.utdallas.cs.alps.flows.AQLFlowFileReader;
import com.utdallas.cs.alps.flows.Flowset;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Runner {

    //program takes in debugger directory, eats all the .txt files (which are the results of a run)
    //program also takes in violation_logs directory so we know what to compare stuff to, this is optional

    public static void main(String[] args) throws IOException {

        File debuggerDir = Paths.get(args[0]).toFile();

        List<File> everyrunFile = new ArrayList<>();
        File[] allDirFile = debuggerDir.listFiles();
        //System.out.println(allDirFile);
        for(File x: allDirFile) {
            String[] extensions = {"txt"};
            List<File> allTXTFiles = ((List<File>) FileUtils.listFiles(x, extensions, false));
            //System.out.println(allTXTFiles);
            everyrunFile.addAll(allTXTFiles);

        }
        String runprefix="";
        System.out.println("this run prefix: "+ runprefix);
        File violationLogsDir= null;
        ArrayList<Flowset> violationThingList =new ArrayList<>();
        if(args.length>1) {

            if(args[1].equals("-p")){
                runprefix=args[2];
            }
        }


        String output="";
        String lineData="";
        String header="" +
                "apk," +
                "config1," +
                "config2," +
                "program_runtime," +
                "violation_type," +
                "violation_or_not," +
                "setup_time,"+
                "binary_time,"+
                "dependency_graph_time,"+
                "avgGoodAqlBinary,"+
                "totalGoodAqlBinary,"+
                "avgGoodCompileBinary,"+
                "totalGoodCompileBinary,"+
                "avgBadAqlBinary,"+
                "totalBadAqlBinary,"+
                "avgBadCompileBinary,"+
                "totalBadCompileBinary,"+
                "%ofProgramTakenByBinary,"+
                "%ofProgramTakenByGoodAQLBinary,"+
                "%ofProgramTakenByGoodCompileBinary,"+
                "%ofProgramTakenByBadAQLBinary,"+
                "%ofProgramTakenByBadCompileBinary,"+
                "avgRotations,"+
                "totalRotations,"+
                "avgGoodAqlHDD,"+
                "totalGoodAqlHDD,"+
                "avgGoodCompileHDD,"+
                "totalGoodCompileHDD,"+
                "avgBadAqlHDD,"+
                "totalBadAqlHDD,"+
                "avgBadCompileHDD,"+
                "totalBadCompileHDD,"+
                "%ofProgramTakenByGoodAQLHDD,"+
                "%ofProgramTakenByGoodCompileHDD,"+
                "%ofProgramTakenByBadAQLHDD,"+
                "%ofProgramTakenByBadCompileHDD,"+
                "changesProposed,"+
                "startLineCount,"+
                "endLineCount,"+
                "%ReductionByLines,"+
                "bestRot,bestRotLines,bestRotLines%,worstRot,worstRotLines,worstRotLines%\n";
        output+=header;
        for(File x: everyrunFile){
            if(!x.getName().contains(runprefix)){
                System.out.println("THIS DONT CONTAIN PREFIX "+x.getName());
                continue;
            }
            System.out.println("THIS DO CONTAIN PREFIX "+x.getName());
            //get the data, verify the results
            String name = x.getName().replace("_time.txt","");
            //apk config1 config2
            String apkName = name.substring(runprefix.length()+1, name.indexOf("config"));
            name = name.substring(runprefix.length()+1+apkName.length());
            String config1 = name.substring(0, name.lastIndexOf("config"));
            name = name.substring((config1.length()));
            String config2 = name.substring(0);

            Scanner sc = new Scanner(x);
            String in="";
            while(sc.hasNextLine()){
                in+=sc.nextLine()+"\n";
            }
            in = in.replaceAll(":"," ");

            String[] inArr = in.split("\\s+");
            LineObj j = new LineObj(
                    apkName,config1,config2,inArr[1],
            inArr[3],inArr[5],inArr[7],inArr[9],
                    inArr[11],inArr[13],inArr[15],
                    inArr[17],inArr[19],inArr[21],inArr[23],
                    inArr[25],inArr[27],inArr[29],inArr[31],
                    inArr[33],inArr[35],inArr[37],inArr[39],inArr[41],
                    inArr[43],inArr[45],inArr[47],inArr[49],inArr[51],
                    inArr[53],inArr[55],inArr[57],inArr[59],inArr[61],
                    inArr[63],inArr[65],inArr[67],inArr[69],inArr[71],
                    inArr[73]);
            System.out.println(j);
            int indexStart=0;
            for(String f: inArr){
                if(f.equals("STARTCODECHANGES")){
                    break;
                }
                indexStart++;
            }

            //handle codechange data
            ArrayList<CodeChange> codeChangeList = new ArrayList<>();

            Comparator<CodeChange> bobC = (o1, o2) -> {
                if(o1.timeMade<o2.timeMade)
                    return -1;
                else if(o1.timeMade>o2.timeMade)
                    return 1;
                return 0;
            };


            for(int i=indexStart+2;i<inArr.length;i++){
                String[] thing = inArr[i].trim().split(",");
                codeChangeList.add(new CodeChange(Double.parseDouble(thing[0]),Integer.parseInt(thing[1]),Integer.parseInt(thing[2]),Integer.parseInt(thing[3]),Double.parseDouble(thing[4])));

            }

            //bestRotation is the one that removed the most lines,


            String line=j.apk+","
                    +j.config1+","
                    +j.config2+","
                    +j.runtime+","
                    +j.violationType +","
                    +j.violation_or_not+","
                    +j.setupTime+","
                    +j.binaryTime+","
                    +j.dependencyGraphTime+","
                    +j.avgGoodAQLBinary+","
                    +j.totalGoodAQLBinary+","
                    +j.avgGoodCompileBinary+","
                    +j.totalGoodCompileBinary+","
                    +j.avgBadAQLBinary+","
                    +j.totalBadAQLBinary+","
                    +j.avgBadCompileBinary+","
                    +j.totalBadCompileBinary+","
                    +j.percentBinary+","
                    +j.percentGoodAQLBinary+","
                    +j.percentGoodCompileBinary+","
                    +j.percentBadAQLBinary+","
                    +j.percentBadCompileBinary+","
                    +j.avgRotation+","
                    +j.totalRotation+","

                    +j.avgGoodAQLHDD+","
                    +j.totalGoodAQLHDD+","
                    +j.avgGoodCompileHDD+","
                    +j.totalGoodCompileHDD+","
                    +j.avgBadAQLHDD+","
                    +j.totalBadAQLHDD+","
                    +j.avgBadCompileHDD+","
                    +j.totalBadCompileHDD+","
                    +j.percentGoodAQLHDD+","
                    +j.percentGoodCompileHDD+","
                    +j.percentBadAQLHDD+","
                    +j.percentBadCompileHDD+","

                    +j.numCandidate+","
                    +j.startLines+","
                    +j.endLines+","
                    +j.percentLines+",";

            String bestRotation = getBestRotation(codeChangeList,j, true);
            line+=bestRotation+",";
            String worstRotation = getBestRotation(codeChangeList,j,false);
            line+=worstRotation+"\n";

            //if we want to verify the results of this line
            /*if(violationLogsDir !=null){

                //get the right input
                Violation matchingV = null;
                for(Violation v: violationThingList){
                    if(v.getConfig1().equals(config1.replace("config_flowdroid",""))
                        && v.getConfig2().equals(config2.replace("config_flowdroid",""))
                        && v.getApk().equals(apkName+".apk")){
                        //this violation matches up to this output
                        matchingV=v;
                    }

                }

                //check that our output is expected

                File f = new File(debuggerDir.getAbsolutePath()+"/minimized_apks/"+apkName+config1+config2+".apk");

                //TODO:: just do the run aql process on this apk and check it gets every flow in matchingV.flows()
                //TODO:: append to end of line and also header maybe


            }*/




            output+=line;

            File thing = new File("changes_csv/");
            thing.mkdir();

            File outF2 = new File("changes_csv/"+apkName+config1+config2+"LineData" +".csv");

            FileWriter fw2 = new FileWriter(outF2);
            double currentTotal=100.0;
            for(CodeChange b: codeChangeList){
                currentTotal=currentTotal-b.percentProgram;
                String str = String.format("%.3f",currentTotal);
                String lineer=b.timeMade+","+b.linesRemoved+","+str+"\n";
                fw2.write(lineer);
            }
            fw2.flush();
            fw2.close();
        }


        File outF = new File("deltadebugger_results"+Long.toHexString(System.currentTimeMillis()) +".csv");
        FileWriter fw = new FileWriter(outF);
        fw.write(output);
        fw.flush();
        fw.close();





    }

    private static String getBestRotation(ArrayList<CodeChange> codeChangeList, LineObj j, boolean isMax) {

        if(codeChangeList.size()==0){
            return "";
        }
        if(codeChangeList.size()==1){
            return (1)+","+(codeChangeList.get(0).linesRemoved)+","+((codeChangeList.get(0).linesRemoved)/(Double.parseDouble(j.percentGoodAQLHDD)+Double.parseDouble(j.percentBadAQLHDD)));
        }
        String returnString="";

        ArrayList<Integer> rotList = new ArrayList<>();
        rotList.add(0);
        //System.out.println(codeChangeList);
        for(CodeChange x: codeChangeList){
            //System.out.println(x);
            if(rotList.size()<x.rot+1){
                rotList.add(x.rot,x.linesRemoved);
            }
            else{
                int old = rotList.get(x.rot);
                rotList.set(x.rot,old+x.linesRemoved);
            }
            //System.out.println(rotList);
        }
        int max=0;
        int worst=0;
        for(int i=0;i<rotList.size();i++){

            if(rotList.get(i)>rotList.get(max)){
                max=i;
            }
            if(rotList.get(i)<rotList.get(worst)){
                worst=i;
            }
        }
        /*returnString="Rotation: "+(max+1)+" removed "+rotList.get(max)+" lines, which was "+(100*(rotList.get(max))/Double.parseDouble(j.percentAQL)) +"% of the program.\n" +

            "Rotation: "+(worst+1)+" removed "+rotList.get(worst)+" lines, which was "+(100*(rotList.get(worst))/Double.parseDouble(j.percentAQL)) +"% of the program.\n";*/

        if(isMax){
            returnString=(max+1)+","+rotList.get(max)+","+(100*(rotList.get(max))/(Double.parseDouble(j.percentGoodAQLHDD)+Double.parseDouble(j.percentBadAQLHDD)));
        }else{
            returnString=(worst+1)+","+rotList.get(worst)+","+(100*(rotList.get(worst))/(Double.parseDouble(j.percentGoodAQLHDD)+Double.parseDouble(j.percentBadAQLHDD)));
        }

        return returnString;
    }
}
