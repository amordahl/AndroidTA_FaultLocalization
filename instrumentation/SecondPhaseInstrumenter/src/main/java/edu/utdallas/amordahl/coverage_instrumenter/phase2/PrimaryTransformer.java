package edu.utdallas.amordahl.coverage_instrumenter.phase2;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.FLPropReader;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Set;

/**
 * A versatile class file transformer that adds code to record system state at
 * the exit point(s) of a patched method. !Internal use only!
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PrimaryTransformer implements ClassFileTransformer {

	private static Logger logger = LoggerFactory.getLogger(PrimaryTransformer.class);
	private Set<LineCoverageRecord> covered;

	public PrimaryTransformer(Set<LineCoverageRecord> covered) {
		this.covered = covered;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) {
		// Filter out ModuleRefType because if we don't, then we get a duplicate class
		// definition error.
		if (!className.contains("soot")) {
			logger.debug("Not instrumenting the class " + className);
			return null; // no transformation
		}
		logger.debug("Instrumenting the class " + className);
		final ClassReader classReader = new ClassReader(classfileBuffer);
		final ClassWriterNoReflection classWriter = new ClassWriterNoReflection(ClassWriter.COMPUTE_FRAMES);
		final ClassVisitor classVisitor = new LoggerClassAdapter(new CheckClassAdapter(classWriter, true), className, covered);
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

		try {
			Path outputDirectory = Paths.get("/Users/austin/Desktop/class_outputs");
			logger.info("Output directory is " + outputDirectory.toString());
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