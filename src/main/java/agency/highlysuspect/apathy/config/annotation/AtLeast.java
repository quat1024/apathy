package agency.highlysuspect.apathy.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//Require the integer to be at least this much.
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeast {
	int value();
}
