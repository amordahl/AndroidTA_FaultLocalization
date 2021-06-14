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
 * A versatile class file transformer that adds code to record system state at the exit
 * point(s) of a patched method.
 * !Internal use only!
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PrimaryTransformer implements ClassFileTransformer {

    private static Logger logger = LoggerFactory.getLogger(PrimaryTransformer.class);
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (!className.contains("soot")) { /* TODO: Filter appropriately. */
            logger.info("Not instrumenting the class " + className);
            return null; // no transformation
        }

    	//System.out.println("In the transform method.");
        final ClassReader classReader = new ClassReader(classfileBuffer);
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        final ClassVisitor classVisitor = new LoggerClassAdapter(classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

        Path outputDirectory = Paths.get("/home/asm140830/Documents/git/AndroidTA_FaultLocalization/instrumentation/FlowDroid_Instrumenter/outputs");
        //System.out.println("Output directory is " + outputDirectory.toString());
        // Write to output so we can inspect outputs.

        //System.out.println("Class being redefined is " + className.replace("/", "_"));
        Path outputFile = outputDirectory.resolve(className.replace("/", "_") + ".class");
        //System.out.println("Output file is" + outputFile.toString());
        try {
            Files.write(outputFile, classWriter.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classWriter.toByteArray();
    }
}