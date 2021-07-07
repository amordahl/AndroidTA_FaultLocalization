package edu.utdallas.amordahl.coverage_instrumenter;
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

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.LoggerHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * A versatile class file transformer that adds code to record system state at
 * the exit point(s) of a patched method. !Internal use only!
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PrimaryTransformer implements ClassFileTransformer {

	private static Logger logger = LoggerFactory.getLogger(PrimaryTransformer.class);

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		// Filter out ModuleRefType because if we don't, then we get a duplicate class definition error.
		if (!className.contains("soot") || className.contains("ModuleRefType")) {
			logger.info("Not instrumenting the class " + className);
			return null; // no transformation
		}

		final ClassReader classReader = new ClassReader(classfileBuffer);
		final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		final ClassVisitor classVisitor = new LoggerClassAdapter(classWriter, className);
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

		try {
			Path outputDirectory = new PropReader().getOutputFile();
			// Write to output so we can inspect outputs.
			Path outputFile = outputDirectory.resolve(className.replace("/", "_") + ".class");
			// System.out.println("Output file is" + outputFile.toString());
			Files.write(outputFile, classWriter.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classWriter.toByteArray();
	}
}