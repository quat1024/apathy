package agency.highlysuspect.apathy.config.types;

import net.minecraft.world.Difficulty;

import java.lang.reflect.Field;
import java.util.Locale;

public class DifficultySerde implements FieldSerde<Difficulty> {
	@Override
	public String write(Field targetField, Difficulty value) {
		return value.getName();
	}
	
	@Override
	public Difficulty parse(Field sourceField, String value) {
		return Difficulty.byName(value.toLowerCase(Locale.ROOT));
	}
}
