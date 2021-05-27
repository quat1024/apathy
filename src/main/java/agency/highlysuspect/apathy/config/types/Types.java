package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.config.annotation.Use;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Types {
	static final Map<Class<?>, FieldSerde<?>> builtinParsers = new HashMap<>();
	static final Map<String, FieldSerde<?>> customParsers = new HashMap<>();
	
	static {
		FieldSerde<Identifier> ident = new StringSerde().map(Bijection.create(Identifier::new, Identifier::toString));
		
		builtinParsers.put(String.class, new StringSerde());
		builtinParsers.put(Identifier.class, ident);
		builtinParsers.put(Integer.TYPE, new IntSerde());
		builtinParsers.put(Boolean.TYPE, new BooleanSerde());
		
		//Basically this thing exists because I can't put custom expressions in Java annotations.
		//So I annotate the field in the config file with @Use("difficultySet") and that bounces over to here.
		
		customParsers.put("difficultySet", new DifficultySerde()
			.commaSeparatedSet(Comparator.comparingInt(Difficulty::getId)));
		
		customParsers.put("entityTypeSet", ident
			.map(Bijection.create(Registry.ENTITY_TYPE::get, Registry.ENTITY_TYPE::getId))
			.commaSeparatedSet(Comparator.comparing(Registry.ENTITY_TYPE::getId))
		);
		
		customParsers.put("triStateAllowDenyDisabled", new TriStateField.AllowDenyDisabled());
		customParsers.put("triStateDifficultyListMode", new TriStateField.DifficultyListMode());
		
		customParsers.put("optionalString", new StringSerde().optional());
		
		customParsers.put("boolAllowDeny", new StringSerde().map(Bijection.create(s -> s.equals("allow"), (Boolean b) -> b ? "allow" : "deny")));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> FieldSerde<T> find(Field field) {
		Use pw = field.getAnnotation(Use.class);
		if(pw != null) {
			return (FieldSerde<T>) customParsers.get(pw.value());
		} else if(builtinParsers.containsKey(field.getType())) {
			return (FieldSerde<T>) builtinParsers.get(field.getType());
		}
		else throw new RuntimeException("No parser for field " + field.toGenericString());
	}
}