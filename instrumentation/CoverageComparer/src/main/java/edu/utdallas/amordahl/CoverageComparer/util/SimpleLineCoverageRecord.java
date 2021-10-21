package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.Serializable;
import java.util.Objects;

public class SimpleLineCoverageRecord implements ICoverageRecord<String, Boolean>, Serializable{

	private static final long serialVersionUID = 6093174906146507804L;

	private Boolean content;
	private String location;
	
	public SimpleLineCoverageRecord(String location, Boolean content) {
		this.location = location;
		this.content = content;
		// TODO Auto-generated constructor stub
	}
	
	public SimpleLineCoverageRecord(String location) {
		this.location = location;
		this.content = true;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleLineCoverageRecord other = (SimpleLineCoverageRecord) obj;
		return Objects.equals(content, other.content) && Objects.equals(location, other.location);
	}

	@Override
	public Boolean getDataStructureContent() {
		return this.getDataStructureContent();
	}

	@Override
	public String getLocation() {
		return this.getLocation();
	}

	@Override
	public Class<?> getType() {
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(content, location);
	}
	
	@Override
	public String toString() {
		return String.format("%s", this.location);
	}

}
