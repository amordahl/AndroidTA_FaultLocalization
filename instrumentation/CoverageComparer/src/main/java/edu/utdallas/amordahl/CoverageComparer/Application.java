package edu.utdallas.amordahl.CoverageComparer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Parameter(names = {"-c1", "--coveragelog_dir1"}, description = "The directory containing the first set of coverage files.",
			required = true)
	protected String c1;
	
	@Parameter(names = {"-c2", "--coveragelog_dir2"}, description = "The directory containing the second set of coverage files.",
			required = true)
	protected String c2;
	
	@Parameter(names = {"-o", "--output"}, description = "The directory in which to store the output files.")
	protected String outputDir;
	
	@Parameter(names = "--help", help = true)
	protected boolean help;
	
	@Parameter(names = {"-t", "--threads"},
			description = "Number of threads to spawn.")
	protected Integer threads;
	
	/**
	 * Just sets up the JCommander argument parser.
	 * @param args The command-line arguments.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Application app = new Application();
		JCommander jcmd = JCommander.newBuilder().addObject(app).build();
		jcmd.parse(args);
		if (app.help) {
			jcmd.usage();
			return;
		}
		app.run();
	}
	
	private void run() throws IOException {
		ExecutorService ep = Executors.newFixedThreadPool(this.threads);
		Map<Path, Path> pairs = computePairs();
		for (Entry<Path, Path> e: pairs.entrySet()) {
			Runnable r = new CoveragePairTask(e.getKey(), e.getValue(), Paths.get(this.outputDir));
			ep.execute(r);
		}
		ep.shutdown();
	}
	
	/**
	 * Computes the matching pairs of files. Assumes that all files are named consistently from
	 * the coverageWrapper script, which is [apkname]_[config].instlog.
	 * @return A map containing the pairs of matching files.
	 */
	private Map<Path, Path> computePairs() {
		File[] dirList1 = getDirList(this.c1);
		File[] dirList2 = getDirList(this.c2);
		if (dirList1.length == dirList2.length) {
			throw new RuntimeException("Directories have different numbers of files in it.");
		}
		Map<Path, Path> pairs = new HashMap<Path, Path>();
		for (File f1: dirList1) {
			boolean found = false;
			for (File f2: dirList2) {
				if (getFirstPart(f1) == getFirstPart(f2)) {
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
	 * Takes a file and returns the first part of it (i.e., the part before the first underscore).
	 * @param f The file.
	 * @return The first part as a String.
	 */
	private String getFirstPart(File f) {
		return f.toPath().getFileName().toString().split("_")[0];
	}

	/**
	 * Returns the list of files in a directory that end with .instlog.
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
