package net.onedaybeard.agrotera.transform;

import java.util.Arrays;
import java.util.List;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ManagerVisitor extends ClassVisitor implements Opcodes
{
	private String className;
	private ArtemisConfigurationData info;
	
	private static final List<String> methods = 
		Arrays.asList("added", "changed", "deleted", "disabled", "enabled");
	
	public ManagerVisitor(ClassVisitor cv, String className, ArtemisConfigurationData info)
	{
		super(ASM4, cv);
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
		else if (methods.contains(name) && "(Lcom/artemis/Entity;)V".equals(desc))
			method = new ManagerMethodWeaver(method, className, info);
		
		return method;
	}
	
	@Override
	public void visitEnd()
	{
		super.visitEnd();
	}
}