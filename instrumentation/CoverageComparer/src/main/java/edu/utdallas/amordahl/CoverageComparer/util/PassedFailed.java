package edu.utdallas.amordahl.CoverageComparer.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;

/**
 * A simple class that contains passed and failed data for a localization computation.
 * @author Austin Mordahl
 *
 * @param <T> The value of a 
 * @param <S>
 */
public class PassedFailed<S, T> {

	/**
	 * Searches both the passed and failed set of a pf object to find the given key.
	 * @param pf The passedfailed set to search.
	 * @param key The path to search as the key.
	 * @return The value that is associated with the key.
	 */
	//	@SuppressWarnings("unchecked")
	//	public static Collection<?> findValueForKey(PassedFailed<?> pf, Path key) {
	//		for (Object o : new Object[] {pf.getPassed(), pf.getFailed(), pf.getOther()} ) {
	//			if (((Map<Path, Collection<?>>)o).containsKey(key)) {
	//				return ((Map<Path, Collection<?>>)o).get(key);
	//			}
	//		}
	//		return null;
	//	}

	public Set<T> getValueOfInPath(S row, Path column) {
		return content.get(row).get(column) == null ? new HashSet<T>() : content.get(row).get(column);
	}

	public void setValueOfInPath(S row, Path column, T value) {
		if (!content.containsKey(row)) {
			content.put(row, new HashMap<Path, Set<T>>());
		}
		if (!content.get(row).containsKey(column)) {
			content.get(row).put(column, new HashSet<T>());
		}
		content.get(row).get(column).add(value);
	}

	public void setAllValuesForPath(Path column, Map<S, T> values) {
		values.entrySet().stream().forEach(e -> this.setValueOfInPath(e.getKey(), column, e.getValue()));
	}

	public List<Pair<S, T>> getUniverse() {
		List<Pair<S, T>> universe = new ArrayList<Pair<S, T>>();
		for (Entry<S, Map<Path, Set<T>>> e1: content.entrySet()) {
			for (Entry<Path, Set<T>> e2: e1.getValue().entrySet()) {
				e2.getValue().stream().forEach(e3 -> universe.add(new ImmutablePair<S, T>(e1.getKey(), e3)));
			}
		}
		return universe;
	}
	
	// Content is a table: The outer hashtable 
	private Map<S, Map<Path, Set<T>>> content;

	private Collection<Path> failed;
	private CoverageTask originatingTask;
	private Collection<Path> other;
	private Collection<Path> passed;

	public PassedFailed() {
		setPassed(new HashSet<Path>());
		setFailed(new HashSet<Path>());
		setOther(new HashSet<Path>());
		content = new HashMap<S, Map<Path, Set<T>>>();
	}

	public Collection<Path> getFailed() {
		return failed;
	}
	public CoverageTask getOriginatingTask() {
		return originatingTask;
	}
	public Collection<Path> getOther() {
		return other;
	}
	public Collection<Path> getPassed() {
		return passed;
	}

	public Collection<Path> getAllFiles() {
		Collection<Path> allFiles  = new HashSet<Path>();
		allFiles.addAll(passed); allFiles.addAll(failed); allFiles.addAll(other);
		return allFiles;
	}
	public void setFailed(Collection<Path> failed) {
		this.failed = failed;
	}

	// Utility functions

	public void setOriginatingTask(CoverageTask originatingTask) {
		this.originatingTask = originatingTask;
	}
	public void setOther(Collection<Path> other) {
		this.other = other;

	}
	public void setPassed(Collection<Path> passed) {
		this.passed = passed;
	}
}
