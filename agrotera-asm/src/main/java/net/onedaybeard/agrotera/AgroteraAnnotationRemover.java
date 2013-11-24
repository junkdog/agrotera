package net.onedaybeard.agrotera;

import static net.onedaybeard.agrotera.ThreadPoolUtil.awaitTermination;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationResolver;
import net.onedaybeard.agrotera.util.ClassFinder;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AgroteraAnnotationRemover implements Opcodes 
{
	private File root;
	
	public AgroteraAnnotationRemover(File root)
	{
		this.root = root;
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
	
	private static void processClass(ExecutorService threadPool, String file, List<ArtemisConfigurationData> processed)
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			
			ClassReader cr = new ClassReader(stream);
			ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
			meta.current = Type.getObjectType(cr.getClassName());
			
			if (meta.annotationType == null)
				return;
			
			threadPool.submit(new AnnotationRemoverWeaver(file, cr, meta));
			processed.add(meta);
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
