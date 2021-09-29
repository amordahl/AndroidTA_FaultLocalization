package edu.utdallas.amordahl.coverage_instrumenter.phase2;

import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.objectweb.asm.Opcodes.ASM4;

import java.util.Set;

public class LoggerClassAdapter extends ClassVisitor {
	
	private static Logger logger = LoggerFactory.getLogger(LoggerClassAdapter.class);
	private Set<LineCoverageRecord> covered;
	
    public LoggerClassAdapter(ClassVisitor cv, String className, Set<LineCoverageRecord> covered) {
        super(ASM4, cv); // TODO: what does this constant do?
        logger.debug("In LoggerClassAdapter for " + className);
        LoggerClassAdapter.className = className;
        this.covered = covered;
    }

    private static String className;
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	logger.debug("In visitMethod");
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new LoggerMethodVisitor(mv, LoggerClassAdapter.className, covered);
        }
        return mv;
    }
}