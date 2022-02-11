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
        String[] extensions = {"txt"};
        //File[] allDirFile = debuggerDir.listFiles();
        //System.out.println(allDirFile);
        /*for(File x: allDirFile) {
            String[] extensions = {"txt"};
            if(x.isDirectory()) {
                List<File> allTXTFiles = ((List<File>) FileUtils.listFiles(x, extensions, false));
                //System.out.println(allTXTFiles);
                everyrunFile.addAll(allTXTFiles);
            }

        }*/
        List<File> allTXTFiles = ((List<File>) FileUtils.listFiles(debuggerDir, extensions, false));
        everyrunFile.addAll(allTXTFiles);

        String runprefix="";
        if(args.length>1) {

            if(args[1].equals("-p")){
                runprefix=args[2];
            }
        }

        System.out.println("this run prefix: "+ runprefix);

        String output="";

        /*String header="" +
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


            }




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
        }*/

        ArrayList<String> headerVals=new ArrayList<>();
        boolean first=true;
        for(File x: everyrunFile) {
            if (!x.getName().contains(runprefix)) {
                System.out.println("THIS DONT CONTAIN PREFIX " + x.getName());
                continue;
            }

            //get the data, verify the results
            String fname = x.getName().replace("_time.txt","");
            //apk config1 config2

            fname = fname.substring(runprefix.length()+1);


            //type,category,apk,config1,config2
            String[] components = fname.split("_");


            Scanner sc = new Scanner(x);
            String in="";
            while(sc.hasNextLine()){
                in+=sc.nextLine()+"\n";
            }
            in = in.replaceAll(":","");
            HashMap<String,String> mappedValues = new HashMap<>();
            HashMap<String,String> distValues = new HashMap<>();
            String[] lines = in.split("\n+");
            for(String line: lines){
                System.out.println("line: "+line);
                boolean dist=false;
                if(line.trim().isEmpty()){
                    continue;
                }
                if(line.contains("Counts")){
                    continue;
                }
                if(line.contains("Times")){
                    continue;
                }
                if(line.contains("Timers")){
                    continue;
                }
                if(line.contains("STARTCODECHANGES")){
                    break;
                }

                String[] lineChange = line.split("\\s+");
                if(line.contains("cucount")){
                    dist=true;
                }
                if(!dist){
                    headerVals.add(lineChange[0]);
                    mappedValues.put(lineChange[0],lineChange[1]);
                }
                else{
                    distValues.put(lineChange[0],lineChange[1]);
                }
            }

            //* Write dist stuff *//
            String distop ="";
            String distheader="File,good_aql,bad_aql,good_compile,bad_compile\n";
            String distline="";
            ArrayList<String> seen = new ArrayList<>();
            for(Map.Entry<String, String> e:distValues.entrySet()){
                String fName = e.getKey().replace("cucount_","");
                fName = fName.substring(0,fName.indexOf(".java"));
                System.out.println(fName);
                //distheader+=e.getKey()+",";
                //distline+=e.getValue()+",";
                if(!seen.contains(fName)){
                    seen.add(fName);

                    distline=distline+fName+","+distValues.get("cucount_"+fName+".java_good_aql")+","+distValues.get("cucount_"+fName+".java_bad_aql")+","
                            +distValues.get("cucount_"+fName+".java_good_compile")+","+distValues.get("cucount_"+fName+".java_bad_compile")+"\n";
                }
            }
            //distheader=distheader.substring(0,distheader.length()-1)+"\n";
            //distline=distline.substring(0,distline.length()-1)+"\n";
            distop=distheader+distline;

            File thing = new File("distributions/");
            thing.mkdir();
            File outF = new File("distributions/"+x.getName().replace("_time.txt","")+"_distributions.csv");
            FileWriter fw = new FileWriter(outF);
            fw.write(distop);
            fw.flush();
            fw.close();


            //* update line stuff *//
            Collections.sort(headerVals);
            if(first){
                first=false;
                String header="apk,category,violation_type,is_violation,config1,config2,";

                for(String heading:headerVals){
                    header=header+heading+",";
                }

                header=header.substring(0,header.length()-1);
                output+=header+"\n";
                System.out.println(header);
            }
            String line="";
            String apk = components[2];
            int index =3;
            while(index<=components.length-3){
                apk+="_"+components[index];
                index++;
            }

            line+=apk+","+components[1]+","+mappedValues.get("violation_type")+","+mappedValues.get("is_violation")+","+components[index]+","+components[index+1]+",";
            mappedValues.remove("violation_type");
            mappedValues.remove("is_violation");
            for(String key:headerVals){
                line+=mappedValues.get(key)+",";
            }
            line=line.substring(0,line.length()-1).trim()+"\n";
            ArrayList<String> crap = new ArrayList<>(mappedValues.keySet());
            Collections.sort(crap);

            System.out.println(mappedValues.keySet().size()+ " "+crap);
            output+=line;
            System.out.println(headerVals.size()+ " "+headerVals);
            headerVals=new ArrayList<>();

        }

        File outF = new File("deltadebugger_results"+Long.toHexString(System.currentTimeMillis()) +".csv");
        FileWriter fw = new FileWriter(outF);
        fw.write(output);
        fw.flush();
        fw.close();





    }

}
