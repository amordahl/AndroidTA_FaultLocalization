package edu.utdallas.amordahl.CoverageComparer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Application {

	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Parameter(names = {"-c1", "--coveragelog1"}, description = "The first coverage log.",
			required = true)
	protected String c1;
	
	@Parameter(names = {"-c2", "--coveragelog2"}, description = "The second coverage log.",
			required = true)
	protected String c2;
	
	@Parameter(names = "--help", help = true)
	protected boolean help;
	
	@Parameter(names = {"-o", "--output"}, 
			description = "If supplied, the application will write the " +
	"set differences to a JSON file.")
	protected String output;
	
	public static void main(String[] args) throws IOException {
		Application app = new Application();
		JCommander.newBuilder().addObject(app).build().parse(args);
		app.run();
	}
	
	@SuppressWarnings("unchecked")
	private void run() throws IOException {
		logger.info("Got arguments, now reading in file contents.");
		ArrayList<String> fc1 = readFileContents(c1);
		ArrayList<String> fc2 = readFileContents(c2);

		logger.info("Computing frequency map.");
		// Compute frequencies
		HashMap<String, Integer> fm1, fm2;
		fm1 = computeFrequencyMap(fc1);
		fm2 = computeFrequencyMap(fc2);

		// Compute sets
		logger.info("Computing sets.");
		HashSet<String> set1, set2;
		set1 = convertListToSet(fc1);
		set2 = convertListToSet(fc2);

		// Now, compute the differences.
		JSONObject jo = new JSONObject();
		jo.put("File1", c1);
		jo.put("File2", c2);
		JSONArray file1minus2 = new JSONArray();
		for (String s: set1) {
			if (!set2.contains(s)) {
				file1minus2.add(s.replace("/", "_"));
			}
		}
		JSONArray file2minus1 = new JSONArray();
		for (String s: set2) {
			if (!set1.contains(s)) {
				file2minus1.add(s.replace("/", "_"));
			}
		}

		jo.put("Presence_File1MinusFile2", file1minus2);
		jo.put("Presense_File2MinusFile1", file2minus1);

		ArrayList<Integer> f1minus2diffs = new ArrayList<Integer>();
		JSONObject freq_file1minus2 = new JSONObject();
		for (String k: fm1.keySet()) {
			if (!fm2.containsKey(k)) {
				freq_file1minus2.put(k.replace(",","_"), fm1.get(k));
				f1minus2diffs.add(fm1.get(k));
			}
			else if (fm1.get(k) > fm2.get(k)) {
				freq_file1minus2.put(k.replace(",","_"), fm1.get(k) - fm2.get(k));
				f1minus2diffs.add(fm1.get(k) - fm2.get(k));
			}
		}

		ArrayList<Integer> f2minus1diffs = new ArrayList<Integer>();
		JSONObject freq_file2minus1 = new JSONObject();
		for (String k: fm2.keySet()) {
			if (!fm1.containsKey(k)) {
				freq_file2minus1.put(k.replace(",","_"), fm2.get(k));
				f2minus1diffs.add(fm2.get(k));
			}
			else if (fm2.get(k) > fm1.get(k)) {
				freq_file2minus1.put(k.replace(",", "_"), fm2.get(k) - fm1.get(k));
				f2minus1diffs.add(fm2.get(k) - fm1.get(k));
			}
		}

		jo.put("Frequency_File1MinusFile2", freq_file1minus2);
		jo.put("Frequency_File2MinusFile1", freq_file2minus1);

		if (output != null) {
			try (FileWriter fw = new FileWriter(output)) {
				fw.write(jo.toJSONString());
			}
		}

		// Output to CSV
		// Format: file1,file2,num_statements1,num_statements2,num_unique1,num_unique2
		// pres_1minus2,pres_2minus1,freq_1minus2sum,freq_2minus1sum,freq_1minus2_mean,freq_2minus1_mean
		// freq_1minus2_max, freq_2minus1_max, freq_1minus2_min, freq_2minus1_min,
		// freq_1minus2_
		StringBuilder sb = new StringBuilder();
		sb.append(c1); sb.append(","); sb.append(c2); sb.append(",");
		sb.append(fc1.size()); sb.append(","); sb.append(fc2.size()); sb.append(",");
		sb.append(set1.size()); sb.append(","); sb.append(set2.size()); sb.append(",");
		sb.append(file1minus2.size()); sb.append(","); sb.append(file2minus1.size()); sb.append(",");
		sb.append(getSum(f1minus2diffs)); sb.append(","); sb.append(getSum(f2minus1diffs)); sb.append(",");
		sb.append(getMean(f1minus2diffs)); sb.append(","); sb.append(getMean(f2minus1diffs)); sb.append(",");
		sb.append(getMax(f1minus2diffs)); sb.append(","); sb.append(getMax(f2minus1diffs)); sb.append(",");
		sb.append(getMin(f1minus2diffs)); sb.append(","); sb.append(getMin(f2minus1diffs)); sb.append(",");
		sb.append(getMedian(f1minus2diffs)); sb.append(","); sb.append(getMedian(f2minus1diffs));
		System.out.println(sb.toString());

	}
	
	private static Integer getMax(ArrayList<Integer> al) {
		Integer max = Integer.valueOf(-1);
		for (Integer i: al) {
			if (i > max) max = i;
		}
		return max;
	}
	
	private static Integer getMin(ArrayList<Integer> al) {
		Integer min = Integer.valueOf(Integer.MAX_VALUE);
		for (Integer i: al) {
			if (i < min) min = i;
		}
		return min;
	}
	
	private static Integer getSum(ArrayList<Integer> al) {
		Integer sum = Integer.valueOf(0);
		for (Integer i: al) sum += i;
		return sum;
	}
	
	private static Double getMean(ArrayList<Integer> al) {
		return getSum(al).doubleValue() / al.size();
	}
	
	private static Double getMedian(ArrayList<Integer> al) {
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
			return sorted.get(sorted.size() / 2) + sorted.get(sorted.size() / 2) / Double.valueOf(2.0);
		}
		else return Double.valueOf(sorted.get((sorted.size()+1)/2));
	}

	private static ArrayList<String> readFileContents(String fileName) {
		ArrayList<String> fileContent = new ArrayList<String>();
		HashMap<Integer, String> mapping = new HashMap<Integer, String>();
		try (Scanner sc = new Scanner(new File(fileName))) {
			while (sc.hasNext()) {
				String line = sc.next();
				if (line.contains("=")) {
					// Mapping line. Need to store map in hashmap.
					String[] tokens = line.split("=");
					mapping.put(Integer.valueOf(tokens[1]), tokens[0]);
				} else if (line.contains(":")) {
					String[] tokens = line.split(":");
					String actualName = mapping.get(Integer.valueOf(tokens[0]));
					fileContent.add(String.format("%s:%d", actualName, tokens[1]));
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("Could not find file " + fileName);
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
