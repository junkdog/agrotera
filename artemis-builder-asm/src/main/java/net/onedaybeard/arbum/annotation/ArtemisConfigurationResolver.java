package net.onedaybeard.arbum.annotation;

import org.objectweb.asm.ClassReader;

public final class ArtemisConfigurationResolver
{
	private ArtemisConfigurationResolver() {}
	
	public static ArtemisConfigurationData getAnnotation(ClassReader source)
	{
		ArtemisConfigurationData info = new ArtemisConfigurationData();
		source.accept(new ArtemisAnnotationScanner(info), 0);
		return info;
	}
}
