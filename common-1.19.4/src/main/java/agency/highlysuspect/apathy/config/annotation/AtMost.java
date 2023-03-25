package agency.highlysuspect.apathy.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//Require the integer to be at most this much.
@Retention(RetentionPolicy.RUNTIME)
public @interface AtMost {
	int maxInt() default Integer.MAX_VALUE;
	long maxLong() default Long.MAX_VALUE;
}
