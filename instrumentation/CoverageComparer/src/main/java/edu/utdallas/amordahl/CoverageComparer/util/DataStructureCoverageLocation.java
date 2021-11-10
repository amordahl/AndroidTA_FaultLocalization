package edu.utdallas.amordahl.CoverageComparer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single coverage record emitted by the instrumentation.
 * @author Austin Mordahl
 */
public class DataStructureCoverageLocation {

	@Override
	public String toString() {
		return String.format("%s:%s", location, type);
	}

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DataStructureCoverageLocation.class);
	private String type;
	private String location;
	
	public DataStructureCoverageLocation(String location, String type) {
		this.location = location;
		this.type = type;
	}
	
	public String getLocation() {
		// TODO Auto-generated method stub
		return location;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}
	
}
