package edu.utdallas.amordahl;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.objectutils.Wrapped;
import edu.utdallas.objectutils.Wrapper;

public class LoggerHelper {

    private static Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    private static HashMap<String, Wrapped> LOGS = new HashMap<>();
    private static int NUM_ITERS = 0;
    public static void logObjArray(Object[] objs, String location) throws Exception {
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
    	LOGS.put(location, Wrapper.wrapObject(objs));
        NUM_ITERS++;
        System.out.println(String.format("Size of logs: %d (%d iterations)", LOGS.size(), NUM_ITERS));
    }
}
