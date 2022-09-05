package agency.highlysuspect.apathy.config.types;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.annotation.Use;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Types {
	static final Map<Class<?>, FieldSerde<?>> builtinParsers = new HashMap<>();
	static final Map<String, FieldSerde<?>> customParsers = new HashMap<>();
	
	static {
		FieldSerde<ResourceLocation> rl = new StringSerde().dimap(ResourceLocation::new, ResourceLocation::toString);
		FieldSerde<Difficulty> difficulty = new EnumSerde<>(Difficulty.class);
		
		builtinParsers.put(String.class, new StringSerde());
		builtinParsers.put(ResourceLocation.class, rl);
		builtinParsers.put(Integer.TYPE, new IntSerde());
		builtinParsers.put(Boolean.TYPE, new BooleanSerde());
		builtinParsers.put(Long.TYPE, new LongSerde());
		builtinParsers.put(Difficulty.class, difficulty);
		builtinParsers.put(BossConfig.DragonInitialState.class, new EnumSerde<>(BossConfig.DragonInitialState.class));
		builtinParsers.put(BossConfig.PortalInitialState.class, new EnumSerde<>(BossConfig.PortalInitialState.class));
		builtinParsers.put(BossConfig.ResummonSequence.class, new EnumSerde<>(BossConfig.ResummonSequence.class));
		builtinParsers.put(BossConfig.ElderGuardianEffect.class, new EnumSerde<>(BossConfig.ElderGuardianEffect.class));
		
		//Basically this thing exists because I can't put custom expressions in Java annotations.
		//So I annotate the field in the config file with @Use("difficultySet"), and that bounces over to here.
		customParsers.put("difficultySet", difficulty.commaSeparatedSet(Comparator.comparingInt(Difficulty::getId)));
		customParsers.put("entityTypeSet", rl.dimap(Registry.ENTITY_TYPE::get, Registry.ENTITY_TYPE::getKey).commaSeparatedSet(Comparator.comparing(Registry.ENTITY_TYPE::getKey)));
		customParsers.put("triStateAllowDenyPass", new TriStateField.AllowDenyPass());
		customParsers.put("optionalString", new StringSerde().optional());
		customParsers.put("boolAllowDeny", new StringSerde().dimap(s -> s.equals("allow"), b -> b ? "allow" : "deny"));
		customParsers.put("stringList", new StringSerde().commaSeparatedList());
		customParsers.put("entityTypeTagSet", rl.dimap(x -> TagKey.create(Registry.ENTITY_TYPE_REGISTRY, x), TagKey::location).commaSeparatedSet(Comparator.comparing(TagKey::location)));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> FieldSerde<T> find(Field field) {
		Use use = field.getAnnotation(Use.class);
		if(use != null) {
			return (FieldSerde<T>) customParsers.get(use.value());
		} else if(builtinParsers.containsKey(field.getType())) {
			return (FieldSerde<T>) builtinParsers.get(field.getType());
		}
		else throw new RuntimeException("No parser for field " + field.toGenericString());
	}
}
