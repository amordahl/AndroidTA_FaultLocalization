package edu.utdallas.amordahl.CoverageComparer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single coverage record emitted by the instrumentation.
 * @author Austin Mordahl
 */
public class DataStructureCoverageRecord implements ICoverageRecord<String, Object> {

	@Override
	public String toString() {
		return String.format("%s:%s:%s", location, type, content);
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DataStructureCoverageRecord.class);
	private String type;
	private String location;
	private Object content;
	
	public DataStructureCoverageRecord(String location, String type, Object content) {
		this.location = location;
		this.type = type;
		this.content = content;
	}
	
	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return location;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Object getDataStructureContent() {
		// TODO Auto-generated method stub
		return content;
	}
	
	
}
