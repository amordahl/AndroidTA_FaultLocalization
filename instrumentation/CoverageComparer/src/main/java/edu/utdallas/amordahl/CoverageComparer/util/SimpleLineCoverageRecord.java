package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.Serializable;

public class SimpleLineCoverageRecord extends CoverageRecord<String, Boolean> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6093174906146507804L;

	public SimpleLineCoverageRecord(String location, Class<?> type, Boolean content) {
		super(location, type, content);
		// TODO Auto-generated constructor stub
	}

}
