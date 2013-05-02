package net.onedaybeard.agrotera;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.onedaybeard.agrotera.matrix.SystemMapping;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationResolver;
import net.onedaybeard.agrotera.util.ClassFinder;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.x5.template.Chunk;
import com.x5.template.Theme;

public class MatrixBuilder implements Opcodes 
{
	public static final String WOVEN_ANNOTATION = "Lnet/onedaybeard/agrotera/internal/WovenByTheHuntress;";
	
	private final File root;
	private final File output;
	private final String projectName;
	
	public MatrixBuilder(String projectName, File root, File output)
	{
		this.projectName = projectName;
		this.root = root;
		this.output = output;
	}
	
	public static void main(String[] args)
    {
    	MatrixBuilder app = new MatrixBuilder("Rebel Escape",
    		new File("/home/junkdog/opt/dev/git/rebelescape/rebelescape/target/classes"),
    		new File("target/matrix.html"));
    	app.process();
    }
	
	public void process()
	{
		List<ArtemisConfigurationData> systems = new ArrayList<>();
		for (File f : ClassFinder.find(root))
			processClass(f, systems);
		
		Collections.sort(systems, new Comparator<ArtemisConfigurationData>()
		{
			@Override
			public int compare(ArtemisConfigurationData o1, ArtemisConfigurationData o2)
			{
				return o1.current.toString().compareTo(o2.current.toString());
			}
		});
		
		
		SortedSet<Type> componentSet = new TreeSet<>(new Comparator<Type>()
		{
			@Override
			public int compare(Type o1, Type o2)
			{
				return o1.getClassName().compareTo(o2.getClassName());
			}
		});
		
		for (ArtemisConfigurationData system : systems)
		{
			componentSet.addAll(system.requires);
			componentSet.addAll(system.requiresOne);
			componentSet.addAll(system.optional);
			componentSet.addAll(system.exclude);
		}
		
		List<SystemMapping> mappedSystems = new ArrayList<>();
		for (ArtemisConfigurationData system : systems)
		{
			SystemMapping mappedSystem = SystemMapping.from(
				system, getComponentIndices(componentSet));
			mappedSystems.add(mappedSystem);
		}
		

		List<String> columns = new ArrayList<>();
		for (Type component : componentSet)
		{
			String name = component.getClassName();
			name = name.substring(name.lastIndexOf('.') + 1);
			columns.add(name);
		}
		
		write(mappedSystems, columns);
	}
	
	private void write(List<SystemMapping> mappedSystems, List<String> columns)
	{
		Theme theme = new Theme();
		Chunk chunk = theme.makeChunk("matrix");
		
		chunk.set("systems", mappedSystems);
		chunk.set("headers", columns);
		chunk.set("project", projectName);

		try (BufferedWriter out = new BufferedWriter(new FileWriter(output)))
		{
			chunk.render(out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Map<Type,Integer> getComponentIndices(SortedSet<Type> componentSet)
	{
		Map<Type, Integer> componentIndices = new HashMap<>();
		int index = 0;
		for (Type component : componentSet)
		{
			componentIndices.put(component, index++);
		}
		return componentIndices;
	}
	
	private static void processClass(File file, List<ArtemisConfigurationData> destination)
	{
		try (FileInputStream stream = new FileInputStream(file))
		{
			ClassReader cr = new ClassReader(stream);
			ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
			meta.current = Type.getObjectType(cr.getClassName());
			
			if (meta.isSystemAnnotation)
				destination.add(meta);
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
