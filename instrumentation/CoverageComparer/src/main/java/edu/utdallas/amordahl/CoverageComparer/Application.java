package edu.utdallas.amordahl.CoverageComparer;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
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
import edu.utdallas.amordahl.CoverageComparer.coverage_tasks.processors.ICoverageTaskProcessor;
import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;
import edu.utdallas.amordahl.CoverageComparer.localizers.TarantulaLocalizer;
import edu.utdallas.amordahl.CoverageComparer.util.CoveredLine;
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
	
	// TODO: Make these into parameters.
	private ILocalizer<Path, CoveredLine> localizer = new TarantulaLocalizer<Path, CoveredLine>();
	private ICoverageTaskProcessor<Path, CoveredLine> processor = new BaselineInstlogProcessor();	
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
			CoverageTask ct = CoverageTaskReader.getCoverageTaskFromFile(Paths.get(s));
			PassedFailed<Path, CoveredLine> pf = processor.processCoverageTask(ct);
			Map<CoveredLine, Double> suspiciousness = localizer.computeSuspiciousness(pf.getPassed(), pf.getFailed());
			output(ct, suspiciousness);
		}
		
	}

	/**
	 * Specifies the way to output suspiciousness rankings on the command line.
	 * @param suspiciousness
	 */
	private void output(CoverageTask ct, Map<CoveredLine, Double> suspiciousness) {
		StringBuilder sb = new StringBuilder();
		sb.append("==================================");
		sb.append(String.format("Localizer: %s", this.localizer.getName()));
		sb.append(String.format("Processor: %s", this.processor.getName()));
		sb.append(String.format("Coverage task: %s", ct.getOriginalPath()));
		sb.append(String.format("Number of passed cases: %d", ct.getPassed().size()));
		sb.append(String.format("Number of failed test cases: %d", ct.getFailed().size()));
		sb.append(String.format("Preserved: %f", (Double)this.preserve));
		sb.append("");
		
		List<Entry<CoveredLine, Double>> sorted = new ArrayList<Entry<CoveredLine, Double>>(suspiciousness.entrySet());
		Collections.sort(sorted, ((e1, e2) -> e2.getValue().compareTo(e1.getValue()))); // sort in descending order.
		if (this.preserve != 1.0) {
			if (this.preserve > 1.0) {
				// Preserve top n results
				sorted = sorted.subList(0, preserve.intValue());
			} else {
				sorted = sorted.subList(0, (int)(sorted.size() * preserve));
			}
		}
		sorted.forEach(e -> sb.append(String.format("%s = %.3f", e.getKey().toString(), e.getValue())));
		
		sb.append("\n==================================\n");
		System.out.println(sb);
	}
}