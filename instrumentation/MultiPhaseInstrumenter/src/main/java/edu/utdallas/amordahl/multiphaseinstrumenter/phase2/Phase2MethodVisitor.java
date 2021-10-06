package edu.utdallas.amordahl.multiphaseinstrumenter.phase2;

import org.objectweb.asm.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utdallas.amordahl.LoggerHelper;
import edu.utdallas.amordahl.SupportedInstrumentations;
import edu.utdallas.amordahl.multiphaseinstrumenter.SettingsManager;
import edu.utdallas.amordahl.multiphaseinstrumenter.util.LineCoverageRecord;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.Frame;

import static org.objectweb.asm.Opcodes.*;

import java.io.IOException;
import java.util.Set;

/**
 * Class to implement our instrumentation. With the exception of
 * visitMethodInsn, everything is copied from Ali Ghanbari's ObjSim, and the
 * algorithm for writing instrumentation was made with significant help and
 * guidance from him. Thanks, Ali!
 */
public class Phase2MethodVisitor extends MethodVisitor {

	private static Logger logger = LoggerFactory.getLogger(Phase2MethodVisitor.class);
	private static String LOG_TRACKER = Type.getInternalName(LoggerHelper.class);
	private static int lineNumber;
	private static int dataStructureIndex;
	private String name;
	private Set<LineCoverageRecord> covered;
	private boolean toggle;

	public Phase2MethodVisitor(MethodVisitor methodVisitor, String name, Set<LineCoverageRecord> covered) {
		super(ASM4, methodVisitor);
		this.name = name;
		this.mv = methodVisitor;
		this.covered = covered;
		this.toggle = false;
	}

//	@Override
//	public void visitMaxs(int maxStack, int maxLocals) {
//		// TODO Auto-generated method stub
//		super.visitMaxs(maxStack, maxLocals);
//	}

	@Override
	public void visitLineNumber(int line, Label start) {
		Phase2MethodVisitor.lineNumber = line;
		Phase2MethodVisitor.dataStructureIndex = 0;
		LineCoverageRecord lr = new LineCoverageRecord(this.name, Integer.valueOf(line));
		if (covered.size() == 0 || 
				covered.contains(new LineCoverageRecord(this.name, Integer.valueOf(line)))) {
			logger.debug(String.format("%s was in coverage record.", lr.toString()));
			toggle = true;
		} else {
			toggle = false;
		}
		super.visitLineNumber(line, start);
	}
	
	
	
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		// TODO Auto-generated method stub
		if (toggle && desc.startsWith("L")) {
			logger.debug("Description for instruction on line {}:{} is {}", this.name, Phase2MethodVisitor.lineNumber, desc);
			switch (opcode) {
			case GETFIELD:
			case GETSTATIC:
				logger.debug(String.format("GET instruction found on %s:%d", this.name, Phase2MethodVisitor.lineNumber));
				// getfield and getstatic do not use a stack operand:
				// the instruction contain the owner, name of the field/static and the description.
				// thus, the algorithm is as follows:
				
				// 1. Call the instruction so the data structure is on the stack.
				super.visitFieldInsn(opcode, owner, name, desc);
				
				// STACK STATE: OBJ, ...
				// 2. Then log the data structure.
				logDataStructure(desc);
				break;
			case PUTFIELD:
			case PUTSTATIC:
				logger.debug(String.format("PUT instruction found on %s:%d", this.name, Phase2MethodVisitor.lineNumber));
				logDataStructure(desc);
				super.visitFieldInsn(opcode, owner, name, desc);
				// Very similar algorithm, except that we do the above steps before we call the put instruction.
				break;
			}
		}
		else {
		super.visitFieldInsn(opcode, owner, name, desc);
		}
	}

	private void logDataStructure(String description) {
		// STACK STATE: ..., OBJ
		// First, clone object reference.
		for (SupportedInstrumentations s: SettingsManager.getInstrumentationType()) {
			super.visitInsn(DUP); // ..., OBJ, OBJ
			super.visitLdcInsn(this.name); // ..., OBJ, OBJ, name
			super.visitLdcInsn(Phase2MethodVisitor.lineNumber); // ..., OBJ, OBJ, name, linum
			super.visitLdcInsn(Phase2MethodVisitor.dataStructureIndex++); // ..., OBJ, OBJ, name, linum, index 
			super.visitLdcInsn(description);
			super.visitLdcInsn(s.toString()); // ..., OBJ, OBJ, name, linum, index, type
			super.visitMethodInsn(INVOKESTATIC, LOG_TRACKER, "logDataStructure", 
					"(Ljava/lang/Object;Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;)V", false); // ..., OBJ
		}	
		
	}

	private void printSomething(String toPrint) {
		super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		super.visitLdcInsn(toPrint);
		super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false); 
	}
}
