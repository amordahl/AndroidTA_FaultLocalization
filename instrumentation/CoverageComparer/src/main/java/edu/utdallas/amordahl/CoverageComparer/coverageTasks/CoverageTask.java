package edu.utdallas.amordahl.CoverageComparer.coverageTasks;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

import edu.utdallas.amordahl.CoverageComparer.util.SupportedLocalization;

/**
 * This class represents an input to the coverage computation program.
 * It supplies the passed and failed records, along with the type of instrumentation requested.
 * @author Austin Mordahl
 *
 */
public class CoverageTask {
	
	
	public Set<Path> getFailed() {
		return failed;
	}

	public Set<Path> getPassed() {
		return passed;
	}

	public SupportedLocalization getLocalization() {
		return localization;
	}
	
	public Path getOriginalPath() {
		return originalPath;
	}

	public void setOriginalPath(Path originalPath) {
		this.originalPath = originalPath;
	}

	private Path originalPath;

	/**
	 * Set of files that represent failed test cases.
	 */
	private Set<Path> failed;
	
	/**
	 * Set of files that represent passed test cases.
	 */
	private Set<Path> passed;
	
	/**
	 * The localization that is requested.
	 */
	private SupportedLocalization localization;
	
	/**
	 * Constructs a {@link CoverageTask}.
	 * @param passed
	 * @param failed
	 * @param localization
	 */
	public CoverageTask(Path p, Set<Path> passed, Set<Path> failed, SupportedLocalization localization) {
		this.setOriginalPath(p);
		this.setFailed(failed);
		this.setPassed(passed);
		this.localization = localization;
	}
	
	/**
	 * Constructs a {@link CoverageTask}, with Tarantula localization.
	 * @param passed The set of passing records.
	 * @param failed The set of failing records.
	 */
	public CoverageTask(Path p, Set<Path> passed, Set<Path> failed) {
		this(p, passed, failed, SupportedLocalization.TARANTULA);
	}

	public void setPassed(Set<Path> passed) {
		this.passed = passed;
	}

	public void setFailed(Set<Path> failed) {
		this.failed = failed;
	}
}
