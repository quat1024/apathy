package agency.highlysuspect.apathy.config.types;

import java.lang.reflect.Field;

public class StringSerde implements FieldSerde.ToString<String> {
	@Override
	public String parse(Field sourceField, String value) {
		return value;
	}
}
