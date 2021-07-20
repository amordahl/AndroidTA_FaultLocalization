package edu.utdallas.amordahl.CoverageComparer;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class CoverageRecordTests {
	
	private static CoverageRecord cr;
	
	private static String file1 = "src/test/resources/sample_coverage_data1.txt";
	private static String file2 = "src/test/resources/sample_coverage_data2.txt";
	@BeforeClass
	public static void setup() throws IOException {
		cr = new CoverageRecord(file1, file2, null);
	}
	
	@Test
	public void testFileNames() {
		assertTrue(cr.getCoverageFile1() == file1 &&
				cr.getCoverageFile2() == file2);
	}
	
	@Test
	public void testFileContentLength() {
		assertTrue(cr.getFileContent1().size() == 11 &&
				cr.getFileContent2().size() == 12);
	}
	
	@Test
	public void testDifferences() {
		assertTrue(
				cr.getFile1minus2().size() == 2 &&
				cr.getFile1minus2().contains("edu.amordahl.test1.SupportClass:3") &&
				cr.getFile1minus2().contains("edu.amordahl.test1.SupportClass:4") &&
				cr.getFile2minus1().size() == 2 &&
				cr.getFile2minus1().contains("edu.amordahl.test1.SupportClass:5") &&
				cr.getFile2minus1().contains("edu.amordahl.test1.SupportClass:6"));
	}
	
	@Test
	public void testMedianOdd() {
		ArrayList<Integer> i1 = new ArrayList<Integer>();
		i1.add(4); i1.add(3); i1.add(1); i1.add(2); i1.add(5);
		assertTrue(CoverageRecord.getMedian(i1) == 3.0);
	}
	
	@Test
	public void testMedianEven() {
		ArrayList<Integer> i1 = new ArrayList<Integer>();
		i1.add(4); i1.add(3); i1.add(1); i1.add(2);
		assertTrue(CoverageRecord.getMedian(i1) == 2.5);
	}
	
}
