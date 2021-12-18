package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.config.annotation.Use;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.Difficulty;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Types {
	static final Map<Class<?>, FieldSerde<?>> builtinParsers = new HashMap<>();
	static final Map<String, FieldSerde<?>> customParsers = new HashMap<>();
	
	static {
		FieldSerde<ResourceLocation> ident = new StringSerde().dimap(ResourceLocation::new, ResourceLocation::toString);
		
		builtinParsers.put(String.class, new StringSerde());
		builtinParsers.put(ResourceLocation.class, ident);
		builtinParsers.put(Integer.TYPE, new IntSerde());
		builtinParsers.put(Boolean.TYPE, new BooleanSerde());
		builtinParsers.put(Long.TYPE, new LongSerde());
		
		//Basically this thing exists because I can't put custom expressions in Java annotations.
		//So I annotate the field in the config file with @Use("difficultySet") and that bounces over to here.
		
		customParsers.put("difficultySet", new DifficultySerde()
			.commaSeparatedSet(Comparator.comparingInt(Difficulty::getId)));
		
		customParsers.put("entityTypeSet", ident
			.dimap(Registry.ENTITY_TYPE::get, Registry.ENTITY_TYPE::getKey)
			.commaSeparatedSet(Comparator.comparing(Registry.ENTITY_TYPE::getKey))
		);
		
		customParsers.put("triStateAllowDenyPass", new TriStateField.AllowDenyPass());
		
		customParsers.put("optionalString", new StringSerde().optional());
		
		customParsers.put("boolAllowDeny", new StringSerde().dimap(s -> s.equals("allow"), b -> b ? "allow" : "deny"));
		
		customParsers.put("stringList", new StringSerde().commaSeparatedList());
		
		//bulk of this stuff (going from string -> tag) is untested; it's only used to write the "default value:" comment anyways
		customParsers.put("entityTypeTagSet", ident.dimap(PlatformSupport.instance::entityTypeTag, Tag.Named::getName).commaSeparatedSet(Comparator.comparing(Tag.Named::getName)));
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
