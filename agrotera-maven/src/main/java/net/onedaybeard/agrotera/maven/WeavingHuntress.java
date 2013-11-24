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

import static net.onedaybeard.agrotera.meta.ArtemisConfigurationData.AnnotationType.MANAGER;
import static net.onedaybeard.agrotera.meta.ArtemisConfigurationData.AnnotationType.POJO;
import static net.onedaybeard.agrotera.meta.ArtemisConfigurationData.AnnotationType.SYSTEM;
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

	@Override
	public void execute() throws MojoExecutionException
	{
		long start = System.currentTimeMillis();
		if (context != null && !context.hasDelta(sourceDirectory))
			return;
		
		ProcessArtemis hunter = new ProcessArtemis(outputDirectory);
		List<ArtemisConfigurationData> processed = hunter.process();
		hunter.process();
		
		Log log = getLog();
		log.info(getSummary(processed, start));
		
		String formatPattern = "\t%s:%-" + findLongestClassName(processed) + "s  Req=%d One=%d Any=%d Not=%d RefSys=%d RefMan=%d";
		for (ArtemisConfigurationData meta : processed)
		{
			log.info(String.format(formatPattern,
				typeCharacter(meta),
				formatClassName(meta.current.getClassName()),
				meta.requires.size(),
				meta.requiresOne.size(),
				meta.optional.size(),
				meta.exclude.size(),
				meta.systems.size(),
				meta.managers.size()));
		}
	}

	private static String typeCharacter(ArtemisConfigurationData meta)
	{
		switch (meta.annotationType) {
			case MANAGER:
				return "M";
			case POJO:
				return "I";
			case SYSTEM:
				return "S";
			default:
				return "S"; // profiled system
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

	private static CharSequence getSummary(List<ArtemisConfigurationData> processed, long start)
	{
		int systems = 0, managers = 0, injected = 0;
		for (ArtemisConfigurationData meta : processed)
		{
			if (meta.is(SYSTEM) || meta.profilingEnabled)
				systems++;
			else if (meta.is(MANAGER))
				managers++;
			else if (meta.is(POJO))
				injected++;
		}
		
		return String.format("Processed %d EntitySystem%s, %d Manager%s and %d Injected types in %dms.",
			systems, (systems == 1 ? "" : "s"),
			managers, (managers == 1 ? "" : "s"),
			injected,
			(System.currentTimeMillis() - start));
	}
}
