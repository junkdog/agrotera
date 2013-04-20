package net.onedaybeard.agrotera.meta;


import static net.onedaybeard.agrotera.ProcessArtemis.WOVEN_ANNOTATION;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ArtemisMetaScanner extends ClassVisitor
{
	private static final String SYSTEM_ANNOTATION = "Llombok/ArtemisSystem;";
	private static final String PROFILER_ANNOTATION = "Llombok/Profile;";
	private ArtemisConfigurationData info;

	ArtemisMetaScanner(ArtemisConfigurationData annotationMirror)
	{
		super(Opcodes.ASM4);
		info = annotationMirror;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
	{
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		if (SYSTEM_ANNOTATION.equals(desc))
			return new ArtemisAnnotationReader(desc, info);
		else if (PROFILER_ANNOTATION.equals(desc))
			return new ProfileAnnotationReader(desc, info);
		else if (WOVEN_ANNOTATION.equals(desc))
			info.isPreviouslyProcessed = true;
			
		return super.visitAnnotation(desc, visible);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
	{
		if ("initialize".equals(name) && "()V".equals(desc))
			info.foundInitialize = true;
		else if ("begin".equals(name) && "()V".equals(desc))
			info.foundBegin = true;
		else if ("end".equals(name) && "()V".equals(desc))
			info.foundEnd = true;
		
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
}