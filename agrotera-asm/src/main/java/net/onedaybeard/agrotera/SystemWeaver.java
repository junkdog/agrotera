package net.onedaybeard.agrotera;

import static net.onedaybeard.agrotera.ProcessArtemis.WOVEN_ANNOTATION;
import static net.onedaybeard.agrotera.meta.ArtemisConfigurationData.AnnotationType.SYSTEM;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.transform.ClassUtil;
import net.onedaybeard.agrotera.transform.ProfileVisitor;
import net.onedaybeard.agrotera.transform.SystemVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class SystemWeaver extends ClassWeaver implements Opcodes
{
	private ArtemisConfigurationData meta;
	private ClassReader cr;
	private ClassWriter cw;

	protected SystemWeaver(String file, ClassReader cr, ArtemisConfigurationData meta)
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
		if (meta.is(SYSTEM) && !meta.foundInitialize)
			injectInitializeStub(meta);
		if (meta.profilingEnabled)
			injectProfiler(meta);
		
		compileClass(meta, file);
	}

	private void compileClass(ArtemisConfigurationData meta, String file)
	{
		ClassVisitor cv = cw;
		if (meta.is(SYSTEM))
			cv = new SystemVisitor(cv, cr.getClassName(), meta);
		if (meta.profilingEnabled)
			cv = new ProfileVisitor(cv, meta);
		cr.accept(cv, ClassReader.EXPAND_FRAMES);

		ClassUtil.writeClass(cw, file);
	}

	private void injectProfiler(ArtemisConfigurationData meta)
	{
		FieldVisitor fv = cw.visitField(ACC_PRIVATE|ACC_FINAL, "$profiler", meta.profilerClass.getDescriptor(), null, null);
		fv.visitEnd();

		if (!meta.foundBegin)
			ClassUtil.injectMethodStub(cw, "begin");
		if (!meta.foundEnd)
			ClassUtil.injectMethodStub(cw, "end");

		cr.accept(cw, 0);
		cr = new ClassReader(cw.toByteArray());
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	}

	private void injectInitializeStub(ArtemisConfigurationData meta)
	{
		MethodVisitor method = cw.visitMethod(ACC_PROTECTED, "initialize", "()V", null, null);
		method.visitCode();
		method.visitLabel(new Label());
		method.visitInsn(RETURN);
		method.visitEnd();

		cr.accept(cw, 0);
		cr = new ClassReader(cw.toByteArray());
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	}
}
