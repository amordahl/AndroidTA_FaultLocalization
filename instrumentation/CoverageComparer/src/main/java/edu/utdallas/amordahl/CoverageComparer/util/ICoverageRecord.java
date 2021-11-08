package edu.utdallas.amordahl.CoverageComparer.util;

public interface ICoverageRecord<S, T> {

	T getDataStructureContent();

	/**
	 * The location is represented by a CoveredLine and an index, so that we can handle the case where there may be multiple data structures referenced on the same line.
	 * @return An ImmutablePair of a CoveredLine and an index.
	 */
	S getLocation();
	
	String getType();

}