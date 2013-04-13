package net.onedaybeard.agrotera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationResolver;
import net.onedaybeard.agrotera.transform.SystemVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Hello world!
 *
 */
public class ProcessArtemis 
{
	public static void main(String[] args)
    {
    	ProcessArtemis app = new ProcessArtemis();
    	
    	if (args.length == 0)
    	{
    		List<File> klazzes = new ArrayList<>();
    		addFiles(klazzes, new File("."));
    		System.out.println("found classes: " + klazzes.size());
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
    	
    	try (FileInputStream stream = new FileInputStream(file))
    	{
    		ClassReader reader = new ClassReader(stream);
    		ArtemisConfigurationData annotation = ArtemisConfigurationResolver.scan(reader);
			
    		if (annotation.isAnnotationPresent)
    		{
    			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    			if (!annotation.foundInitialize)
    			{
    				MethodVisitor method = writer.visitMethod(Opcodes.ACC_PROTECTED, "initialize", "()V", null, null);
    				method.visitCode();
    				method.visitLabel(new Label());
    				method.visitInsn(Opcodes.RETURN);
    				method.visitEnd();
    				
    				reader.accept(writer, 0);
    				reader = new ClassReader(writer.toByteArray());
    				writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    			}
    			
    			reader.accept(new SystemVisitor(writer, reader.getClassName(), annotation), 0);
    			byte[] cafebabe = writer.toByteArray();
    			
//    			PrintWriter printer = new PrintWriter(System.out);
//    			CheckClassAdapter.verify(new ClassReader(cafebabe), false, printer);
    			writeClass(writer, file);
    		}
			
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

	private static void writeClass(ClassWriter writer, String file)
	{
		try (FileOutputStream fos = new FileOutputStream(file))
		{
			fos.write(writer.toByteArray());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
