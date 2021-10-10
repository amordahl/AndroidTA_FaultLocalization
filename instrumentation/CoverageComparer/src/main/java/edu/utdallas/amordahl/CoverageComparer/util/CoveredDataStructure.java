package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class CoveredDataStructure implements Serializable {

	@Override
	public int hashCode() {
		return Objects.hash(dataStructureContent, location);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoveredDataStructure other = (CoveredDataStructure) obj;
		return Objects.equals(dataStructureContent, other.dataStructureContent)
				&& Objects.equals(location, other.location);
	}

	@Override
	public String toString() {
		return String.format("%s-%d:%s", this.getLocation().left, this.getLocation().right, this.getDataStructureContent());
	}
	
	/**
	 * 
	 * @param dataStructureContent
	 * @param location
	 */
	public CoveredDataStructure(String location, ArrayList<String> dataStructureContent) {
		super();
		this.dataStructureContent = dataStructureContent;
		int splitIndex = location.lastIndexOf("-");
		this.location = new ImmutablePair<CoveredLine, Integer>(new CoveredLine(location.substring(0, splitIndex).split(":")[0], 
				Integer.valueOf(location.substring(0, splitIndex).split(":")[1])),
				Integer.valueOf(location.substring(splitIndex+1, location.length())));
	}
	private static final long serialVersionUID = -3515116290649641320L;

	public ArrayList<String> getDataStructureContent() {
		return dataStructureContent;
	}
	
	/**
	 * The location is represented by a CoveredLine and an index, so that we can handle the case where there may be multiple data structures referenced on the same line.
	 * @return An ImmutablePair of a CoveredLine and an index.
	 */
	public ImmutablePair<CoveredLine, Integer> getLocation() {
		return location;
	}
	
	private ArrayList<String> dataStructureContent;
	private ImmutablePair<CoveredLine, Integer> location;
	
	
}
