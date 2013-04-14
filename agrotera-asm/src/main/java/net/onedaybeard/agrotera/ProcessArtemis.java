package net.onedaybeard.agrotera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationResolver;
import net.onedaybeard.agrotera.transform.ProfileVisitor;
import net.onedaybeard.agrotera.transform.SystemVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Hello world!
 *
 */
public class ProcessArtemis implements Opcodes 
{
	private ClassReader cr;
	private ClassWriter cw;
	
	public static void main(String[] args)
    {
    	ProcessArtemis app = new ProcessArtemis();
    	
    	if (args.length == 0)
    	{
    		List<File> klazzes = new ArrayList<>();
    		addFiles(klazzes, new File("."));
    		for (File f : klazzes)
    		{
    			app.readFile(f.getAbsolutePath());
    		}
    	}
    	else
    	{
    		for (String arg : args)
    		{
    			app.readFile(arg);
    		}
    	}
    }
	
	private static void addFiles(List<File> files, File folder)
	{
		for (File f : folder.listFiles())
		{
			if (f.isFile() && f.getName().endsWith(".class"))
				files.add(f);
			else if (f.isDirectory())
				addFiles(files, f);
		}
	}

    private void readFile(String file)
    {
    	if (!file.endsWith(".class"))
    		return;
    	
    	System.out.println("processing " + file);
    	try (FileInputStream stream = new FileInputStream(file))
    	{
    		cr = new ClassReader(stream);
    		ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
    		meta.current = Type.getObjectType(cr.getClassName());
    		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			
    		if (meta.isAnnotationPresent)
    			injectConfiguration(meta);
    		if (meta.profilingEnabled)
    			injectProfiler(meta);
    		
    		if (meta.isAnnotationPresent || meta.profilingEnabled)
    			writeClass(cw, file);
    	}
		catch (FileNotFoundException e)
		{
			System.err.println("not found: " + file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }

	private void injectProfiler(ArtemisConfigurationData meta)
	{
		System.out.println("injecting for " + meta.current);
		
		FieldVisitor fv = cw.visitField(ACC_PRIVATE, "$profiler", meta.profilerClass.getDescriptor(), null, null);
		fv.visitEnd();
		
		if (!meta.foundBegin)
			injectMethodStub("begin");
		if (!meta.foundEnd)
			injectMethodStub("end");
		
//		cr.accept(cw, 0);
//		cr = new ClassReader(cw.toByteArray());
//		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		
		cr.accept(new ProfileVisitor(cw , cr.getClassName(), meta), 0);
	}

	private void injectMethodStub(String methodName)
	{
		MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, methodName, "()V", null, null);
		mv.visitCode();
		mv.visitInsn(RETURN);
		mv.visitEnd();
	}

	private void injectConfiguration(ArtemisConfigurationData meta)
	{
		if (!meta.foundInitialize)
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
		cr.accept(new SystemVisitor(cw, cr.getClassName(), meta), 0);
	}

	private void writeClass(ClassWriter writer, String file)
	{
//		cr.accept(cw, 0);
//    	PrintWriter printer = new PrintWriter(System.out);
//    	CheckClassAdapter.verify(new ClassReader(writer.toByteArray()), false, printer);
		try (FileOutputStream fos = new FileOutputStream(file.substring(file.lastIndexOf('/') + 1)))
		{
			fos.write(writer.toByteArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
