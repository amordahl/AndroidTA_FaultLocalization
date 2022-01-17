package edu.utdallas.amordahl.multiphaseinstrumenter.util;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineCoverageRecord {
	
	private static Logger logger = LoggerFactory.getLogger(LineCoverageRecord.class);

	public String getClazz() {
		return clazz;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	private String clazz;
	private Integer lineNumber;
	
	public LineCoverageRecord(String record) {
		String[] tokens = record.split(":");
		if (tokens.length != 2) {
			throw new RuntimeException(String.format("Coverage record %s could not be parsed.", record));
		}
		this.clazz = tokens[0];
		this.lineNumber = Integer.valueOf(tokens[1]);
	}

	public LineCoverageRecord(String name, Integer line) {
		clazz = name;
		lineNumber = line;
		logger.debug(String.format("Creating a new record with class %s on line %d", clazz, lineNumber));

	}

	@Override
	public int hashCode() {
		return Objects.hash(clazz, lineNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LineCoverageRecord)) return false;
		LineCoverageRecord other =  (LineCoverageRecord)obj;
		return (other.clazz.equals(this.clazz) && other.lineNumber.equals(this.lineNumber));
	}

	@Override
	public String toString() {
		return String.format("%s:%d", clazz, lineNumber);
	}
	
	
}
