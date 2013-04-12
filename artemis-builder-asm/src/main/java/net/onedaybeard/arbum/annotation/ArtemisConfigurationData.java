package net.onedaybeard.arbum.annotation;

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;

import org.objectweb.asm.Type;

@ToString
public class ArtemisConfigurationData
{
	public final List<Type> requires = new ArrayList<>();
	public final List<Type> optional = new ArrayList<>();
	public final List<Type> exclude = new ArrayList<>();
	public final List<Type> systems = new ArrayList<>();
	public final List<Type> managers = new ArrayList<>();
	
	public boolean isAnnotationPresent;
	
	ArtemisConfigurationData() {}
}
