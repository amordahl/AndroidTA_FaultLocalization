package edu.utdallas.amordahl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import edu.columbia.cs.psl.phosphor.struct.TaintedWithObjTag;

public class TaintChecker {
	
	private static Logger logger = LoggerFactory.getLogger(TaintChecker.class);
	
	public static void checkTaint(Object obj, int lineNumber, String classname) {
//		logger.debug("inside call to checkTaint");
//		if (obj != null) {
//			try {
//				TaintedWithObjTag taintedObj = (TaintedWithObjTag)obj;
//				if (taintedObj.getPHOSPHOR_TAG() != null) {
//						System.out.println(String.format("TAINT:%s:%d", classname, lineNumber));
//				}
//			} catch (ClassCastException ex) {
//				logger.error("Could not cast object of type " + obj.getClass().toString() + " to TaintedWithObjTag.");
//			}
//		}
		throw new RuntimeException("Not implemented.");
	}
}
