package edu.utdallas.amordahl.coverage_instrumenter;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM4;

public class LoggerClassAdapter extends ClassVisitor {
    public LoggerClassAdapter(ClassVisitor cv, String className) {
        super(ASM4, cv); // TODO: what does this constant do?
        System.out.println("In LoggerClassAdapter for " + className);
        LoggerClassAdapter.className = className;
    }

    private static String className;
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    	System.out.println("In visitMethod");
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new LoggerMethodVisitor(mv, LoggerClassAdapter.className);
        }
        return mv;
    }
}