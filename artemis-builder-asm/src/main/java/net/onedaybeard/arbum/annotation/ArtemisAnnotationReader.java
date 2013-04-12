package net.onedaybeard.arbum.annotation;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

final class ArtemisAnnotationReader extends AnnotationVisitor
{
	private final String annotationField;
	private ArtemisConfigurationData info;

	ArtemisAnnotationReader(String name, ArtemisConfigurationData info)
	{
		super(Opcodes.ASM4);
		this.annotationField = name;
		this.info = info;
		
		info.isAnnotationPresent = true;
	}

	@Override
	public void visit(String ignore, Object value)
	{
		if ("requires".equals(annotationField))
			info.requires.add((Type)value);
		else if ("optional".equals(annotationField))
			info.optional.add((Type)value);
		else if ("exclude".equals(annotationField))
			info.exclude.add((Type)value);
		else if ("systems".equals(annotationField))
			info.systems.add((Type)value);
		else if ("managers".equals(annotationField))
			info.managers.add((Type)value);
		
		super.visit(annotationField, value);
	}
	
	@Override
	public AnnotationVisitor visitArray(final String name)
	{
		return new ArtemisAnnotationReader(name, info);
	}
}