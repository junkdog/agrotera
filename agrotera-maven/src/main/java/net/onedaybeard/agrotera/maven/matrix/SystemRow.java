package net.onedaybeard.agrotera.maven.matrix;

import java.util.ArrayList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import net.onedaybeard.agrotera.annotations.ArtemisSystem;
import net.onedaybeard.agrotera.matrix.ComponentReference;

import com.artemis.EntitySystem;
import com.artemis.Manager;

/**
 * POJO that will contain the information used for the dhtml output.
 * @author GJ Roelofs info@codepoke.net
 *
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class SystemRow {

	boolean isSystem;
	boolean isManager;
	boolean isPackage;	// Used by the chtml matrix to fill an empty row

	String name;

	ComponentReference[] componentIndices;
	String[] ref_managers;
	String[] ref_systems;

	public SystemRow(Class<?> systemClass, ArrayList<Class<?>> components) {

		/**
		 * Don't ever forget to give the current ClassLoader to the URLClassLoader as context.
		 * Woe is me
		 */
		ArtemisSystem annotation = systemClass.getAnnotation(ArtemisSystem.class);

		isManager = Manager.class.isAssignableFrom(systemClass);
		isSystem = EntitySystem.class.isAssignableFrom(systemClass);
		isPackage = false;

		name = systemClass.getSimpleName();

		if (annotation != null) {
			this.ref_managers = new String[annotation.managers().length];
			this.ref_systems = new String[annotation.systems().length];
		} else {
			this.ref_managers = new String[0];
			this.ref_systems = new String[0];
		}

		this.componentIndices = new ComponentReference[components.size()];

		for (int i = 0; i < components.size(); i++) {

			Class<?> comp = components.get(i);
			ComponentReference target = ComponentReference.NOT_REFERENCED;

			if (annotation != null) {
				if (contains(annotation.optional(), comp))
					target = ComponentReference.OPTIONAL;
				else if (contains(annotation.requires(), comp))
					target = ComponentReference.REQUIRED;
				else if (contains(annotation.requiresOne(), comp))
					target = ComponentReference.ANY;
				else if (contains(annotation.excludes(), comp))
					target = ComponentReference.EXCLUDED;
			}

			this.componentIndices[i] = target;
		}

	}

	/**
	 * Simple method to check whether the array contains the given value.
	 * Unsure why the JDK still doesn't have this utility method, instead opting for asList().contains().
	 * 
	 * @param array
	 * @param v
	 * @return
	 */
	public static <T> boolean contains(final T[] array, final T v) {

		if (array == null)
			return false;

		for (final T e : array)
			if (e == v || v != null && v.equals(e))
				return true;

		return false;
	}
	

	public SystemRow(String name) {

		isPackage = true;
		isManager = false;
		isSystem = false;

		this.name = name;

		this.ref_managers = new String[0];
		this.ref_systems = new String[0];
		this.componentIndices = new ComponentReference[0];

	}

}
