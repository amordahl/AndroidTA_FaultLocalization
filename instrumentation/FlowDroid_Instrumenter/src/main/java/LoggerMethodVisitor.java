import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;

/**
 * Class to implement our instrumentation. With the exception of visitMethodInsn, everything
 * is copied from Ali Ghanbari's ObjSim, and the algorithm for writing instrumentation was made with
 * significant help and guidance from him. Thanks, Ali!
 */
public class LoggerMethodVisitor extends MethodVisitor {

    private static Logger logger = LoggerFactory.getLogger(LoggerMethodVisitor.class);
    private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

    public LoggerMethodVisitor(MethodVisitor methodVisitor) {
        super(ASM4, methodVisitor);
        mv = methodVisitor;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        Type[] parameters = Type.getArgumentTypes(desc);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Type p : parameters) {
            sb.append(p);
            sb.append(" ");
        }
        sb.append("]");

        logger.info("Parameters are " + sb.toString());
        if (parameters.length > 0) {
            // Create array.
            logger.info("Creating an array of size " + parameters.length);
            createArray(OBJECT_TYPE, parameters.length);
            logger.info("Array created!");
            //System.out.println("Created array with size " + parameters.length);
            int arrayIndex = 0;
            int paramIndex = 0;
            // Current stack state is PARAMETERS, ARRAYREF
            logger.debug("Made it to the first log.");
            for (int i = 0; i < parameters.length; i++) {
                // 1. Duplicate the arrayref so it's not lost.
                logger.debug("In iteration " + i + " of the for loop.");
                super.visitInsn(DUP_X1);
                // Now, the stack state is PARAMTERS, ARRAYREF, LAST_PARAM, ARRAYREF
                super.visitInsn(SWAP);
                // Now, the stack state is PARAMETERS, ARRAYREF, ARRAYREF, LAST_PARAM

                // 2. Push the array index.
                logger.debug("Pushing integer " + i);
                pushInteger(i);
                // Now, the stack state is PARAMETERS, ARRAYREF, ARRAYREF, LAST_PARAM, INDEX
                super.visitInsn(SWAP);
                // Now, the stack state is PARAMETERS, ARRAYREF, ARRAYREF, INDEX, LAST_PARAM

                // If the type is primitive, this will box it into a reference type.
                this.box(parameters[parameters.length - i]);

                // Store in the array.
                super.visitInsn(AASTORE);
                // Stack state is PARAMETERS, ARRAYREF
            }
            logger.debug("Made it past the first for loop.");
            // Now, the stack has an arrayref on it and no references.
            // Next, we need to log.
            super.visitInsn(DUP);
            // Stack state is ..., ARRAYREF, ARRAYREF
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitLdcInsn("TEST");
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                    "(Ljava/lang/String;)V", false); // consumes out and ARRAYREF
            // Stack state is ..., ARRAYREF, ARRAYREF, out
            //super.visitInsn(SWAP);
            // Stack state is ..., ARRAYREF, out, ARRAYREF

