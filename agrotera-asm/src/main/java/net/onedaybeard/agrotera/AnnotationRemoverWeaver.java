package net.onedaybeard.agrotera;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.transform.AnnotationRemoverVisitor;
import net.onedaybeard.agrotera.transform.ClassUtil;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class AnnotationRemoverWeaver extends ClassWeaver implements Opcodes
{
	private ArtemisConfigurationData meta;
	private ClassReader cr;
	private ClassWriter cw;

	protected AnnotationRemoverWeaver(String file, ClassReader cr, ArtemisConfigurationData meta)
	{
		super(file);
		this.cr = cr;
		this.meta = meta;
	}
	
	@Override
	protected void process(String file) throws FileNotFoundException, IOException
	{
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		compileClass(meta, file);
	}

	private void compileClass(ArtemisConfigurationData meta, String file)
	{
		ClassVisitor cv = cw;
		cv = new AnnotationRemoverVisitor(cv);
		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		ClassUtil.writeClass(cw, file);
	}
}
