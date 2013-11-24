package net.onedaybeard.agrotera;

import static net.onedaybeard.agrotera.matrix.MatrixStringUtil.findLongestClassName;
import static net.onedaybeard.agrotera.matrix.MatrixStringUtil.findLongestManagerList;
import static net.onedaybeard.agrotera.matrix.MatrixStringUtil.findLongestSystemList;

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
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.onedaybeard.agrotera.matrix.AgroteraMapping;
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
	private final File root;
	private final File output;
	private final String projectName;
	
	public MatrixBuilder(String projectName, File root, File output)
	{
		this.projectName = projectName;
		this.root = root;
		this.output = output;
	}
	
	public void process()
	{
		List<ArtemisConfigurationData> systems = findSystems(root);
		if (systems.size() == 0)
			return;
		SortedSet<Type> componentSet = findComponents(systems);
		
		List<AgroteraMapping> systemMappings = new ArrayList<AgroteraMapping>();
		for (ArtemisConfigurationData system : systems)
		{
			AgroteraMapping mappedSystem = AgroteraMapping.from(
				system, getComponentIndices(componentSet));
			systemMappings.add(mappedSystem);
		}
		

		List<String> columns = new ArrayList<String>();
		for (Type component : componentSet)
		{
			String name = component.getClassName();
			name = name.substring(name.lastIndexOf('.') + 1);
			columns.add(name);
		}
		
		write(toMap(systemMappings), columns);
	}
	
	public static SortedMap<String,List<AgroteraMapping>> toMap(List<AgroteraMapping> systems)
	{
		String common = findCommonPackage(systems);
		SortedMap<String, List<AgroteraMapping>> map = new TreeMap<String, List<AgroteraMapping>>();
		for (int i = 0, s = systems.size(); s > i; i++)
		{
			AgroteraMapping system = systems.get(i);
			String packageName = toPackageName(system.system.getClassName());
			packageName = (packageName.length() > common.length())
				? packageName.substring(common.length())
				: ".";
			if (!map.containsKey(packageName))
				map.put(packageName, new ArrayList<AgroteraMapping>());
			
			map.get(packageName).add(system);
		}
		
		return map;
	}
	
	private static String findCommonPackage(List<AgroteraMapping> systems)
	{
		String prefix = toPackageName(systems.get(0).system.getClassName());
		for (int i = 1, s = systems.size(); s > i; i++)
		{
			String p = toPackageName(systems.get(i).system.getClassName());
			for (int j = 0, l = Math.min(prefix.length(), p.length()); l > j; j++)
			{
				if (prefix.charAt(j) != p.charAt(j))
				{
					prefix = prefix.substring(0, j);
					break;
				}
			}
		}

		return prefix;
	}

	private static String toPackageName(String className)
	{
		return className.substring(0, className.lastIndexOf('.'));
	}

	private static List<ArtemisConfigurationData> findSystems(File root)
	{
		List<ArtemisConfigurationData> systems = new ArrayList<ArtemisConfigurationData>();
		for (File f : ClassFinder.find(root))
			filterSystems(f, systems);
		
		Collections.sort(systems, new SystemComparator());
		return systems;
	}

	private static SortedSet<Type> findComponents(List<ArtemisConfigurationData> systems)
	{
		SortedSet<Type> componentSet = new TreeSet<Type>(new ComponentSorter());
		for (ArtemisConfigurationData system : systems)
		{
			componentSet.addAll(system.requires);
			componentSet.addAll(system.requiresOne);
			componentSet.addAll(system.optional);
			componentSet.addAll(system.exclude);
		}
		return componentSet;
	}
	
	private void write(SortedMap<String, List<AgroteraMapping>> mappedSystems, List<String> columns)
	{
		Theme theme = new Theme();
		Chunk chunk = theme.makeChunk("matrix");
		
		List<AgroteraMapping> mapping = new ArrayList<AgroteraMapping>();
		for (Entry<String,List<AgroteraMapping>> entry : mappedSystems.entrySet())
		{
			mapping.add(new AgroteraMapping(entry.getKey()));
			mapping.addAll(entry.getValue());
		}
		
		chunk.set("longestName", findLongestClassName(mappedSystems).replaceAll(".", "_") + "______");
		chunk.set("longestManagers", findLongestManagerList(mappedSystems).replaceAll(".", "_"));
		chunk.set("longestSystems", findLongestSystemList(mappedSystems).replaceAll(".", "_"));
		chunk.set("systems", mapping);
		chunk.set("headers", columns);
		chunk.set("project", projectName);
		
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(new FileWriter(output));
			chunk.render(out);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null) try {
				out.close();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static Map<Type,Integer> getComponentIndices(SortedSet<Type> componentSet)
	{
		Map<Type, Integer> componentIndices = new HashMap<Type, Integer>();
		int index = 0;
		for (Type component : componentSet)
		{
			componentIndices.put(component, index++);
		}
		return componentIndices;
	}
	
	private static void filterSystems(File file, List<ArtemisConfigurationData> destination)
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			
			ClassReader cr = new ClassReader(stream);
			ArtemisConfigurationData meta = ArtemisConfigurationResolver.scan(cr);
			meta.current = Type.getObjectType(cr.getClassName());
			
			if (meta.annotationType != null)
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
	
	private static class ComponentSorter implements Comparator<Type>
	{
		@Override
		public int compare(Type o1, Type o2)
		{
			return o1.getClassName().compareTo(o2.getClassName());
		}
	}

	private static class SystemComparator implements Comparator<ArtemisConfigurationData>
	{
		@Override
		public int compare(ArtemisConfigurationData o1, ArtemisConfigurationData o2)
		{
			return o1.current.toString().compareTo(o2.current.toString());
		}
	}
}
