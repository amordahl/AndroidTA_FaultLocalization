import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerHelper {

    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);

    public static void logObjArray(Object[] objs) {
        StringBuilder sb = new StringBuilder();
        sb.append("ObjArray: [ ");
        for (Object o: objs) {
            sb.append(o.toString());
            sb.append(" ");
        }
        sb.append("]");
        logger.error(sb.toString());
    }
}
