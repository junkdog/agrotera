package lombok;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artemis.Component;
import com.artemis.EntitySystem;
import com.artemis.Manager;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ProcessingSystem
{
	Class<? extends Component>[] requires();
	Class<? extends Component>[] optional() default {};
	Class<? extends Component>[] excludes() default {};
	Class<? extends EntitySystem>[] systems() default {};
	Class<? extends Manager>[] managers() default{};
	boolean profile() default false;
}
