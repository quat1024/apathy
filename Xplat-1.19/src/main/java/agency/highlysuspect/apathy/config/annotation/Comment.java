package agency.highlysuspect.apathy.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//Prints a comment before printing this config value.
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {
	String[] value();
}
