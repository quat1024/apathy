package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.config.annotation.AtLeast;
import agency.highlysuspect.apathy.config.annotation.AtMost;

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
		if(atLeast != null && x < atLeast.minInt()) {
			Apathy119.LOG.warn("Value " + sourceField.getName() + " not at least " + atLeast.minInt());
			x = atLeast.minInt();
		}
		
		AtMost atMost = sourceField.getDeclaredAnnotation(AtMost.class);
		if(atMost != null && x > atMost.maxInt()) {
			Apathy119.LOG.warn("Value " + sourceField.getName() + " not at most " + atMost.maxInt());
			x = atMost.maxInt();
		}
		
		return x;
	}
}
