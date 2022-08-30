package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.world.Difficulty;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

//too lazy to implement enum value renaming, it always uses toLowercase of the enum name().
public record EnumSerde<T extends Enum<?>>(Class<T> enumClass) implements FieldSerde<T> {
	@Override
	public String write(Field targetField, T value) {
		return name(value);
	}
	
	@Override
	public T parse(Field sourceField, String value) {
		value = value.trim().toLowerCase(Locale.ROOT);
		
		for(T t : enumClass.getEnumConstants()) {
			if(name(t).equals(value)) return t;
		}
		
		//Error case
		String possibleValues = Arrays.stream(enumClass.getEnumConstants()).map(this::name).collect(Collectors.joining("/"));
		throw new RuntimeException("Value " + value + " on field " + sourceField.getName() + " is not one of " + possibleValues + ".");
	}
	
	private String name(T value) {
		if(value.getClass() == Difficulty.class) return ((Difficulty) value).getKey(); //I am lazy
		else return value.name().toLowerCase(Locale.ROOT);
	}
}
