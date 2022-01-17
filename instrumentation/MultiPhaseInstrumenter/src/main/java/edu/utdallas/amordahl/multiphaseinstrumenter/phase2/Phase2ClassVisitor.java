package edu.utdallas.amordahl.multiphaseinstrumenter.phase2;

import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.multiphaseinstrumenter.util.LineCoverageRecord;

import static org.objectweb.asm.Opcodes.ASM4;

import java.util.Set;

public class Phase2ClassVisitor extends ClassVisitor {
	
	private static Logger logger = LoggerFactory.getLogger(Phase2ClassVisitor.class);
	private Set<LineCoverageRecord> covered;
	
    public Phase2ClassVisitor(ClassVisitor cv, String className, Set<LineCoverageRecord> covered) {
        super(ASM4, cv); // TODO: what does this constant do?
        Phase2ClassVisitor.className = className;
        this.covered = covered;
    }

    private static String className;
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	logger.debug("In visitMethod");
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new Phase2MethodVisitor(mv, Phase2ClassVisitor.className, covered);
        }
        return mv;
    }
}