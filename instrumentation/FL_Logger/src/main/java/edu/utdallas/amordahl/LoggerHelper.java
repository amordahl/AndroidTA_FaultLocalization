package edu.utdallas.amordahl;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerHelper {

    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    private static String lastNode;
    private static DefaultDirectedGraph<String, DefaultEdge> defaultEdgeDefaultDirectedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public static void logObjArray(Object[] objs, String location) {
//        if (lastNode != null) {
//            defaultEdgeDefaultDirectedGraph.addVertex(lastNode);
//            defaultEdgeDefaultDirectedGraph.addVertex(location);
//            defaultEdgeDefaultDirectedGraph.addEdge(lastNode, location);
//            CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(defaultEdgeDefaultDirectedGraph);
//            if (cycleDetector.detectCyclesContainingVertex(location)) {
//                logger.warn("Found a cycle! Aborting.");
//                defaultEdgeDefaultDirectedGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
//                return;
//            }
//        }
//        lastNode = location;
        StringBuilder sb = new StringBuilder();
        sb.append("ObjArray on " + location + "- [ ");
        for (Object o: objs) {
            try {
                sb.append(o.toString());
            } catch (NullPointerException np) {
                sb.append("null");
            } catch (Exception ex) {
                sb.append("EXCEPTION_CAUSED_WHEN_ACCESSING");
            } catch (Error er) {
                sb.append("ERROR_CAUSED_WHEN_ACCESSING");
            }
            sb.append(" ");
        }
        sb.append("]");
        logger.error(sb.toString());
    }
}
