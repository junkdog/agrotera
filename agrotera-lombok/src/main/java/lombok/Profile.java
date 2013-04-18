package lombok;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.onedaybeard.agrotera.ArtemisProfiler;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Profile
{
	Class<? extends ArtemisProfiler> using();
	boolean enabled() default true;
}
