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
	public final List<Type> requires = new ArrayList<Type>();
	public final List<Type> requiresOne = new ArrayList<Type>();
	public final List<Type> optional = new ArrayList<Type>();
	public final List<Type> exclude = new ArrayList<Type>();
	public final List<Type> systems = new ArrayList<Type>();
	public final List<Type> managers = new ArrayList<Type>();
	public boolean isSystemAnnotation;
	public boolean isManagerAnnotation;
	
	// method search
	public boolean foundInitialize;
	public boolean foundBegin;
	public boolean foundEnd;
	
	// when found, means class has been processed
	public boolean isPreviouslyProcessed;
	
	// profiler annotation
	public boolean profilingEnabled;
	public Type profilerClass;
	
	public Type current;
	
	ArtemisConfigurationData() {}
}
