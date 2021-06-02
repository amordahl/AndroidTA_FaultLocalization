import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM4;

public class LoggerClassAdapter extends ClassVisitor {
    public LoggerClassAdapter(ClassVisitor cv) {
        super(ASM4, cv); // TODO: what does this constant do?
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new LoggerMethodVisitor(mv);
        }
        return mv;
    }
}