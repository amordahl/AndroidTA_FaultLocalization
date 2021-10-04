package edu.utdallas.amordahl.multiphaseinstrumenter.phase1;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.objectweb.asm.Opcodes.ASM4;

public class Phase1ClassVisitor extends ClassVisitor {
	
	private static Logger logger = LoggerFactory.getLogger(Phase1ClassVisitor.class);
	
    public Phase1ClassVisitor(ClassVisitor cv, String className) {
        super(ASM4, cv); // TODO: what does this constant do?
        logger.debug("In LoggerClassAdapter for " + className);
        Phase1ClassVisitor.className = className;
    }

    private static String className;
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	logger.debug("In visitMethod");
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new Phase1MethodVisitor(mv, Phase1ClassVisitor.className);
        }
        return mv;
    }
}