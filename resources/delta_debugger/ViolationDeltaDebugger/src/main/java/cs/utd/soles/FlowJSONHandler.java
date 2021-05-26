package cs.utd.soles;

import com.utdallas.cs.alps.flows.Flow;
import com.utdallas.cs.alps.flows.Statement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FlowJSONHandler {

    public static ArrayList<Flow> turnTargetPathIntoFlowList(String filePath){
        ArrayList<Flow> returnList= new ArrayList<>();
        JSONParser pb = new JSONParser();
        try {
            JSONArray obj = (JSONArray) pb.parse(new FileReader(Paths.get(filePath).toFile()));
            obj.forEach((x)->returnList.add(getFlowForJSONObj((JSONObject)x)));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return returnList;
    }


    private static Flow getFlowForJSONObj(JSONObject obj) {
        Flow f = new Flow();
        Statement fSink = f.getSink();
        Statement fSource= f.getSource();
        JSONObject sinkJS = (JSONObject) obj.get("sink");
        JSONObject sourceJS = (JSONObject) obj.get("source");

        //set sink up
        fSink.setStatement((String) sinkJS.get("statement"));
        fSink.setMethod((String) sinkJS.get("method"));
        fSink.setClassname((String) sinkJS.get("classname"));

        //set source up

        fSource.setStatement((String) sourceJS.get("statement"));
        fSource.setMethod((String) sourceJS.get("method"));
        fSource.setClassname((String) sourceJS.get("classname"));


        return f;
    }
}
