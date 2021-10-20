package edu.utdallas.amordahl.CoverageComparer.util;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;

/**
 * A simple class that contains passed and failed data for a localization computation.
 * @author Austin Mordahl
 *
 * @param <T>
 * @param <S>
 */
public class PassedFailed<S> {
	private Map<Path, Collection<S>> other;

	public Map<Path, Collection<S>> getPassed() {
		return passed;
	}
	public Map<Path, Collection<S>> getFailed() {
		return failed;
	}
	public void setPassed(Map<Path, Collection<S>> passed) {
		this.passed = passed;
	}
	public void setFailed(Map<Path, Collection<S>> failed) {
		this.failed = failed;
	}
	
	public PassedFailed() {
		setPassed(new HashMap<Path, Collection<S>>());
		setFailed(new HashMap<Path, Collection<S>>());
		setOther(new HashMap<Path, Collection<S>>());
	}
	public CoverageTask getOriginatingTask() {
		return originatingTask;
	}
	public void setOriginatingTask(CoverageTask originatingTask) {
		this.originatingTask = originatingTask;
	}
	private Map<Path, Collection<S>> passed;
	private Map<Path, Collection<S>> failed;
	private CoverageTask originatingTask;

	// Utility functions
	
	/**
	 * Searches both the passed and failed set of a pf object to find the given key.
	 * @param pf The passedfailed set to search.
	 * @param key The path to search as the key.
	 * @return The value that is associated with the key.
	 */
	public static Collection<?> findValueForKey(PassedFailed<?> pf, Path key) {
		for (Object o : new Object[] {pf.getPassed(), pf.getFailed(), pf.getOther()} ) {
			if (((Map<Path, Collection<?>>)o).containsKey(key)) {
				return ((Map<Path, Collection<?>>)o).get(key);
			}
		}
		return null;
	}
	public void setOther(Map<Path, Collection<S>> other) {
		this.other = other;
		
	}
	public Map<Path, Collection<S>> getOther() {
		return other;
	}
	public void setOther(HashMap<Path, Collection<S>> other) {
		this.other = other;
	}
}
