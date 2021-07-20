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
		JCommander jcmd = JCommander.newBuilder().addObject(app).build();
		jcmd.parse(args);
		if (app.help) {
			jcmd.usage();
			return;
		}
		app.run();
	}
	
	@SuppressWarnings("unchecked")
	private void run() throws IOException {
		CoverageRecord cr = new CoverageRecord(c1, c2, output);
		System.out.println(cr.toString());
	}



}
