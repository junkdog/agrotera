package net.onedaybeard.agrotera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<ArtemisConfigurationData> processed = new ArrayList<ArtemisConfigurationData>();
		if (args.length == 0)
		{
			for (File f : ClassFinder.find("."))
			{
				processClass(threadPool, f.getAbsolutePath(), processed);
			}
		}
		else
		{
			for (String arg : args)
			{
				// eclipse sends folders along too
				if (arg.endsWith(".class")) processClass(threadPool, arg, processed);
			}
		}
		
		awaitTermination(threadPool);
	}
	
	public List<ArtemisConfigurationData> process()
	{
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<ArtemisConfigurationData> processed = new ArrayList<ArtemisConfigurationData>();
		for (File f : ClassFinder.find(root))
			processClass(threadPool, f.getAbsolutePath(), processed);
		
		awaitTermination(threadPool);
		return processed;
	}

	private static void awaitTermination(ExecutorService threadPool)
	{
		threadPool.shutdown();
		try
		{
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void processClass(ExecutorService threadPool, String file, List<ArtemisConfigurationData> processed)
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			
			ClassReader cr = new ClassReader(stream);
			ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
			meta.current = Type.getObjectType(cr.getClassName());
			
			if (meta.isPreviouslyProcessed)
				return;
			
			if (meta.isSystemAnnotation || meta.profilingEnabled)
			{
				threadPool.submit(new SystemWeaver(file, cr, meta));
				processed.add(meta);
			}
			else if (meta.isManagerAnnotation)
			{
				threadPool.submit(new ManagerWeaver(file, cr, meta));
				processed.add(meta);
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
		finally
		{
			if (stream != null) try {
				stream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
