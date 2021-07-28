package edu.utdallas.amordahl.CoverageComparer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;

public class CoveragePairTask implements Runnable {

	private Path file1;
	private Path file2;
	private Path outputFile;
	
	// Static list of coverage records that we can access later, in order to compute suspiciousnesses.
	private static List<CoverageRecord> records = Collections.synchronizedList(new ArrayList<CoverageRecord>());
	
	public static List<CoverageRecord> getRecords() {
		return records;
	}

	public CoveragePairTask(String file1, String file2, String outputDir) {
		this.file1 = Paths.get(file1);
		this.file2 = Paths.get(file2);
		this.outputFile = this.computeOutputFileName(Paths.get(outputDir));
	}
	
	public CoveragePairTask(Path file1, Path file2, Path outputDir) {
		this.file1 = file1;
		this.file2 = file2;
		this.outputFile = this.computeOutputFileName(outputDir);
	}
	
	/**
	 * Compose the output JSON file name based off the first two paths.
	 * @param outputDir
	 * @return
	 */
	private Path computeOutputFileName(Path outputDir) {
		String f1 = this.file1.getFileName().toString();
		String f2 = this.file2.getFileName().toString();
		String f1NoExt = f1.split(".")[0];
		String f2NoExt = f2.split(".")[0];
		return outputDir.resolve(String.format("%s_%s.json", f1NoExt, f2NoExt));
		
	}
	
	@Override
	public void run() {
		CoverageRecord cr;
		try {
			cr = new CoverageRecord(file1, file2, this.outputFile);
			records.add(cr);
			System.out.println(cr.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
			System.out.println(String.format("Could not run CoverageRecord construction. Args are %s, %s, %s",
					this.file1.toString(), this.file2.toString(), this.outputFile.toString()));
		}
		
	}

}
