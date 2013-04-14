package net.onedaybeard.agrotera.meta;

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;

import org.objectweb.asm.Type;

/**
 * Annotation blob bloat.
 */
@ToString
public class ArtemisConfigurationData
{
	// artemis configuration annotation
	public final List<Type> requires = new ArrayList<>();
	public final List<Type> optional = new ArrayList<>();
	public final List<Type> exclude = new ArrayList<>();
	public final List<Type> systems = new ArrayList<>();
	public final List<Type> managers = new ArrayList<>();
	public boolean isAnnotationPresent;
	
	// method search
	public boolean foundInitialize;
	public boolean foundBegin;
	public boolean foundEnd;
	
	// profiler annotation
	public boolean profilingEnabled;
	public Type profilerClass;
	
	public Type current;
	
	ArtemisConfigurationData() {}
}
