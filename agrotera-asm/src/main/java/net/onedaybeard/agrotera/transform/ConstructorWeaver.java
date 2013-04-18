package net.onedaybeard.agrotera.transform;

import java.util.List;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class ConstructorWeaver extends MethodVisitor implements Opcodes
{
	private static final String COMPONENT_ARGUMENT_DESC = "(Ljava/lang/Class;[Ljava/lang/Class;)Lcom/artemis/Aspect;";
	private static final String ASPECT = "com/artemis/Aspect";
	private static final String CLASS = "java/lang/Class";
	
	private ArtemisConfigurationData info;
	
	private boolean aspectIntercepted;
	
	ConstructorWeaver(MethodVisitor methodVisitor, ArtemisConfigurationData info)
	{
		super(Opcodes.ASM4, methodVisitor);
		this.info = info;
	}
	
	@Override
	public void visitInsn(int opcode)
	{
		boolean injectAspect = false;
		if (!aspectIntercepted)
		{
			injectAspect = (opcode == ACONST_NULL);
			System.out.println("\taspect: " + (injectAspect ? "building" : "already defined"));
			aspectIntercepted = true;
		}
		
		if (injectAspect && info.requires.size() > 0) // avoid VoidSystems
			transformConstructor();
		else
			mv.visitInsn(opcode);
	}
	
	private void transformConstructor()
	{
		injectAspect(info.requires, INVOKESTATIC, "getAspectForAll");
		if (info.exclude.size() > 0)
			injectAspect(info.exclude, INVOKEVIRTUAL, "exclude");
	}

	private void injectAspect(List<Type> components, int methodInvocation, String methodName)
	{
		// initial argument
		mv.visitLdcInsn(components.get(0));
		
		// size of varargs array
		injectIntValue(mv, components.size() - 1);
		mv.visitTypeInsn(ANEWARRAY, CLASS);
		for (int i = 1; components.size() > i; i++)
		{
//			if (i > 1) mv.visitInsn(AASTORE); // TODO: remove if works
			mv.visitInsn(DUP);
			injectIntValue(mv, i - 1);
			mv.visitLdcInsn(components.get(i));
			mv.visitInsn(AASTORE); //TODO: make sure it works
		}
		
//		if (components.size() > 1) // TODO: remove if works
//			mv.visitInsn(AASTORE);
		
		mv.visitMethodInsn(methodInvocation, ASPECT, 
			methodName, COMPONENT_ARGUMENT_DESC);
		
	}
	
	private static void injectIntValue(MethodVisitor methodVisitor, int value)
	{
		if (value > (ICONST_5 - ICONST_0))
			methodVisitor.visitIntInsn(BIPUSH, value);
		else
			methodVisitor.visitInsn(ICONST_0 + value);
	}
}
