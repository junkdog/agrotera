package net.onedaybeard.agrotera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationResolver;
import net.onedaybeard.agrotera.util.ClassFinder;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ProcessArtemis implements Opcodes 
{
	public static final String WOVEN_ANNOTATION = "Lnet/onedaybeard/agrotera/internal/WovenByTheHuntress;";
	
	private File root;
	
	public ProcessArtemis(File root)
	{
		this.root = root;
	}
	
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			for (File f : ClassFinder.find("."))
			{
				processClass(f.getAbsolutePath());
			}
		}
		else
		{
			for (String arg : args)
			{
				// eclipse sends folders along too
				if (arg.endsWith(".class")) processClass(arg);
			}
		}
	}
	
	public void process()
	{
		for (File f : ClassFinder.find(root))
			processClass(f.getAbsolutePath());
	}
	
	private static void processClass(String file)
	{
		try (FileInputStream stream = new FileInputStream(file))
		{
			ClassReader cr = new ClassReader(stream);
			ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
			meta.current = Type.getObjectType(cr.getClassName());
			
			if (meta.isPreviouslyProcessed)
				return;
			
			if (meta.isSystemAnnotation || meta.profilingEnabled)
			{
				ClassWeaver weaver = new SystemWeaver(cr, meta);
				weaver.process(file);
			}
			else if (meta.isManagerAnnotation)
			{
				ClassWeaver weaver = new ManagerWeaver(cr, meta);
				weaver.process(file);
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
}
