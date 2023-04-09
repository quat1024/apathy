package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.config.ConfigProperty;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.config.CookedConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ForgeBackedConfig implements CookedConfig {
	public ForgeBackedConfig(Map<ConfigProperty<?>, Supplier<?>> liveConfig) {
		this.liveConfig = liveConfig;
	}
	
	//Behind these Suppliers are live ForgeConfigSpec.Builder objects. "Live" in the sense that Forge will keep them up to date.
	private final Map<ConfigProperty<?>, Supplier<?>> liveConfig;
	
	//NightConfig can't handle all the object types I want, so I map weirder types to string config options (in the bakery below.)
	//This means to realize the object I need to parse the string. I'd like to not have to do that every time I read an option.
	private final Map<ConfigProperty<?>, Object> cache = new IdentityHashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> key) {
		return (T) cache.computeIfAbsent(key, this::getUncached);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getUncached(ConfigProperty<T> key) {
		Supplier<T> s = (Supplier<T>) liveConfig.get(key);
		if(s == null) return key.defaultValue();
		else {
			try {
				return s.get();
			} catch (Exception e) {
				Apathy.instance.log.error("Failed to parse option '" + key.name() + "': " + e.getMessage(), e);
				return key.defaultValue();
			}
		}
	}
	
	@Override
	public boolean refresh() {
		cache.clear();
		return true;
	}
	
	public static class Bakery implements ConfigSchema.Bakery {
		public Bakery(ForgeConfigSpec.Builder spec) {
			this.spec = spec;
		}
		
		public final ForgeConfigSpec.Builder spec;
		
		@Override
		public CookedConfig cook(ConfigSchema schema) {
			Map<ConfigProperty<?>, Supplier<?>> configGetters = new IdentityHashMap<>();
			
			spec.push("Uncategorized");
			
			schema.accept(new ConfigSchema.Visitor() {
				@Override
				public void visitSection(String section) {
					spec.pop();
					spec.push(section);
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public <T> void visitOption(ConfigProperty<T> option) {
					List<String> comment = option.comment();
					if(comment.isEmpty()) spec.comment(" "); //forge will complain otherwise
					else {
						List<String> commentWithDefault = new ArrayList<>(comment.size() + 1);
						commentWithDefault.addAll(comment);
						//not part of forge's stock format for some weird reason!
						commentWithDefault.add("Default: " + option.write(option.defaultValue()));
						spec.comment(commentWithDefault.toArray(String[]::new));
					}
					
					//annoying part:
					T hmm = option.defaultValue();
					if(hmm instanceof Integer || hmm instanceof String) {
						//Forge config weirdstuff is able to handle these types without any processing, and
						//the value Forge holds in the ForgeConfigSpec.ConfigValue is the same as the requested type.
						//For numbers, we don't use Forge's "defineInRange" function, because integer range information
						//is encoded in my config library's validation function instead
						ForgeConfigSpec.ConfigValue<?> forge = spec.define(
							Collections.singletonList(option.name()),
							option::defaultValue,
							(Object thing) -> {
								try {
									option.validate(option, (T) thing);
									return true;
								} catch (Exception e) {
									return false;
								}
							},
							hmm.getClass()
						);
						configGetters.put(option, forge::get);
					} else if(hmm instanceof Boolean) {
						//Forge has weirdshit around booleans and its bad lmao, nightconfig can't do bools so forge has a wrapper function
						ForgeConfigSpec.BooleanValue forge = spec.define(option.name(), (boolean) hmm);
						configGetters.put(option, () -> {
							//Surprise!!!! Forge actually gives you a string option when you use this helper
							//I genuinely have no idea what is going on. Writing this any shorter still gave me classcastexceptions
							Object what = forge.get();
							String stringified = what.toString();
							boolean real = Boolean.parseBoolean(stringified);
							option.validate(option, (T) (Object) real);
							return real;
						});
					} else if(hmm instanceof Long) {
						//You'd think that if you passed Long.class into nightconfig, you'd get a config property that deserialized... longs.
						//Nope! You get one that deserializes integers. And there's another forge wrapper to correct for this deficiency
						ForgeConfigSpec.LongValue forge = spec.defineInRange(option.name(), (long) hmm, Long.MIN_VALUE, Long.MAX_VALUE);
						configGetters.put(option, () -> {
							Object what = forge.get();
							String stringified = what.toString();
							long real = Long.parseLong(stringified);
							option.validate(option, (T) (Object) real);
							return real;
						});
					} else {
						//Forge config weirdstuff is definitely not able to handle this type by default.
						//Fall back to a string option.
						ForgeConfigSpec.ConfigValue<String> forge = spec.define(
							Collections.singletonList(option.name()),
							() -> option.write(option.defaultValue()), //<- stringify on the way in
							(Object thing) -> {
								try {
									T thingParsed = option.parse(thing.toString()); //<- parse on the way out
									option.validate(option, thingParsed);
									return true;
								} catch (Exception e) {
									return false;
								}
							},
							String.class
						);
						
						configGetters.put(option, () -> option.parse(forge.get())); //<- parse on the way out
					}
				}
			});
			
			return new ForgeBackedConfig(configGetters);
		}
	}
}
