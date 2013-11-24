package net.onedaybeard.agrotera.transform;

import static net.onedaybeard.agrotera.ProcessArtemis.WOVEN_ANNOTATION;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class AnnotationRemoverVisitor extends ClassVisitor implements Opcodes
{
	public AnnotationRemoverVisitor(ClassVisitor cv)
	{
		super(ASM4, cv);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		if (WOVEN_ANNOTATION.equals(desc)
			|| "Llombok/ArtemisInjected;".equals(desc)
			|| "Llombok/ArtemisSystem;".equals(desc)
			|| "Llombok/ArtemisManager;".equals(desc))
			
			return null;
		else 
			return super.visitAnnotation(desc, visible);
	}
}