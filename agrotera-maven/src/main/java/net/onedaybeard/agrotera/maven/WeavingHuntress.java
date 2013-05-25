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

import net.onedaybeard.agrotera.ProcessArtemis;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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

	public void execute() throws MojoExecutionException
	{
		if (context != null && !context.hasDelta(sourceDirectory))
			return;
		
		ProcessArtemis hunter = new ProcessArtemis(outputDirectory);
		hunter.process();
	}
}
