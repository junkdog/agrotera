package net.onedaybeard.arbum;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import net.onedaybeard.arbum.annotation.ArtemisConfigurationData;
import net.onedaybeard.arbum.annotation.ArtemisConfigurationResolver;
import net.onedaybeard.arbum.transform.InitializeVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args)
    {
    	App app = new App();
    	for (String arg : args)
    	{
    		app.readFile(arg);
    	}
    }
    
    private void readFile(String file)
    {
    	try (FileInputStream stream = new FileInputStream(file))
    	{
    		
    		ClassReader reader = new ClassReader(stream);
    		ArtemisConfigurationData annotation = ArtemisConfigurationResolver.getAnnotation(reader);
			
    		if (annotation.isAnnotationPresent)
    		{
    			ClassWriter writer = new ClassWriter(0);
    			reader.accept(new InitializeVisitor(writer, reader.getClassName(), annotation), 0);
    			byte[] cafebabe = writer.toByteArray();
    			
    			PrintWriter printer = new PrintWriter(System.out);
    			CheckClassAdapter.verify(new ClassReader(cafebabe), false, printer);
    			writeClass(writer);
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
    	System.out.println();
    }

    private static int i;
    
	private static void writeClass(ClassWriter writer)
	{
		byte[] classData = writer.toByteArray();
		try (FileOutputStream fos = new FileOutputStream("Test" + (i++) + ".class"))
		{
			fos.write(classData);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
