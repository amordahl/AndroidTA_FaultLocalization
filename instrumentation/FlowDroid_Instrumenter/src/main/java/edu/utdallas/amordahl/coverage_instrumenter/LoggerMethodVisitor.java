package edu.utdallas.amordahl.coverage_instrumenter;
import org.objectweb.asm.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.LoggerHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;

/**
 * Class to implement our instrumentation. With the exception of visitMethodInsn, everything
 * is copied from Ali Ghanbari's ObjSim, and the algorithm for writing instrumentation was made with
 * significant help and guidance from him. Thanks, Ali!
 */
public class LoggerMethodVisitor extends MethodVisitor {

    private static Logger logger = LoggerFactory.getLogger(LoggerMethodVisitor.class);
    private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");
    private static String LOG_TRACKER = Type.getInternalName(LoggerHelper.class);
    private static int lineNumber;
    private String name;
    
    public LoggerMethodVisitor(MethodVisitor methodVisitor, String name) {
        super(ASM4, methodVisitor);
        this.name = name;
        mv = methodVisitor;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
    	mv.visitLdcInsn(line);
    	mv.visitLdcInsn(this.name);
    	mv.visitMethodInsn(INVOKESTATIC, LOG_TRACKER, "logCoverageInfo", "(ILjava/lang/String;)V", false);
        super.visitLineNumber(line, start);
    }

//    @Override
//    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
//    	// Prevents infinite loops, by preventing instrumentation of calls that are to my
//    	//  own logging facilities.
//    	if (name.contains("logObjArray")) {
//    		logger.debug("Skipping instrumenting a call to logObjArray. Owner is " + owner);
//    		super.visitMethodInsn(opcode, owner, name, desc, itf);
//    		return;
//    	}
////    	if (owner.startsWith("java/") || owner.startsWith("org/jgrapht")) {
////    	    logger.debug("Skipping a call to library methods.");
////    	    super.visitMethodInsn(opcode, owner, name, desc, itf);
////    	    return;
////        }
//    	if (!owner.contains("soot")) {
//    		logger.info("Skipping call: " + owner + ": " + name);
//            super.visitMethodInsn(opcode, owner, name, desc, itf);
//            return;
//    	}
//    	if (owner.startsWith("java")) {
//            logger.debug("Skipping library call: " + owner + ": " + name);
//            super.visitMethodInsn(opcode, owner, name, desc, itf);
//            return;
//    	}
//    	if (owner.contains("java/lang") && owner.contains("Error")) {
//            logger.debug("Skipping exception: " + owner + ": " + name);
//            super.visitMethodInsn(opcode, owner, name, desc, itf);
//            return;
//        }
//    	if (owner.contains("java/lang") && name.contains("valueOf")) {
//            logger.debug("Skipping value call: " + owner + ": " + name);
//            super.visitMethodInsn(opcode, owner, name, desc, itf);
//            return;
//        }
//    	if (owner.contains("chain") || owner.contains("Chain")) {
//    	    logger.warn("Skipping instrumenting " + owner + "'s " + name + " method, to prevent infinite loops.");
//    	    super.visitMethodInsn(opcode, owner, name, desc, itf);
//    	    return;
//        }
//
//    	// Check if we've already logged this.
//    	logger.info("Now logging method " + owner + ": " + name);
//    	Type[] parameters = Type.getArgumentTypes(desc);
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        for (Type p : parameters) {
//            sb.append(p);
//            sb.append(" ");
//        }
//        sb.append("]");
//
//        logger.debug("Parameters are " + sb.toString());
//
//        // TODO: Handle longs.
//        for (Type p: parameters) {
//            if (p.getClassName().contains("long") || p.getClassName().contains("double")) {
//                logger.warn("Skipping logging call to " + name + " because it has long or double parameters.");
//                super.visitMethodInsn(opcode, owner, name, desc, itf);
//                return;
//            }
//            else {
//                logger.debug("Type " + p.getClassName() + " is not a long or double.");
//            }
//        }
//        if (parameters.length > 0) {
//            // Create array.
//            logger.debug("Creating an array of size " + parameters.length);
//            createArray(OBJECT_TYPE, parameters.length);
//            logger.debug("Array created!");
//            // Current stack state is PARAMETERS, ARRAYREF
//            logger.debug("Made it to the first log.");
//            for (int i = parameters.length - 1; i >= 0; i--) {
//                // 1. Duplicate the arrayref so it's not lost.
//                logger.debug("In iteration " + i + " of the for loop.");
//                super.visitInsn(DUP_X1);
//                // Now, the stack state is PARAMTERS, ARRAYREF, LAST_PARAM, ARRAYREF
//                super.visitInsn(SWAP);
//                // Now, the stack state is PARAMETERS, ARRAYREF, ARRAYREF, LAST_PARAM
//
//                // 2. Push the array index.
//                logger.debug("Pushing integer " + i);
//                pushInteger(i);
//                // Now, the stack state is PARAMETERS, ARRAYREF, ARRAYREF, LAST_PARAM, INDEX
//                super.visitInsn(SWAP);
//                // Now, the stack state is PARAMETERS, ARRAYREF, ARRAYREF, INDEX, LAST_PARAM
//
//                // If the type is primitive, this will box it into a reference type.
//                logger.debug("Boxing parameter " + i + " of type " + parameters[i].toString());
//                this.box(parameters[i]);
//
//                // Store in the array.
//                super.visitInsn(AASTORE);
//                // Stack state is PARAMETERS, ARRAYREF
//            }
//            logger.debug("Made it past the first for loop.");
//            // Now, the stack has an arrayref on it and no references.
//            // Next, we need to log.
//            super.visitInsn(DUP);
//            // Stack state is ..., ARRAYREF, ARRAYREF
////            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
////            super.visitLdcInsn("TEST");
////            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
////                    "(Ljava/lang/String;)V", false); // consumes out and ARRAYREF
//            // Stack state is ..., ARRAYREF, ARRAYREF, out
//            //super.visitInsn(SWAP);
//            // Stack state is ..., ARRAYREF, out, ARRAYREF
//            super.visitLdcInsn(String.format("%s %d %s", owner, lineNumber, name));
//            super.visitMethodInsn(INVOKESTATIC, LOG_TRACKER, "logObjArray",
//                    "([Ljava/lang/Object;Ljava/lang/String;)V", false); // consumes out and ARRAYREF
//            logger.debug("Visited instruction to log object array.");
//            // Stack state is ..., ARRAYREF
//            // Now, we need to unpack everything from the array and put it in.
//            logger.debug("Now putting everything back.");
//            for (int i = 0; i < parameters.length; i++) {
//                logger.debug("In iteration " + i + " of the second for loop.");
//                // Stack: PARAMS, ARRAYREF
//                super.visitInsn(DUP);
//                // Stack: PARAMS, ARRAYREF, ARRAYREF
//                logger.debug("Pushing integer " + i);
//                pushInteger(i);
//                // Stack: PARAMS, ARRAYREF, ARRAYREF, INDEX
//                super.visitInsn(AALOAD);
//                // Stack: PARAMS, ARRAYREF, VALUE
//
//                unbox(parameters[i]);
//                // Stack: PARAMS, ARRAYREF, VALUE
//                switch (parameters[i].getSort()) {
//                    case Type.ARRAY:
//                    case Type.OBJECT:
//                    case Type.METHOD:
//                        super.visitTypeInsn(CHECKCAST, parameters[i].getInternalName());
//                }
//                //super.visitTypeInsn(CHECKCAST, parameters[i].getClassName());
//                super.visitInsn(SWAP);
//                // Stack: PARAMS, VALUE, ARRAYREF
//            }
//            logger.debug("Made it past the last for loop.");
//            super.visitInsn(POP);
//            // Stack: PARAMS, VALUE
//            logger.debug("Made it to end!");
//        }
//        super.visitMethodInsn(opcode, owner, name, desc, itf);
//    }

    private void pushInteger(final int intValue) {
        // Copy of Ali Ghanbari's pushInteger function.;
        if (intValue <= 5) {
            mv.visitInsn(3 + intValue);
        } else if (intValue <= 127) {
            mv.visitIntInsn(BIPUSH, intValue);
        } else if (intValue <= 32767) {
            mv.visitIntInsn(SIPUSH, intValue);
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
        logger.debug("Trying to box type " + type.toString());
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
