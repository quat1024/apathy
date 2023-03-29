package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Apathy118;
import agency.highlysuspect.apathy.config.annotation.AtLeast;
import agency.highlysuspect.apathy.config.annotation.AtMost;
import agency.highlysuspect.apathy.core.ApathyHell;

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
			ApathyHell.instance.log.warn("Value " + sourceField.getName() + " not at least " + atLeast.minInt());
			x = atLeast.minInt();
		}
		
		AtMost atMost = sourceField.getDeclaredAnnotation(AtMost.class);
		if(atMost != null && x > atMost.maxInt()) {
			ApathyHell.instance.log.warn("Value " + sourceField.getName() + " not at most " + atMost.maxInt());
			x = atMost.maxInt();
		}
		
		return x;
	}
}
