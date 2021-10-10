package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * Simply represents a covered line in a coverage record (e.g., foo:1).
 * @author Austin Mordahl
 *
 */
public class CoveredLine implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1981338624800671426L;

	@Override
	public int hashCode() {
		return Objects.hash(lineNumber, location);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoveredLine other = (CoveredLine) obj;
		return Objects.equals(lineNumber, other.lineNumber) && Objects.equals(location, other.location);
	}
	public String getLocation() {
		return location;
	}
	public Integer getLineNumber() {
		return lineNumber;
	}
	private final String location;
	private final Integer lineNumber;
	
	public CoveredLine(String location, Integer lineNumber) {
		this.location = location;
		this.lineNumber = lineNumber;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d", location, lineNumber);
	}

	public static CoveredLine fromString(String s) {
		return new CoveredLine(s.split(":")[0], Integer.valueOf(s.split(":")[1]));
	}
	
}
