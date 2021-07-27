package edu.utdallas.amordahl.CoverageComparer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoverageRecord {

	@SuppressWarnings("unchecked")
	public CoverageRecord(Path coverageFile1, Path coverageFile2, Path outputFile) throws IOException {
		this.coverageFile1 = coverageFile1;
		this.coverageFile2 = coverageFile2;
		this.outputFile = outputFile;
		file1minus2 = new ArrayList<String>();
		file2minus1 = new ArrayList<String>();
		freq_file1minus2 = new HashMap<String, Integer>();
		freq_file2minus1 = new HashMap<String, Integer>();
		f1minus2diffs = new ArrayList<Integer>();
		f2minus1diffs = new ArrayList<Integer>();
		
		logger.info("Got arguments, now reading in file contents.");
		logger.info(String.format("c1 is %s, c2 is %s", this.coverageFile1, this.coverageFile2));
		// Read in file contents.
		fileContent1 = readFileContents(coverageFile1);
		fileContent2 = readFileContents(coverageFile2);

		logger.info("Computing frequency map.");
		// Compute frequencies
		frequencyMap1 = computeFrequencyMap(fileContent1);
		frequencyMap2 = computeFrequencyMap(fileContent2);

		// Compute sets
		logger.info("Computing sets.");
		set1 = convertListToSet(fileContent1);
		set2 = convertListToSet(fileContent2);

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

	public HashMap<String, Integer> getFreq_file1minus2() {
		return freq_file1minus2;
	}

	public HashMap<String, Integer> getFreq_file2minus1() {
		return freq_file2minus1;
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

	public HashSet<String> getSet1() {
		return set1;
	}

	public HashSet<String> getSet2() {
		return set2;
	}

	public HashMap<String, Integer> getFrequencyMap1() {
		return frequencyMap1;
	}

	public HashMap<String, Integer> getFrequencyMap2() {
		return frequencyMap2;
	}

	public ArrayList<String> getFileContent1() {
		return fileContent1;
	}

	public ArrayList<String> getFileContent2() {
		return fileContent2;
	}

	public Path getCoverageFile1() {
		return coverageFile1;
	}

	public Path getCoverageFile2() {
		return coverageFile2;
	}

	public Path getOutput() {
		return outputFile;
	}

	private JSONObject jo;
	private ArrayList<Integer> f1minus2diffs, f2minus1diffs;
	private ArrayList<String> file1minus2, file2minus1;
	private HashSet<String> set1, set2;

	private HashMap<String, Integer> frequencyMap1, frequencyMap2, freq_file1minus2, freq_file2minus1;

	private ArrayList<String> fileContent1, fileContent2;

	private Path coverageFile1, coverageFile2, outputFile;
	private static Logger logger = LoggerFactory.getLogger(CoverageRecord.class);

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
		sb.append(fileContent1.size());
		sb.append(",");
		sb.append(fileContent2.size());
		sb.append(",");
		sb.append(set1.size());
		sb.append(",");
		sb.append(set2.size());
		sb.append(",");
		sb.append(file1minus2.size());
		sb.append(",");
		sb.append(file2minus1.size());
//		sb.append(",");
//		sb.append(getSum(f1minus2diffs));
//		sb.append(",");
//		sb.append(getSum(f2minus1diffs));
//		sb.append(",");
//		sb.append(getMean(f1minus2diffs));
//		sb.append(",");
//		sb.append(getMean(f2minus1diffs));
//		sb.append(",");
//		sb.append(getMax(f1minus2diffs));
//		sb.append(",");
//		sb.append(getMax(f2minus1diffs));
//		sb.append(",");
//		sb.append(getMin(f1minus2diffs));
//		sb.append(",");
//		sb.append(getMin(f2minus1diffs));
//		sb.append(",");
//		sb.append(getMedian(f1minus2diffs));
//		sb.append(",");
//		sb.append(getMedian(f2minus1diffs));
		return sb.toString();
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
	private static Integer getMin(ArrayList<Integer> al) {
		Integer min = Integer.valueOf(Integer.MAX_VALUE);
		for (Integer i : al) {
			if (i < min)
				min = i;
		}
		return min;
	}

	private static Integer getSum(ArrayList<Integer> al) {
		Integer sum = Integer.valueOf(0);
		for (Integer i : al)
			sum += i;
		return sum;
	}

	@SuppressWarnings("unused")
	private static Double getMean(ArrayList<Integer> al) {
		return getSum(al).doubleValue() / al.size();
	}

	@SuppressWarnings("unused")
	protected static Double getMedian(ArrayList<Integer> al) {
		if (al == null || al.size() == 0) { return Double.NaN; }
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
			return (sorted.get((int) (Math.ceil((sorted.size() - 1) / 2.0))) + 
					sorted.get((int) (Math.floor((sorted.size() - 1) / 2.0)))) / 2.0;
		} else
			return Double.valueOf(sorted.get((sorted.size() - 1) / 2));
	}

	private static ArrayList<String> readFileContents(Path coverageFile) {
		ArrayList<String> fileContent = new ArrayList<String>();
		HashMap<Integer, String> mapping = new HashMap<Integer, String>();
		try (Scanner sc = new Scanner(coverageFile.toFile())) {
			while (sc.hasNext()) {
				String line = sc.next();
				if (line.contains("=")) {
					// Mapping line. Need to store map in hashmap.
					String[] tokens = line.split("=");
					mapping.put(Integer.valueOf(tokens[1]), tokens[0]);
				} else if (line.contains(":")) {
					String[] tokens = line.split(":");
					String actualName = mapping.get(Integer.valueOf(tokens[0]));
					fileContent.add(String.format("%s:%d", actualName, Integer.valueOf(tokens[1])));
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("Could not find file " + coverageFile);
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(2);
		}
		return fileContent;
	}

	private static HashMap<String, Integer> computeFrequencyMap(ArrayList<String> fileContents) {
		HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();

		for (String k : fileContents) {
			if (!frequencyMap.containsKey(k)) {
				frequencyMap.put(k, Integer.valueOf(0));
			}
			frequencyMap.put(k, frequencyMap.get(k) + 1);
		}

		return frequencyMap;

	}

	private static HashSet<String> convertListToSet(ArrayList<String> al) {
		HashSet<String> hs = new HashSet<String>();
		for (String k : al) {
			hs.add(k);
		}
		return hs;
	}

}
