package lombok;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.EntitySystem;
import com.artemis.Manager;

/**
 * Configures a normal java class by injecting code during the compilation
 *  phase. The transformed class get a <code>initialize(World)</code><p/>
 *  method, which - when invoked - wires up the class.
 * 
 * Component mappers are named according to the component type they operate on,
 * suffixed with <code>-Mapper</code>. Systems and managers retain their full
 * type name. All field names are <code>camelCased</code>.<p/>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ArtemisInjected
{
	/**
	 * Maps to {@link Aspect#all}.
	 */
	Class<? extends Component>[] mappers() default {};
	
	/**
	 * Systems to inject as fields.
	 */
	Class<? extends EntitySystem>[] systems() default {};
	
	/**
	 * Managers to inject as fields.
	 */
	Class<? extends Manager>[] managers() default{};
}
