package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.config.annotation.AtLeast;

import java.lang.reflect.Field;

public class LongSerde implements FieldSerde.ToString<Long> {
	@Override
	public Long parse(Field sourceField, String value) {
		long x;
		try {
			x = Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse " + value + " as an integer", e);
		}
		
		AtLeast atLeast = sourceField.getDeclaredAnnotation(AtLeast.class);
		if(atLeast != null && x < atLeast.minLong()) {
			Init.LOG.warn("Value " + sourceField.getName() + " not at least " + atLeast.minLong());
			x = atLeast.minLong();
		}
		
		return x;
	}
}
