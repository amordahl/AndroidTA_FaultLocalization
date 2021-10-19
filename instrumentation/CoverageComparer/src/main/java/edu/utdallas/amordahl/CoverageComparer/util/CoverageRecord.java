package edu.utdallas.amordahl.CoverageComparer.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single coverage record emitted by the instrumentation.
 * @author Austin Mordahl
 *
 * @param <S> The type of the key (e.g., a string location)
 * @param <T> The type of the value encoded (e.g., a data structure content, or simply a coverage record).
 */
public class CoverageRecord<S, T> {

	private static Logger logger = LoggerFactory.getLogger(CoverageRecord.class);
	private Class<?> type;
	

	@Override
	public int hashCode() {
		return Objects.hash(content, location, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoverageRecord<S, T> other = (CoverageRecord<S, T>) obj;
		return Objects.equals(content, other.content) && Objects.equals(location, other.location)
				&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		if (this.content instanceof Boolean) {
			logger.debug("Omitting boolean value of CoverageRecord.");
			
			return String.format("%s", this.getLocation());
		}
		return String.format("%s:%s", this.getLocation(), this.getDataStructureContent());
	}
	
	/**
	 * 
	 * @param content The content of the data structure.
	 * @param type The type of this data structure.
	 * @param location The location in which the data structure 
	 */
	public CoverageRecord(S location, Class<?> type, T content) {
		super();
		this.content = content;
		this.location = location;
		this.type = type;
	}
	

	public T getDataStructureContent() {
		return content;
	}
	
	/**
	 * The location is represented by a CoveredLine and an index, so that we can handle the case where there may be multiple data structures referenced on the same line.
	 * @return An ImmutablePair of a CoveredLine and an index.
	 */
	public S getLocation() {
		return location;
	}
	
	private T content;
	private S location;
	
	
}
