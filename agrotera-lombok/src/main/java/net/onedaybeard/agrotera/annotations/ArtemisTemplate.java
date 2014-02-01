package net.onedaybeard.agrotera.annotations;

import com.artemis.Aspect;
import com.artemis.Component;

/**
 * Tagging annotation that will be used by the agrotera-maven-plugin to create additional information for the Component Matrix.<p/>
 * 
 * A Template is used to define a set of Components which will be used by some aspect of your code.
 * The Component Matrix will then also denote which systems are applicable for this Template.
 * 
 * @author GJ Roelofs info@codepoke.net
 *
 */
public @interface ArtemisTemplate {
	
	String name() default "Artemis Template";

	/**
	 * The description of this Template
	 * @return
	 */
	String description() default "No description given";

	/**
	 * Maps to {@link Aspect#all}.
	 */
	Class<? extends Component>[] components() default {};
}
