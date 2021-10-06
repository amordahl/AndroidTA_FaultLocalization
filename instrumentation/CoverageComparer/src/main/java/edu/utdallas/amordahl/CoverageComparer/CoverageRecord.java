package edu.utdallas.amordahl.CoverageComparer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoverageRecord {

	private static Logger logger = LoggerFactory.getLogger(CoverageRecord.class);

	private static Map<String, Integer> computeFrequencyMap(ArrayList<String> fileContents, Path intermediate) {
		HashMap<String, Integer> frequencyMap = new HashMap<>();

		for (String k : fileContents) {
			String[] tokens = k.split(",");
			String line = tokens[0];
			Integer value = Integer.valueOf(tokens[1]);
			if (!frequencyMap.containsKey(k)) {
				frequencyMap.put(k, Integer.valueOf(0));
			}
			frequencyMap.put(k, frequencyMap.get(k) + value);
		}

		writeSetToFile(frequencyMap, intermediate);
		return frequencyMap;

	}

	private static Set<String> convertListToSet(Map<String, ?> map) {
		return map.keySet();
	}

	@SuppressWarnings("unused")
	private static Integer getMax(ArrayList<Integer> al) {
		Integer max = Integer.valueOf(-1);
		for (Integer i : al) {
			if (i > max)
				max = i;
		}
		return max;
	}

	@SuppressWarnings("unused")
	private static Double getMean(ArrayList<Integer> al) {
		return getSum(al).doubleValue() / al.size();
	}

	protected static Double getMedian(ArrayList<Integer> al) {
		if (al == null || al.size() == 0) {
			return Double.NaN;
		}
		if (al.size() == 1)
			return Double.valueOf(al.get(0));
		@SuppressWarnings("unchecked")
		ArrayList<Integer> sorted = (ArrayList<Integer>) al.clone();
		sorted.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				// TODO Auto-generated method stub
				return arg0.compareTo(arg1);
			}

		});
		if (sorted.size() % 2 == 0) {
			// Even, we have to average the two middle elements.
			return (sorted.get((int) (Math.ceil((sorted.size() - 1) / 2.0)))
					+ sorted.get((int) (Math.floor((sorted.size() - 1) / 2.0)))) / 2.0;
		} else
			return Double.valueOf(sorted.get((sorted.size() - 1) / 2));
	}

	@SuppressWarnings("unused")
	private static Integer getMin(ArrayList<Integer> al) {
		Integer min = Integer.valueOf(Integer.MAX_VALUE);
		for (Integer i : al) {
			if (i < min)
				min = i;
		}
		return min;
	}

	private static Integer getSum(ArrayList<Integer> al) {
		int sum = 0;
		for (Integer i : al)
			sum += i;
		return sum;
	}

	private static Map<String, Integer> readFileContents(Path coverageFile) {
	    Path intermediate = coverageFile.resolveSibling("." + coverageFile.getFileName() + ".set");
		if (Files.exists(intermediate)) {
		    logger.info(String.format("Intermediate file for %s exists.", coverageFile.getFileName()));
		    return readSetFromFile(intermediate);
		}

		logger.info(String.format("Intermediate file for %s does not exist. Generating the file now.", coverageFile.getFileName()));
		ArrayList<String> fileContent = new ArrayList<>();
		HashMap<Integer, String> mapping = new HashMap<>();
		try (Scanner sc = new Scanner(coverageFile.toFile())) {
		    while (sc.hasNext()) {
			String line = sc.next().replaceAll("\\P{Print}", "");
			if (line.contains("=")) {
			    // Mapping line. Need to store map in hashmap.
			    String[] tokens = line.split("=");
			    mapping.put(Integer.valueOf(tokens[1]), tokens[0]);
			} else if (line.contains(":")) {
				// We have two possible encodings that we need to handle.
				//  The default encoding is a:b,v, where a is a class name (or code),
				//  b is the line number, and v is a value e.g., size.
				//  Sometimes, v is omitted, in which case the value should be assumed to be 1. 
				Integer value = 1;
				if (line.contains(",")) {
					String[] topLevelTokens = line.split(",");
					line = topLevelTokens[0];
					value = Integer.valueOf(topLevelTokens[1]);
				}
				String[] tokens = line.split(":");
			    String actualName = mapping.get(Integer.valueOf(tokens[0]));
			    fileContent.add(String.format("%s:%s,%d", actualName, tokens[1], value));
			}
		    }
		} catch (FileNotFoundException e) {
		    logger.error("Could not find file " + coverageFile);
		    // TODO Auto-generated catch block
		    e.printStackTrace();
			System.exit(2);
		}
		return computeFrequencyMap(fileContent, intermediate);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Integer> readSetFromFile(Path intermediate) {
		Map<String, Integer> result = null;
		try (FileInputStream f = new FileInputStream(intermediate.toFile());
				ObjectInputStream o = new ObjectInputStream(f)) {
			result = (Map<String, Integer>) o.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Writes the frequencyMap to a file. This way, once that the initial
	 * computation is done, we can easily read it back in instead of doing the very
	 * expensive I/O.
	 *
	 * @param frequencyMap
	 * @param intermediate
	 */
	private static void writeSetToFile(Map<String, Integer> frequencyMap, Path intermediate) {
		try (FileOutputStream f = new FileOutputStream(intermediate.toFile());
				ObjectOutputStream o = new ObjectOutputStream(f)) {
			o.writeObject(frequencyMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Path coverageFile1, coverageFile2, outputFile;

	private ArrayList<Integer> f1minus2diffs, f2minus1diffs;

	private ArrayList<String> file1minus2, file2minus1;

	Map<String, Integer> frequencyMap1, freq_file1minus2, freq_file2minus1;
	Map<String, Integer> frequencyMap2;
	private JSONObject jo;
	private Set<String> set1;
	private Set<String> set2;

	@SuppressWarnings("unchecked")
	public CoverageRecord(Path coverageFile1, Path coverageFile2, Path outputFile) throws IOException {
		this.coverageFile1 = coverageFile1;
		this.coverageFile2 = coverageFile2;
		this.outputFile = outputFile;
		file1minus2 = new ArrayList<>();
		file2minus1 = new ArrayList<>();
		freq_file1minus2 = new HashMap<>();
		freq_file2minus1 = new HashMap<>();
		f1minus2diffs = new ArrayList<>();
		f2minus1diffs = new ArrayList<>();

		logger.info("Got arguments, now reading in file contents.");
		logger.info(String.format("c1 is %s, c2 is %s", this.coverageFile1, this.coverageFile2));
		// Read in file contents.
		frequencyMap1 = readFileContents(coverageFile1);
		frequencyMap2 = readFileContents(coverageFile2);

		logger.info("Computing frequency map.");
		// Compute frequencies

		// Compute sets
		logger.info("Computing sets.");
		set1 = convertListToSet(frequencyMap1);
		set2 = convertListToSet(frequencyMap2);

		for (String s : set1) {
			if (!set2.contains(s)) {
				file1minus2.add(s.replace("/", "_"));
			}
		}
		for (String s : set2) {
			if (!set1.contains(s)) {
				file2minus1.add(s.replace("/", "_"));
			}
		}
		for (String k : frequencyMap1.keySet()) {
			if (!frequencyMap2.containsKey(k)) {
				freq_file1minus2.put(k.replace(",", "_"), frequencyMap1.get(k));
				f1minus2diffs.add(frequencyMap1.get(k));
			} else if (frequencyMap1.get(k) > frequencyMap2.get(k)) {
				freq_file1minus2.put(k.replace(",", "_"), frequencyMap1.get(k) - frequencyMap2.get(k));
				f1minus2diffs.add(frequencyMap1.get(k) - frequencyMap2.get(k));
			}
		}

		for (String k : frequencyMap2.keySet()) {
			if (!frequencyMap1.containsKey(k)) {
				freq_file2minus1.put(k.replace(",", "_"), frequencyMap2.get(k));
				f2minus1diffs.add(frequencyMap2.get(k));
			} else if (frequencyMap2.get(k) > frequencyMap1.get(k)) {
				freq_file2minus1.put(k.replace(",", "_"), frequencyMap2.get(k) - frequencyMap1.get(k));
				f2minus1diffs.add(frequencyMap2.get(k) - frequencyMap1.get(k));
			}
		}
		// Now, compute the differences.
		jo = new JSONObject();
		jo.put("File1", coverageFile1);
		jo.put("File2", coverageFile2);
		jo.put("Presence_File1MinusFile2", file1minus2);
		jo.put("Presence_File2MinusFile1", file2minus1);
		jo.put("Frequency_File1MinusFile2", freq_file1minus2);
		jo.put("Frequency_File2MinusFile1", freq_file2minus1);

		if (outputFile != null) {
			try (FileWriter fw = new FileWriter(outputFile.toFile())) {
				fw.write(jo.toJSONString());
			}
		}
	}
	public Path getCoverageFile1() {
		return coverageFile1;
	}

	public Path getCoverageFile2() {
		return coverageFile2;
	}
	public ArrayList<Integer> getF1minus2diffs() {
		return f1minus2diffs;
	}

	public ArrayList<Integer> getF2minus1diffs() {
		return f2minus1diffs;
	}

	public ArrayList<String> getFile1minus2() {
		return file1minus2;
	}

	public ArrayList<String> getFile2minus1() {
		return file2minus1;
	}

	public Map<String, Integer> getFreq_file1minus2() {
		return freq_file1minus2;
	}

	public Map<String, Integer> getFreq_file2minus1() {
		return freq_file2minus1;
	}

	public Map<String, Integer> getFrequencyMap1() {
		return frequencyMap1;
	}

	public Map<String, Integer> getFrequencyMap2() {
		return frequencyMap2;
	}

	public Path getOutput() {
		return outputFile;
	}

	public Set<String> getSet1() {
		return set1;
	}

	public Set<String> getSet2() {
		return set2;
	}

	@Override
	public String toString() {
		// Output to CSV
		// Format: file1,file2,num_statements1,num_statements2,num_unique1,num_unique2
		// pres_1minus2,pres_2minus1,freq_1minus2sum,freq_2minus1sum,freq_1minus2_mean,freq_2minus1_mean
		// freq_1minus2_max, freq_2minus1_max, freq_1minus2_min, freq_2minus1_min,
		// freq_1minus2_median, freq_2minus1_median
		StringBuilder sb = new StringBuilder();
		sb.append(coverageFile1);
		sb.append(",");
		sb.append(coverageFile2);
		sb.append(",");
		sb.append(frequencyMap1.values().stream().mapToInt(i -> i).sum());
		sb.append(",");
		sb.append(frequencyMap2.values().stream().mapToInt(i -> i).sum());
		sb.append(",");
		sb.append(set1.size());
		sb.append(",");
		sb.append(set2.size());
		sb.append(",");
		sb.append(file1minus2.size());
		sb.append(",");
		sb.append(file2minus1.size());
		return sb.toString();
	}

}
