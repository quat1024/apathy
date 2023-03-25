package agency.highlysuspect.apathy.config.types;

import java.lang.reflect.Field;

public class BooleanSerde implements FieldSerde.ToString<Boolean> {
	@Override
	public Boolean parse(Field sourceField, String value) {
		if(value.equalsIgnoreCase("true")) return true;
		else if(value.equalsIgnoreCase("false")) return false;
		else throw new RuntimeException("Cannot parse " + value + " as a bool");
	}
}
