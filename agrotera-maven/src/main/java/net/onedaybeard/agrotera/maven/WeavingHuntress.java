package net.onedaybeard.agrotera.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;

import java.io.File;
import java.util.List;

import net.onedaybeard.agrotera.ProcessArtemis;
import net.onedaybeard.agrotera.meta.ArtemisConfigurationData;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.Type;
import org.sonatype.plexus.build.incremental.BuildContext;

@Mojo(name="agrotera", defaultPhase=PROCESS_CLASSES)
public class WeavingHuntress extends AbstractMojo
{
	@Parameter(property="project.build.outputDirectory")
	private File outputDirectory;

	@Parameter(property="project.build.sourceDirectory")
	private File sourceDirectory;
	
	@Component
	private BuildContext context;

	public void execute() throws MojoExecutionException
	{
		if (context != null && !context.hasDelta(sourceDirectory))
			return;
		
		ProcessArtemis hunter = new ProcessArtemis(outputDirectory);
		List<ArtemisConfigurationData> processed = hunter.process();
		hunter.process();
		
		Log log = getLog();
		log.info(getSummary(processed));
		
		String formatPattern = "\t%s:%-" + findLongestClassName(processed) + "s  Req:%d/One:%d/Any:%d/Not:%d RefSys:%d/RefMan:%d";
		for (ArtemisConfigurationData meta : processed)
		{
			log.info(String.format(formatPattern,
				(meta.isSystemAnnotation || meta.profilingEnabled) ? "S" : "M",
				formatClassName(meta.current.getClassName()),
				meta.requires.size(),
				meta.requiresOne.size(),
				meta.optional.size(),
				meta.exclude.size(),
				meta.systems.size(),
				meta.managers.size()));
		}
	}

	private static String formatClassName(String className)
	{
		return className.substring(className.lastIndexOf('.') + 1);
	}

	private static int findLongestClassName(List<ArtemisConfigurationData> processed)
	{
		int longest = 0;
		for (ArtemisConfigurationData meta : processed)
			longest = Math.max(longest, formatClassName(meta.current.getClassName()).length());
		
		return longest;
	}

	private CharSequence getSummary(List<ArtemisConfigurationData> processed)
	{
		int systems = 0, managers = 0;
		for (ArtemisConfigurationData meta : processed)
		{
			if (meta.isSystemAnnotation) systems++;
			else if (meta.isManagerAnnotation) managers++;
		}
		
		return String.format("Processed %d EntitySystems and %d Managers.", systems, managers);
	}
}
