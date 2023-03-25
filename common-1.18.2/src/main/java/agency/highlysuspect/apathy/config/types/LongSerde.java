package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.config.annotation.AtLeast;
import agency.highlysuspect.apathy.config.annotation.AtMost;

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
			Apathy.LOG.warn("Value " + sourceField.getName() + " not at least " + atLeast.minLong());
			x = atLeast.minLong();
		}
		
		AtMost atMost = sourceField.getDeclaredAnnotation(AtMost.class);
		if(atMost != null && x > atMost.maxLong()) {
			Apathy.LOG.warn("Value " + sourceField.getName() + " not at most " + atMost.maxLong());
			x = atMost.maxLong();
		}
		
		return x;
	}
}
