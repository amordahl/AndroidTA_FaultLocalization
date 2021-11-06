package edu.utdallas.amordahl.multiphaseinstrumenter;
/*
 * #%L
 * objsim
 * %%
 * Copyright (C) 2020 The University of Texas at Dallas
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.multiphaseinstrumenter.util.LineCoverageRecord;
import edu.utdallas.amordahl.multiphaseinstrumenter.util.LineCoverageRecordFactory;
import edu.utdallas.amordahl.SupportedInstrumentations;
import edu.utdallas.amordahl.multiphaseinstrumenter.phase1.Phase1ClassVisitor;
import edu.utdallas.amordahl.multiphaseinstrumenter.phase2.Phase2ClassVisitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A versatile class file transformer that adds code to record system state at
 * the exit point(s) of a patched method. !Internal use only!
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PrimaryTransformer implements ClassFileTransformer {

	private static Logger logger = LoggerFactory.getLogger(PrimaryTransformer.class);
	private static String filter = "soot";
	
	public void setFilter(String filter) {
		PrimaryTransformer.filter = filter;
	}
	
	private static Set<LineCoverageRecord> covered = null;
	
	private static Set<LineCoverageRecord> getCovered() {
		if (covered == null) {
			covered = new HashSet<LineCoverageRecord>();
			try (FileReader fr = new FileReader(SettingsManager.getCoverageFile().toFile());
					BufferedReader br = new BufferedReader(fr)) {
				covered = br.lines().map(l -> LineCoverageRecordFactory.makeLineCoverageRecord(l))
						.filter(r -> r != null).collect(Collectors.toSet());
				logger.info(String.format("covered has %d lines.", covered.size()));
			} catch (FileNotFoundException fnfe) {
				logger.error(String.format("Could not find file %s", SettingsManager.getCoverageFile().toString()));
				System.exit(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(2);
			}
		}
		logger.debug("Size of covered is {}", covered.size());
		logger.debug("Covered is {}", covered.toString());
		return covered;
	}
	
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
//		// Filter out ModuleRefType because if we don't, then we get a duplicate class definition error.
		if (PrimaryTransformer.filter != "" && 
				!className.contains(PrimaryTransformer.filter)) {
			logger.debug("Not instrumenting the class " + className);
			return null; // no transformation
		}
		logger.debug("Instrumenting the class " + className);
		//logger.debug("SettingsManager's coverage file is {}", SettingsManager.getCoverageFile() == null ? " " : SettingsManager.getCoverageFile());
		final ClassReader classReader = new ClassReader(classfileBuffer);
		final ClassWriter classWriter = new ClassWriterNoReflection(ClassWriter.COMPUTE_FRAMES);
		ClassVisitor classVisitor = null;
		logger.debug("Created all three visitors.");
		// Create different class visitors depending on whether we have a coverage file or not.
		try {
			if (!SettingsManager.getInstrumentationType().contains(SupportedInstrumentations.COVERAGE)) {
			logger.debug("Creating a phase 2 instrumentation.");
			classVisitor = new TraceClassVisitor(new Phase2ClassVisitor(new CheckClassAdapter(classWriter, false), 
					className, PrimaryTransformer.getCovered()), null);
		} else {
			logger.debug("Creating a phase 1 instrumentation.");
			classVisitor = new Phase1ClassVisitor(classWriter, className);
		}
		} catch (Throwable ex) {
			logger.error("Something bad happened.", ex.fillInStackTrace());
		}
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
		
		try {
			Path outputDirectory = Paths.get("/home/austin/Desktop/class_outputs");
			if (Files.exists(outputDirectory, new LinkOption[] {})) {
				logger.debug("Output directory is " + outputDirectory.toString());
				// Write to output so we can inspect outputs.
				Path outputFile = outputDirectory.resolve(className.replace("/", "_") + ".class");
				// System.out.println("Output file is" + outputFile.toString());
				Files.write(outputFile, classWriter.toByteArray());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classWriter.toByteArray();
	}
}