            super.visitMethodInsn(INVOKESTATIC, "edu/utdallas/amordahl/LoggerHelper", "logObjArray",
                    "([Ljava/lang/Object;)V", false); // consumes out and ARRAYREF
            // Stack state is ..., ARRAYREF
            // Now, we need to unpack everything from the array and put it in.
            for (int i = 0; i < parameters.length; i++) {
                // Stack: PARAMS, ARRAYREF
                super.visitInsn(DUP);
                // Stack: PARAMS, ARRAYREF, ARRAYREF
                pushInteger(i);
                // Stack: PARAMS, ARRAYREF, ARRAYREF, INDEX
                super.visitInsn(AALOAD);
                // Stack: PARAMS, ARRAYREF, VALUE
                unbox(parameters[i]);
                // Stack: PARAMS, ARRAYREF, VALUE
                super.visitInsn(SWAP);
                // Stack: PARAMS, VALUE, ARRAYREF
            }
            System.out.println("Made it past the last for loop.");
            super.visitInsn(POP);
            // Stack: PARAMS, VALUE
        }
        System.out.println("Made it to end!");
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    private void pushInteger(final int intValue) {
        // Copy of Ali Ghanbari's pushInteger function.
        if (intValue <= 5) {
            mv.visitInsn(3 + intValue);
        } else if (intValue <= 127) {
            mv.visitIntInsn(16, intValue);
        } else if (intValue <= 32767) {
            mv.visitIntInsn(17, intValue);
        } else {
            mv.visitLdcInsn(intValue);
        }
    }

    private void createArray(final Type type, final int intValue) {
        pushInteger(intValue);
        super.visitTypeInsn(ANEWARRAY, type.getInternalName());
    }

    private void pushWrappedParamValue(final Type paramType, final int paramIndex) {
        switch (paramType.getSort()) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.INT:
            case Type.SHORT:
                super.visitVarInsn(ILOAD, paramIndex);
                box(paramType);
                break;
            case Type.DOUBLE:
                super.visitVarInsn(DLOAD, paramIndex);
                box(paramType);
                break;
            case Type.FLOAT:
                super.visitVarInsn(FLOAD, paramIndex);
                box(paramType);
                break;
            case Type.LONG:
                super.visitVarInsn(LLOAD, paramIndex);
                box(paramType);
                break;
            default:
                super.visitVarInsn(ALOAD, paramIndex);
        }
    }

    private void box(final Type type) {
        // if type represents a primitive type a primitive-typed value is expected
        // on top of the stack.
        System.out.println("Trying to box type " + type.toString());
        switch (type.getSort()) {
            case Type.BOOLEAN:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean",
                        "valueOf", "(Z)Ljava/lang/Boolean;",
                        false);
                break;
            case Type.BYTE:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Byte",
                        "valueOf", "(B)Ljava/lang/Byte;",
                        false);
                break;
            case Type.CHAR:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Character",
                        "valueOf", "(C)Ljava/lang/Character;",
                        false);
                break;
            case Type.DOUBLE:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Double",
                        "valueOf", "(D)Ljava/lang/Double;",
                        false);
                break;
            case Type.FLOAT:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Float",
                        "valueOf", "(F)Ljava/lang/Float;",
                        false);
                break;
            case Type.INT:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
                        "valueOf", "(I)Ljava/lang/Integer;",
                        false);
                break;
            case Type.LONG:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Long",
                        "valueOf", "(J)Ljava/lang/Long;",
                        false);
                break;
            case Type.SHORT:
                super.visitMethodInsn(INVOKESTATIC, "java/lang/Short",
                        "valueOf", "(S)Ljava/lang/Short;",
                        false);
        }

        // if type of the item on top of the stack is not primitive, there
        // is no need to take further action.
    }

    private void unbox(final Type type) {
        // in case the type represents a primitive type, a wrapped object is expected
        // on top of the stack.
        switch (type.getSort()) {
            case Type.BOOLEAN:
                super.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean",
                        "booleanValue", "()Z",
                        false);
                break;
            case Type.BYTE:
                super.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte",
                        "byteValue", "()B",
                        false);
                break;
            case Type.CHAR:
                super.visitTypeInsn(CHECKCAST, "java/lang/Character");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character",
                        "charValue", "()C",
                        false);
                break;
            case Type.DOUBLE:
                super.visitTypeInsn(CHECKCAST, "java/lang/Double");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double",
                        "doubleValue", "()D",
                        false);
                break;
            case Type.FLOAT:
                super.visitTypeInsn(CHECKCAST, "java/lang/Float");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float",
                        "floatValue", "()F",
                        false);
                break;
            case Type.INT:
                super.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer",
                        "intValue", "()I",
                        false);
                break;
            case Type.LONG:
                super.visitTypeInsn(CHECKCAST, "java/lang/Long");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long",
                        "longValue", "()J",
                        false);
                break;
            case Type.SHORT:
                super.visitTypeInsn(CHECKCAST, "java/lang/Short");
                super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short",
                        "shortValue", "()S",
                        false);
        }
        // if the original type was not primitive, no wrapping has taken place,
        // so there is no need to take further action.
    }
}