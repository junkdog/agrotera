package net.onedaybeard.agrotera.transform;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ProfileVisitor extends ClassVisitor implements Opcodes
{
	private String className;
	private ArtemisConfigurationData info;
	
	public ProfileVisitor(ClassVisitor cv, String className, ArtemisConfigurationData info)
	{
		super(Opcodes.ASM4, cv);
		this.className = className;
		this.info = info;
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature,
		String[] exceptions)
	{
		System.out.println("\tgetting method: " + name);
		MethodVisitor method = super.visitMethod(access, name, desc, signature, exceptions);
		
		if ("begin".equals(name) && "()V".equals(desc))
			method = new ProfileBeginWeaver(method, info, access, name, desc);
		else if ("end".equals(name) && "()V".equals(desc))
			method = new ProfileEndWeaver(method, info, access, name, desc);
		else if ("<init>".equals(name))
			method = new ProfileConstructorWeaver(method, info, access, name, desc);
		
		return method;
	}
	
	@Override
	public void visitEnd()
	{
		super.visitEnd();
	}
}