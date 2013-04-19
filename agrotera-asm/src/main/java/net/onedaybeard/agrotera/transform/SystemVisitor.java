package net.onedaybeard.agrotera.transform;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SystemVisitor extends ClassVisitor implements Opcodes
{
	private String className;
	private ArtemisConfigurationData info;
	
	public SystemVisitor(ClassVisitor cv, String className, ArtemisConfigurationData info)
	{
		super(Opcodes.ASM4, cv);
		this.className = className;
		this.info = info;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature,
		String[] exceptions)
	{
		MethodVisitor method = super.visitMethod(access, name, desc, signature, exceptions);
		
		if ("initialize".equals(name) && "()V".equals(desc))
			method = new InitializeWeaver(method, className, info);
		else if ("<init>".equals(name))
			method = new ConstructorWeaver(method, info);
		
		return method;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		if ("Llombok/ArtemisSystem;".equals(desc))
			return null; // removing annotation to avoid further processing
		else
			return super.visitAnnotation(desc, visible);
	}
	
	@Override
	public void visitEnd()
	{
		super.visitEnd();
	}
}