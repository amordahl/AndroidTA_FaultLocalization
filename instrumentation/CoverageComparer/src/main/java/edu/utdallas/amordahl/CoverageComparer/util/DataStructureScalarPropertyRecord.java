package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStructureScalarPropertyRecord implements ICoverageRecord<String, Integer>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5396708420517138151L;

	@Override
	public String toString() {
		return String.format("%s:%s:%s", location, type, content);
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DataStructureScalarPropertyRecord.class);
	private String type;
	private String location;
	private Integer content;
	
	public DataStructureScalarPropertyRecord(String location, String type, Integer content) {
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
	public Integer getDataStructureContent() {
		// TODO Auto-generated method stub
		return content;
	}
}
