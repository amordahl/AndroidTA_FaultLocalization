package cs.utd.soles.setup;

import com.utdallas.cs.alps.flows.Flowset;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArgsHandler{

    HashMap<String, Object> argValues=new HashMap<>();
    public ArgsHandler(Flowset thisViolation, String[] args){
        argValues.put("RUN_PREFIX","");
        for(int i=1;i<args.length;i++) {

            if (args[i].equals("-l")) {
                argValues.put("LOG",true);
            }
            if(args[i].equals("-c")){
                argValues.put("CLASS_REDUCTION",true);
            }
            if(args[i].equals("-m")){
                argValues.put("METHOD_REDUCTION",true);
            }
            if(args[i].equals("-hdd")){
                argValues.put("REGULAR_REDUCTION",true);
            }
            if(args[i].equals("-p")){
                String prefix=args[i+1];
                prefix = ""+thisViolation.getConfig1()+"_"+thisViolation.getConfig2()+"/"+prefix.replace("/","");
                argValues.put("RUN_PREFIX",prefix);
                i++;
            }
            if(args[i].equals("-t")){
                argValues.put("TIMEOUT_TIME_MINUTES",Integer.parseInt(args[i+1]));
                i++;
            }
            //add other args here if we want em
        }
    }
    public Optional<Object> getValueOfArg(String arg){
        return Optional.ofNullable(argValues.get(arg));
    }
    public String printArgValues(){
        String returnString="";
        for(Map.Entry<String, Object> e: argValues.entrySet()){
            returnString+=e.getKey().toString()+": "+e.getValue().toString()+"\n";
        }
        return returnString;
    }
}