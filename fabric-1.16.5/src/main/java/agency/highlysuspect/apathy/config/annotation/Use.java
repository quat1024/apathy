package agency.highlysuspect.apathy.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//Use a custom FieldSerde for this config field.
@Retention(RetentionPolicy.RUNTIME)
public @interface Use {
	String value();
}
