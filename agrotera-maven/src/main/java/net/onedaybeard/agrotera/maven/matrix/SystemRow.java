package net.onedaybeard.agrotera.maven.matrix;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import net.onedaybeard.agrotera.annotations.ArtemisSystem;
import net.onedaybeard.agrotera.matrix.ComponentReference;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentType;
import com.artemis.EntitySystem;
import com.artemis.Manager;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * POJO that will contain the information used for the dhtml output.
 * 
 * @author GJ Roelofs info@codepoke.net
 * 
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class SystemRow<T> {

	/**
	 * The object around which this SystemRow was constructed
	 */
	T target;

	boolean isSystem;
	boolean isManager;
	boolean isPackage;		// Used by the chtml matrix to fill an empty row

	String name;
	String usedSystems;		// The ArtemisSystems this template targets; indices,
	int idx;				// The index in the matrix table

	ComponentReference[] componentIndices;
	String[] ref_managers;
	String[] ref_systems;

	/**
	 * Creates a row based on the given Class, assumes the class is annotated with the ArtemisSystem annotation.
	 * 
	 * @param systemClass
	 * @param components
	 * @param index		The index in the matrix; used by the {@link SystemRow(Field, ArrayList, Arraylist, int)} as a reference
	 */
	public SystemRow(Class<?> systemClass, ArrayList<Class<?>> components, int index) {

		target = (T) systemClass;
		idx = index;

		/**
		 * Don't ever forget to give the current ClassLoader to the URLClassLoader as context.
		 * Woe is me
		 */
		ArtemisSystem annotation = systemClass.getAnnotation(ArtemisSystem.class);

		isManager = Manager.class.isAssignableFrom(systemClass);
		isSystem = EntitySystem.class.isAssignableFrom(systemClass);
		isPackage = false;

		name = systemClass.getSimpleName();
		usedSystems = "";

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
	 * A tagging SystemRow, which will only display the given string.
	 * 
	 * @param name
	 * @param index		The index in the matrix; used by the {@link SystemRow(Field, ArrayList, Arraylist, int)} as a reference
	 */
	public SystemRow(String name, int index) {

		target = null;
		idx = index;

		isPackage = true;
		isManager = false;
		isSystem = false;

		this.name = name;
		this.usedSystems = "";

		this.ref_managers = new String[0];
		this.ref_systems = new String[0];
		this.componentIndices = new ComponentReference[0];

	}

	/**
	 * Creates a row based on the given Field.
	 * Assumes it is a static field, storing an Aspect. 
	 * 
	 * @param f	(static) Field containing an Aspect
	 * @param components
	 * @param systems
	 * @param index		The index in the matrix; used by the {@link SystemRow(Field, ArrayList, Arraylist, int)} as a reference
	 */
	public SystemRow(Field f, ArrayList<Class<?>> components, ArrayList<SystemRow> systems, int index) {

		target = (T) f;
		idx = index;

		isManager = true;
		isSystem = false;
		isPackage = false;

		this.ref_managers = new String[0];
		this.ref_systems = new String[0];

		name = f.getName();

		componentIndices = new ComponentReference[components.size()];

		Aspect a = null;

		// Try to get the Aspect from the static field
		// TODO: Proper logging
		try {
			a = (Aspect) f.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String usedSystems = "";

		if (a != null) {
			/**
			 * Handle the system lookup table; which systems this template can be used in
			 */
			ArrayList<Integer> targets = new ArrayList<Integer>();
			for (int i = 0; i < systems.size(); i++) {

				SystemRow<Class<?>> system = systems.get(i);

				if (system.isSystem) {
					Class<?> clz = system.getTarget();
					ArtemisSystem annotation = clz.getAnnotation(ArtemisSystem.class);

					Aspect match = Aspect.getEmpty()
											.all(annotation.requires())
											.one(annotation.requiresOne())
											.exclude(annotation.excludes());

					// For now we only look at the ALL variable in the Templates aspect
					if (match.isInterested(a.getAllSet())) {
						// Add the system as a target; first row in the matrix is the header, so offset by one
						targets.add(system.getIdx() + 1);
					}
				}
			}

			// Create the array, and strip it of the surrounding "[,]"
			if(!targets.isEmpty()){
				usedSystems = Arrays.toString(targets.toArray());
				usedSystems = usedSystems.substring(1, usedSystems.length() - 1);
			}

			/**
			 * Handle the component lookup table
			 */
			BitSet allSet = a.getAllSet(), oneSet = a.getOneSet(), exclusionSet = a.getExclusionSet();

			for (int i = 0; i < components.size(); i++) {

				Class<?> comp = components.get(i);
				ComponentReference target = ComponentReference.NOT_REFERENCED;

				if (a != null) {
					int idx = ComponentType.getIndexFor((Class<? extends Component>) comp);

					if (allSet.get(idx))
						target = ComponentReference.REQUIRED;
					else if (oneSet.get(idx))
						target = ComponentReference.ANY;
					else if (exclusionSet.get(idx))
						target = ComponentReference.EXCLUDED;
				}

				this.componentIndices[i] = target;
			}
		}

		this.usedSystems = usedSystems;
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

}
