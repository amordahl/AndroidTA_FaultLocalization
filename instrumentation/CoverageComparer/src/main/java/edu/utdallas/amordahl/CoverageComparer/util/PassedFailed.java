package edu.utdallas.amordahl.CoverageComparer.util;

import java.util.Collection;
import java.util.Map;

/**
 * A simple class that contains passed and failed data for a localization computation.
 * @author Austin Mordahl
 *
 * @param <T>
 * @param <S>
 */
public class PassedFailed<T, S> {
	public Map<T, Collection<S>> getPassed() {
		return passed;
	}
	public Map<T, Collection<S>> getFailed() {
		return failed;
	}
	public void setPassed(Map<T, Collection<S>> passed) {
		this.passed = passed;
	}
	public void setFailed(Map<T, Collection<S>> failed) {
		this.failed = failed;
	}
	private Map<T, Collection<S>> passed;
	private Map<T, Collection<S>> failed;
}
