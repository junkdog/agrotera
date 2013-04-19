package net.onedaybeard.agrotera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationResolver;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ProcessArtemis implements Opcodes 
{
	private File root;
	private ClassReader cr;
	
	public ProcessArtemis(File root)
	{
		this.root = root;
	}
	
	public static void main(String[] args)
    {
    	ProcessArtemis app = new ProcessArtemis(null);
    	
    	if (args.length == 0)
    	{
    		List<File> klazzes = new ArrayList<>();
    		addFiles(klazzes, new File("."));
    		
    		for (File f : klazzes)
    		{
    			app.processClass(f.getAbsolutePath());
    		}
    	}
    	else
    	{
    		for (String arg : args)
    		{
    			app.processClass(arg);
    		}
    	}
    }
	
	public void process()
	{
		List<File> klazzes = new ArrayList<>();
		addFiles(klazzes, root);
		for (File f : klazzes)
			processClass(f.getAbsolutePath());
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

    private void processClass(String file)
    {
    	if (!file.endsWith(".class"))
    		return;
    	
    	try (FileInputStream stream = new FileInputStream(file))
    	{
    		cr = new ClassReader(stream);
    		ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
    		meta.current = Type.getObjectType(cr.getClassName());
    		
    		ClassWeaver weaver = new SystemWeaver(cr, meta);
    		weaver.process(file);
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
}
