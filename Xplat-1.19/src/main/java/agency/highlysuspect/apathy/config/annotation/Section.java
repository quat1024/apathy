package agency.highlysuspect.apathy.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//Prints a big section header.
@Retention(RetentionPolicy.RUNTIME)
public @interface Section {
	String value();
}
