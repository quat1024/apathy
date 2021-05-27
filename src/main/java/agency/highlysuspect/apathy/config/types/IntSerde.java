package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.config.annotation.AtLeast;

import java.lang.reflect.Field;

public class IntSerde implements FieldSerde.ToString<Integer> {
	@Override
	public Integer parse(Field sourceField, String value) {
		int x;
		try {
			x = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse " + value + " as an integer", e);
		}
		
		AtLeast atLeast = sourceField.getDeclaredAnnotation(AtLeast.class);
		if(atLeast != null && x < atLeast.value()) {
			Init.LOG.warn("Value " + sourceField.getName() + " not at least " + atLeast.value());
			x = atLeast.value();
		}
		
		return x;
	}
}
