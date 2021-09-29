package edu.utdallas.amordahl.CoverageComparer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Parameter(names = { "-c1",
			"--coveragelog_dir1" }, description = "The directory containing the first set of coverage files.", required = true)
	protected String c1;

	@Parameter(names = { "-c2",
			"--coveragelog_dir2" }, description = "The directory containing the second set of coverage files.", required = true)
	protected String c2;

	@Parameter(names =  "--faulty", description = "The text file containing the faulty records.", required = true)
	protected String faulty;
	
	@Parameter(names = { "-o",
			"--output" }, description = "The directory in which to store the output files.", required = true)
	protected String outputDir;

	@Parameter(names = "--help", help = true)
	protected boolean help;

	@Parameter(names = { "-t", "--threads" }, description = "Number of threads to spawn.")
	protected Integer threads = 8;

	@Parameter(names = "--no-delta", description = "Do not compute fault localization rankings on the delta.")
	protected boolean noDelta = false;
	
	@Parameter(names = "--delta-based-count", description = "Compute rankings using the delta, such that if A is a failing test case "
			+ "and B is the corresponding config on the same APK, a statement executed by A will only be counted as failed if it "
			+ "was executed by A and not by B, otherwise it will be counted as successful.")
	protected boolean deltaBasedCount = false;

	@Parameter(names = "--minuend", description = "A string indicating which "
			+ "member of the pair should be considered the minuend (i.e., A in A - B) "
			+ "in the delta computation. The file names will be searched for this string, "
			+ "and those that match will be considered as the minuend. In general, "
			+ "this should be the faulty configuration.")
	protected String minuend;
	
	@Parameter(names = "--preserve", description = "How much of the output to preserve. "
			+ "Values <= 1 are treated as percentages, while values > 1 are the number of top "
			+ "results to include.")
	protected double preserve = 1.0;
	
	/**
	 * Just sets up the JCommander argument parser.
	 * 
	 * @param args The command-line arguments.
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Application app = new Application();
		JCommander jcmd = JCommander.newBuilder().addObject(app).build();
		jcmd.parse(args);
		if (app.help) {
			jcmd.usage();
			return;
		}
		app.sanityCheck();
		app.run();
	}
	
	private void sanityCheck() {
		if (noDelta && deltaBasedCount) {
			throw new RuntimeException("--no-delta and --delta-based-counts cannot be enabled simultaneously.");
		}
	}

	private void run() throws IOException, InterruptedException {
		ExecutorService ep = Executors.newFixedThreadPool(this.threads);
		Map<Path, Path> pairs = computePairs();
		for (Entry<Path, Path> e : pairs.entrySet()) {
			Runnable r = new CoveragePairTask(e.getKey(), e.getValue(), Paths.get(this.outputDir));
			ep.execute(r);
		}
		ep.shutdown();
		ep.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		// Now, if there are faulty.txt files, we need to compute fault suspiciousness.
		List<Path> faultyRuns = new ArrayList<Path>();
		faultyRuns.addAll(Files.readAllLines(Paths.get(faulty)).stream().map(st -> Paths.get(st)).collect(Collectors.toList()));

		if (faultyRuns.size() == 0) {
			logger.info("faulty.txt not found. Exiting without computing suspiciousness.");
			System.exit(0);
		}

		// Else, we have to compute suspiciousness.
		logger.info("Now computing statement suspiciousness over " + 
		CoveragePairTask.getRecords().size() + " records.");
		
		Map<String, Pair<Integer, Integer>> statementCounts = noDelta ? 
				getStatementCounts(CoveragePairTask.getRecords(), faultyRuns) :
				getStatementCountsOnDelta(CoveragePairTask.getRecords(), faultyRuns);
					
		Map<String, Double> tarantulaSuspiciousness = computeTarantulaSuspiciousness(statementCounts,
				faultyRuns.size(), pairs.size() * 2 - faultyRuns.size());
		
		// Sort by suspiciousness
		List<Entry<String, Double>> sortedSus = new ArrayList<>(tarantulaSuspiciousness.entrySet());
		sortedSus.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));		
		if (this.preserve < 1.0) {
			int sizeToKeep = (int) Math.ceil(sortedSus.size() * this.preserve);
			sortedSus = sortedSus.subList(0, sizeToKeep);
		} else if (this.preserve > 1.0) {
			sortedSus = sortedSus.subList(0, (int)preserve);
		}
		// Output suspiciousness of each statement.
		sortedSus.forEach(e -> System.out.println(String.format("%s,%f", e.getKey(), e.getValue())));
	}
	
	

	private Map<String, Pair<Integer, Integer>> getStatementCountsOnDelta(Map<Path, Set<String>> records,
			List<Path> faultyRuns) {
		// This method needs to reference the faultyRuns list and the minuend field
		// to figure out which pairs to make. Then, it should compute the delta.
		Map<String, MutablePair<Path, Path>> pairs = new HashMap<String, MutablePair<Path, Path>>();
		for (Path p: records.keySet()) {
			// This for loop iterates through the paths and adds them to the
			//  map. It uses the minuend filter to identify which path should be added
			//  as the minuend (i.e., A in A - B, the left half of the pair) and the
			//  subtrahend (i.e., B in A - B).
		    String apk = p.getFileName().toString().split("_")[0].split("\\.")[0];
			logger.info(String.format("APK is %s", apk));
			if (!pairs.containsKey(apk)) {
				pairs.put(apk, new MutablePair<Path, Path>());
			}
			MutablePair<Path, Path> pair = pairs.get(apk);
			if (p.getFileName().toString().contains(minuend)) {
				if (pair.getLeft() != null) {
					throw new RuntimeException(String.format("Trying to add %s as minuend, "
							+ "but %s is already listed as the minuend. "
							+ "Perhaps the minuend filter %s is too broad?", 
							p.toString(), pair.getLeft().toString(),
							minuend));
				}
				else {
				    logger.info("Setting " + p + " as left.");
					pair.setLeft(p);
				}
			} else {
				if (pair.getRight() != null) {
					throw new RuntimeException(String.format("Trying to add %s as the "
							+ "subtrahend, but %s is already listed as the subtrahend. "
							+ "Perhaps the minuend filter %s is too narrow?",
							p.toString(), pair.getRight().toString(),
							minuend));
				}
				else {
				    logger.info("Setting " + p + " as right.");
				    pair.setRight(p);
				}
			}
		}
		
		Map<Path, Set<String>> deltaRecords = new HashMap<Path, Set<String>>();
		
		for (Pair p: pairs.values()) {
			if (p.getLeft() == null || p.getRight() == null) {
				throw new RuntimeException(String.format("Could not find a partner for %s!",
						p.getLeft() == null ? p.getRight() : p.getLeft()));
			}
			
			if (faultyRuns.contains(p.getRight())) {
				logger.warn(String.format("The subtrahend %s is listed as faulty. "
						+ "Unless you're sure what you're doing, this likely indicates that "
						+ "you've used the incorrect minuend (typically, the minuend should "
						+ "match the faulty records.", p.getRight().toString()));
			}
			
			if (deltaBasedCount) {
				// If a configuration A is in the faulty runs, we add A-B as the statements for that.
				//  However, we also produce a configuration A' that contains A - (A - B) that passes.
				if (faultyRuns.contains(((Path) p.getLeft()).getFileName())) {
					logger.info(String.format("%s is in faultyRuns, so computing the difference.", (Path)p.getLeft()));
					// Failed statements is A - B.
					Set<String> failedStatements = new HashSet<String>(records.get(p.getLeft()));
					failedStatements.removeAll(records.get(p.getRight()));
					
					logger.info(String.format("Difference contains %d elements.", failedStatements.size()));
					// Put in the failed statements for the record p.getLeft().
					deltaRecords.put((Path) p.getLeft(), failedStatements);
					
					// Now, we generate a P' that contains A - (A - B)
					records.get(p.getLeft()).removeAll(failedStatements);
					logger.info(String.format("Generating a passing case %s with %d elements.", 
							((Path)p.getLeft()).resolve("passed"), records.get(p.getLeft()).size()));
					deltaRecords.put(((Path)p.getLeft()).resolveSibling("passed"), records.get(p.getLeft()));
				} else {
					deltaRecords.put((Path) p.getLeft(), records.get(p.getLeft()));
				}
				
				// No matter what, we add the passing case.
				deltaRecords.put((Path) p.getRight(), records.get(p.getRight()));
			}
			else {
				// Compute the difference.
				records.get(p.getLeft()).removeAll(records.get(p.getRight()));

				logger.debug(String.format("Putting delta for %s: %s", p.getLeft(), records.get(p.getLeft()).toString()));
				deltaRecords.put((Path) p.getLeft(), records.get(p.getLeft()));
			}
		}
		
		return getStatementCounts(deltaRecords, faultyRuns);
	}

	/**
	 * Computes statement counts for standard fault localization (no delta).
	 * @param records Records mapping paths to a set of statements executed.
	 * @param faultyRuns The list of faulty runs.
	 * @return A map, mapping linenumbers to how many times they were executed by failed and successful test cases.
	 */
	private Map<String, Pair<Integer, Integer>> getStatementCounts(Map<Path, Set<String>> records, List<Path> faultyRuns) {
		Map<String, Pair<Integer, Integer>> statementCounts = new HashMap<String, Pair<Integer, Integer>>();
		for (Entry<Path, Set<String>> entry : records.entrySet()) {
			Set<String> fileContent = entry.getValue();
			Path file = entry.getKey().getFileName();
			logger.debug("Faulty runs is " + faultyRuns.toString());
			logger.debug("Checking if " + file.toString() + " is in faulty.");
			boolean isFaulty = faultyRuns.contains(file);
			if (isFaulty) logger.debug(String.format("%s is faulty", file));
			for (String line : fileContent) {
				if (!statementCounts.containsKey(line)) {
					statementCounts.put(line, new ImmutablePair<Integer, Integer>(0, 0));
				}
				Pair<Integer, Integer> old = statementCounts.get(line);
				if (isFaulty) {
					statementCounts.put(line, new ImmutablePair<Integer, Integer>(old.getLeft() + 1, old.getRight()));
				} else {
					statementCounts.put(line, new ImmutablePair<Integer, Integer>(old.getLeft(), old.getRight() + 1));
				}
			}
		}
		
		return statementCounts;
	}

	private Map<String, Double> computeTarantulaSuspiciousness(
			Map<String, Pair<Integer, Integer>> statementCounts, 
			Integer numFaulty, Integer numSuccessful) {
		statementCounts.forEach(new BiConsumer<String, Pair<Integer, Integer>>() {

			@Override
			public void accept(String t, Pair<Integer, Integer> u) {
				logger.debug("Statement: %s (%d/%d)", t, u.getRight(), u.getLeft());
			}
			
		});
		Map<String, Double> suspiciousness = 
				statementCounts.entrySet().stream()
				.map(pr -> new ImmutablePair<String, Double>(pr.getKey(),
						((pr.getValue().getLeft() / Double.valueOf(numFaulty)) / 
						((pr.getValue().getLeft() / Double.valueOf(numFaulty) +
						(pr.getValue().getRight() / Double.valueOf(numSuccessful)))))))
				.collect(Collectors.toMap(pair -> pair.getLeft(), pair -> pair.getRight()));
		return suspiciousness;

	}

	/**
	 * Computes the matching pairs of files. Assumes that all files are named
	 * consistently from the coverageWrapper script, which is
	 * [apkname]_[config].instlog.
	 * 
	 * @return A map containing the pairs of matching files.
	 */
	private Map<Path, Path> computePairs() {
		File[] dirList1 = getDirList(this.c1);
		File[] dirList2 = getDirList(this.c2);
		if (dirList1.length != dirList2.length) {
			throw new RuntimeException("Directories have different numbers of files in it.");
		}
		Map<Path, Path> pairs = new HashMap<Path, Path>();
		for (File f1 : dirList1) {
			boolean found = false;
			for (File f2 : dirList2) {
				if (getFirstPart(f1).equals(getFirstPart(f2))) {
					logger.info(String.format("%s and %s are matches.", f1.toString(), f2.toString()));
					pairs.put(f1.toPath(), f2.toPath());
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException(String.format("Could not find a match for %s", f1.toString()));
			}
		}
		return pairs;
	}

	/**
	 * Takes a file and returns the first part of it (i.e., the part before the
	 * first underscore).
	 * 
	 * @param f The file.
	 * @return The first part as a String.
	 */
	private String getFirstPart(File f) {
	    String firstPart = f.toPath().getFileName().toString().split("_")[0].split("\\.")[0];
	    logger.info("firstPart is " + firstPart);
	    return firstPart;
	}

	/**
	 * Returns the list of files in a directory that end with .instlog.
	 * 
	 * @param directory The directory.
	 * @return The list of files.
	 */
	private File[] getDirList(String directory) {
		return new File(directory).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().endsWith(".instlog");
			}
		});
	}

}
