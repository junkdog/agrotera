package net.onedaybeard.arbum.transform;

import net.onedaybeard.arbum.meta.ArtemisConfigurationData;

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
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		System.out.println("class " + name.substring(name.lastIndexOf('/') + 1)
			+ " extends " + superName.substring(superName.lastIndexOf('/') + 1));
		super.visit(version, access, name, signature, superName, interfaces);
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
		if ("Llombok/ArtemisConfiguration;".equals(desc))
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