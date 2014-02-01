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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.onedaybeard.agrotera.annotations.ArtemisSystem;
import net.onedaybeard.agrotera.annotations.ArtemisTemplate;
import net.onedaybeard.agrotera.matrix.MatrixStringUtil;
import net.onedaybeard.agrotera.maven.matrix.SystemRow;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.x5.template.Chunk;
import com.x5.template.Theme;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Scans the class path for all occurences of the annotation {@link ArtemisSystem}; and implementations of Component,
 * and creates a dependency matrix.
 * 
 * @author Adrian Papari junkdog@onedaybeard.net
 * @author GJ Roelofs info@codepoke.net
 * 
 */
@Mojo(name = "matrix", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ComponentMatrix
		extends AbstractMojo {

	public static String[] EXCLUSION_LIST = new String[] { "org.eclipse.jdt.core", "org/apache", "org/codehaus/plexus", "org/ow2/asm" };

	@Parameter(property = "project.build.outputDirectory")
	private File classDirectory;

	@Parameter(property = "project.build.sourceDirectory")
	private File sourceDirectory;

	@Component
	private BuildContext context;

	@Component
	private MavenProject project;

	@Parameter(property = "project.build.directory")
	private File saveDirectory;

	@Parameter(property = "project.name")
	private String name;

	@Override
	public void execute() throws MojoExecutionException {

		long then = System.currentTimeMillis();
		Log log = getLog();

		/**
		 * Create Resolver for all classes on the classpath; including dependencies
		 */
		List<URL> classPathURLS = new ArrayList<URL>();
		List<String> classpathElements = null;
		try {
			classpathElements = project.getCompileClasspathElements();
			List<URL> projectClasspathList = new ArrayList<URL>();
			for (String element : classpathElements) {

				// Check if part of the exclusion list
				boolean found = false;
				for (String key : EXCLUSION_LIST) {
					if (element.contains(key)) {
						found = true;
						break;
					}
				}

				if (found) {
					log.debug(String.format("ComponentMatrix::execute() skipping class path: %s", element));
					continue;
				}

				try {
					URL url = new File(element).toURI()
												.toURL();

					log.debug(String.format("ComponentMatrix::execute() adding class path: %s", element));

					projectClasspathList.add(url);
				} catch (MalformedURLException e) {
					throw new MojoExecutionException(element + " is an invalid classpath element", e);
				}
			}

			classPathURLS.addAll(projectClasspathList);
		} catch (DependencyResolutionRequiredException e) {
			new MojoExecutionException("Dependency resolution failed", e);
		}

		/**
		 * Creates a class loader with all dependencies
		 * TODO: Restrict this only to those dependencies which are interesting
		 * COMMENT #1: Can't just do INCLUSION, might forget some class paths which are needed for type resolvement.
		 * COMMENT #2: Just restrict to package-phase; don't care about 1 or 2 second generation then.
		 */
		URLClassLoader urlcl = new URLClassLoader(classPathURLS.toArray(new URL[0]), Thread.currentThread()
																							.getContextClassLoader());
		Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader(urlcl))
																			.addClassLoader(urlcl));

		/**
		 * Populate all required fields, Systems, Templates and Components
		 */
		ArrayList<Class<?>> artemisSystems = new ArrayList<Class<?>>(reflections.getTypesAnnotatedWith(ArtemisSystem.class));
		ArrayList<Class<?>> artemisTemplates = new ArrayList<Class<?>>(reflections.getTypesAnnotatedWith(ArtemisTemplate.class));
		ArrayList<Class<?>> components = new ArrayList<Class<?>>(reflections.getSubTypesOf(com.artemis.Component.class));
		ArrayList<Class<?>> managers = new ArrayList<Class<?>>(reflections.getSubTypesOf(com.artemis.Manager.class));

		// Sort on simple name (class name)
		Comparator<Class<?>> lexicalCompare = new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> arg0, Class<?> arg1) {
				return arg0.getSimpleName()
							.compareTo(arg1.getSimpleName());
			}
		};
		Comparator<Class<?>> packageCompare = new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> arg0, Class<?> arg1) {
				return arg0.getName()
							.compareTo(arg1.getName());
			}
		};

		// Sort everything and create strings for export
		Collections.sort(artemisSystems, packageCompare);
		Collections.sort(artemisTemplates, lexicalCompare);
		Collections.sort(components, lexicalCompare);

		ArrayList<String> componentsStr = convertToString(components);
		ArrayList<String> templatesStr = convertToString(artemisTemplates);

		/**
		 * Create the Rows, and create Strings
		 * TODO: Create better hierarchical representation?
		 */

		ArrayList<SystemRow> rows = new ArrayList<SystemRow>();
		Class prev = String.class;	// Prev is a nonsensical class the package of which we can never be in
		for (Class system : artemisSystems) {

			// See if we need to add a name row
			Package curPack = system.getPackage();
			Package prevPack = prev.getPackage();
			if (curPack != prevPack) {
				// Create the difference string between the current and last package
				String constructDiffPackage = constructDiffPackage(prevPack, curPack);
				System.err.println(constructDiffPackage);
				rows.add(new SystemRow(constructDiffPackage));
				prev = system;
			}

			rows.add(new SystemRow(system, components));
		}

		Theme theme = new Theme();
		Chunk chunk = theme.makeChunk("altMatrix");

		chunk.set("longestName", MatrixStringUtil.findLongestString(components)
													.replaceAll(".", "_") + "______");
		chunk.set("longestManagers", MatrixStringUtil.findLongestString(managers)
														.replaceAll(".", "_"));
		chunk.set("longestSystems", MatrixStringUtil.findLongestString(artemisSystems)
													.replaceAll(".", "_"));
		chunk.set("systems", rows);
		chunk.set("headers", componentsStr);
		chunk.set("templates", templatesStr);
		chunk.set("project", name);

		BufferedWriter out = null;
		try {
			System.err.println("Writing to: "+ saveDirectory);
			out = new BufferedWriter(new FileWriter(new File(saveDirectory, "altMatrix.html")));
			chunk.render(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		log.debug(String.format("ComponentMatrix::execute() matrix generation took: %.4f s", (System.currentTimeMillis() - then) / 1000f));
	}

	/**
	 * Returns, in String, the difference in package names.
	 * 
	 * test.foo.bar && test.foo.baz returns: baz
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static String constructDiffPackage(Package a, Package b) {

		// Get the package names and split on the separator
		String[] aName = a.getName()
							.split("\\.");
		String[] bName = b.getName()
							.split("\\.");

		// Compare all packages and start appending the moment one differs
		StringBuffer buffer = new StringBuffer();
		int size = Math.min(aName.length, bName.length);
		boolean diff = false, first = true;
		for (int i = 0; i < size; i++) {

			// Check if we already found a diff package name, or the current one is different
			diff = !diff ? !aName[i].equals(bName[i]) : true;

			// Append everything of package B if we have found a differing package
			if (diff) {
				if (!first)
					buffer.append(".");

				buffer.append(bName[i]);

				first = false;
			}
		}

		// Append any possible packages existing after Package a.
		for (int i = size; i < bName.length; i++) {
			if (!first)
				buffer.append(".");
			buffer.append(bName[i]);
		}

		return buffer.toString();
	}

	protected static ArrayList<String> convertToString(ArrayList<Class<?>> list) {
		ArrayList<String> output = new ArrayList<String>();
		for (Class<?> c : list) {
			output.add(c.getSimpleName());
		}

		return output;
	}
}
