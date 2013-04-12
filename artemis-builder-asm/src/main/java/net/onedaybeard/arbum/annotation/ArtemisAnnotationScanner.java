package net.onedaybeard.arbum.annotation;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

class ArtemisAnnotationScanner extends ClassVisitor
{
	private static final String ARTEMIS_ANNOTATION = "Llombok/ArtemisConfiguration;";
	private ArtemisConfigurationData info;

	ArtemisAnnotationScanner(ArtemisConfigurationData annotationMirror)
	{
		super(Opcodes.ASM4);
		info = annotationMirror;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		if (ARTEMIS_ANNOTATION.equals(desc))
			return new ArtemisAnnotationReader(desc, info);
		else
			return super.visitAnnotation(desc, visible);
	}
}