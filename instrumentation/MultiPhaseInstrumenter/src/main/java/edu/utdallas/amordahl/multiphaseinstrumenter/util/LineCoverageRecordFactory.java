package edu.utdallas.amordahl.multiphaseinstrumenter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineCoverageRecordFactory {

	private static Logger logger = LoggerFactory.getLogger(LineCoverageRecordFactory.class);
	
	public static LineCoverageRecord makeLineCoverageRecord(String record) {
		try {
			return new LineCoverageRecord(record);
		}
		catch (RuntimeException re) {
			logger.error(re.getMessage());
			return null;
		}
	}
}
