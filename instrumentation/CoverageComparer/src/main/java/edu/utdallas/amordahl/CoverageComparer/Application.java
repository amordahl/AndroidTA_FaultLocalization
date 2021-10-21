package edu.utdallas.amordahl.CoverageComparer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTask;
import edu.utdallas.amordahl.CoverageComparer.coverageTasks.CoverageTaskReader;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.BaselineInstlogProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.DataStructureContentLogProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.AbstractPostProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.postprocessors.IdentityPostProcessor;
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.AbstractCoverageTaskProcessor;
import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;
import edu.utdallas.amordahl.CoverageComparer.localizers.TarantulaLocalizer;
import edu.utdallas.amordahl.CoverageComparer.util.PassedFailed;

public class Application {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	@Parameter(
			names = { "-c", "--coveragetasks"},
			description = "The coverage files to compute coverage information for.",
			variableArity = true,
			required = true
	)
	private List<String> coverageTasks;
	
	@Parameter(
			names = {"-h", "--help"},
			description = "Print out usage information."
	)
	private boolean help;
	
	@Parameter(
			names = {"-p", "--preserve"},
			description = "The number of records to deserve. Numbers from (0, 1] indicate a "
					+ "percentage of values to keep (e.g., 0.8 will preserve the top 80% of results, and "
					+ "1.0 will preserve all of them. Integers greater than 1 indicate an absolute number of "
					+ "records to keep."
	)
	private Double preserve = 1.0;
	
	@Parameter(
			names = {"--phase2"},
			description = "If enabled, treat results as data structure logs.")
	private boolean phase2 = false;
	
	// TODO: Make these into parameters.
	private ILocalizer<Object> localizer = new TarantulaLocalizer<Object>();
	private AbstractCoverageTaskProcessor<?> processor = 
			phase2 ? new DataStructureContentLogProcessor() : new BaselineInstlogProcessor();	
	private AbstractPostProcessor<Object> app = new IdentityPostProcessor<Object>();
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
		app.run();
	}
	
	private void run() {
		for (String s: this.coverageTasks) {
			// Read in coverage file, which details the passed and failed test cases.
			System.out.println(String.format("Reading in coverage file %s", Paths.get(s)));
			CoverageTask ct = CoverageTaskReader.getCoverageTaskFromFile(Paths.get(s));
			
			// Process the coverage task -- actually read in the files and produce a PassedFailed object.
			System.out.println(String.format("Now processing coverage task %s", Paths.get(s)));
			@SuppressWarnings("unchecked")
			PassedFailed<Object> pf = (PassedFailed<Object>) processor.processCoverageTask(ct);
			
			// Post process the PassedFailed object (e.g., only pass the delta to the localizer.
			System.out.println(String.format("Now post-processing task %s", Paths.get(s)));
			pf = app.postProcess(pf);
			
			// Localize the results.
			System.out.println(String.format("Computing %s suspiciousness for task %s", localizer.getName(), Paths.get(s)));
			Map<Object, Double> suspiciousness = localizer.computeSuspiciousness(pf);
			
			// Output the result.
			output(ct, suspiciousness);
		}
		
	}

	/**
	 * Specifies the way to output suspiciousness rankings on the command line.
	 * @param suspiciousness
	 */
	private void output(CoverageTask ct, Map<?, Double> suspiciousness) {
		StringBuilder sb = new StringBuilder();
		sb.append("==================================\n");
		sb.append(String.format("Localizer: %s\n", this.localizer.getName()));
		sb.append(String.format("Processor: %s\n", this.processor.getName()));
		sb.append(String.format("Coverage task: %s\n", ct.getOriginalPath()));
		sb.append(String.format("Number of passed cases: %d\n", ct.getPassed().size()));
		sb.append(String.format("Number of failed test cases: %d\n", ct.getFailed().size()));
		sb.append(String.format("Preserved: %f\n", (Double)this.preserve));
		sb.append("\n");
		
		List<Entry<?, Double>> sorted = new ArrayList<Entry<?, Double>>(suspiciousness.entrySet());
		Collections.sort(sorted, ((e1, e2) -> e2.getValue().compareTo(e1.getValue()))); // sort in descending order.
		if (this.preserve != 1.0) {
			if (this.preserve > 1.0) {
				// Preserve top n results
				sorted = sorted.subList(0, preserve.intValue());
			} else {
				sorted = sorted.subList(0, (int)(sorted.size() * preserve));
			}
		}
		sorted.forEach(e -> sb.append(String.format("%s = %.3f\n", e.getKey().toString(), e.getValue())));
		
		sb.append("\n==================================\n");
		System.out.println(sb);
	}
}
