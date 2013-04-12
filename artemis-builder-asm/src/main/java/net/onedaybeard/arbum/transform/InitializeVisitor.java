package net.onedaybeard.arbum.transform;

import net.onedaybeard.arbum.annotation.ArtemisConfigurationData;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InitializeVisitor extends ClassVisitor implements Opcodes
{
	private String className;
	private ArtemisConfigurationData info;
	
	private boolean foundInitializeMethod;
	
	public InitializeVisitor(ClassVisitor cv, String className, ArtemisConfigurationData info)
	{
		super(Opcodes.ASM4, cv);
		this.className = className;
		this.info = info;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		System.out.println("class " + name + " extends " + superName);
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature,
		String[] exceptions)
	{
		System.out.println("visiting " + name);
		MethodVisitor method = super.visitMethod(access, name, desc, signature, exceptions);
		
		if ("initialize".equals(name) && "()V".equals(desc))
		{
			method = new InitializeWeaver(method, className, info);
			foundInitializeMethod = true;
		}
		
		return method;
	}
	
	
	@Override
	public void visitEnd()
	{
		if (!foundInitializeMethod)
		{
			MethodVisitor method = visitMethod(ACC_PROTECTED, "initialize", "()V", null, null);
			new InitializeWeaver(method, className, info);
		}
		
		super.visitEnd();
	}
	
	public boolean foundInitializeMethod()
	{
		return foundInitializeMethod;
	}
}