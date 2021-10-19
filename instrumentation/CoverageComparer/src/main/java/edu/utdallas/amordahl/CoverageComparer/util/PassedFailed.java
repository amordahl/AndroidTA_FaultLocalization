package edu.utdallas.amordahl.CoverageComparer.util;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple class that contains passed and failed data for a localization computation.
 * @author Austin Mordahl
 *
 * @param <T>
 * @param <S>
 */
public class PassedFailed<S> {
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
		passed = new HashMap<Path, Collection<S>>();
		failed = new HashMap<Path, Collection<S>>();
	}
	private Map<Path, Collection<S>> passed;
	private Map<Path, Collection<S>> failed;
}
