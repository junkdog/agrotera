package net.onedaybeard.agrotera;

import static net.onedaybeard.agrotera.ProcessArtemis.WOVEN_ANNOTATION;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.transform.ClassUtil;
import net.onedaybeard.agrotera.transform.ManagerVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

class ManagerWeaver extends ClassWeaver implements Opcodes
{
	private ArtemisConfigurationData meta;
	private ClassReader cr;
	private ClassWriter cw;

	protected ManagerWeaver(String file, ClassReader cr, ArtemisConfigurationData meta)
	{
		super(file);
		this.cr = cr;
		this.meta = meta;
	}

	@Override
	protected void process(String file) throws FileNotFoundException, IOException
	{
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassUtil.injectAnnotation(cw, WOVEN_ANNOTATION);
		
		compileClass(meta, file);
	}

	private void compileClass(ArtemisConfigurationData meta, String file)
	{
		ClassVisitor cv = cw;
		cv = new ManagerVisitor(cv, cr.getClassName(), meta);
		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		ClassUtil.writeClass(cw, file);
	}
}
