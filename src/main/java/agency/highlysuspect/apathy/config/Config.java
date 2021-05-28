package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.DefaultRule;
import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.config.annotation.*;
import agency.highlysuspect.apathy.config.types.FieldSerde;
import agency.highlysuspect.apathy.config.types.Types;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "OptionalUsedAsFieldOrParameterType"})
public class Config implements Opcodes {
	private static int CURRENT_CONFIG_VERSION = 0;
	@NoDefault public int configVersion = CURRENT_CONFIG_VERSION;
	
	///////////////////
	@Section("Clojure")
	///////////////////
	
	@Comment({
		"Enable the Clojure API for configuring the mod. See the README on github for more information.",
		"Rules configured through Clojure take precedence over this config file."
	})
	public boolean useClojure = false; //False by default. Sorry Eutro.
	
	///////////////////////
	@Section("Performance")
	///////////////////////
	
	@Comment({
		"By default, mobs that are currently attacking a player do not check every tick if it's still okay to do so.",
		"This is how often the mob will check. (Set this to 1 to check every tick.)"
	})
	@AtLeast(minInt = 1)
	public int recheckInterval = 20;
	
	////////////////
	@Section("Rule")
	////////////////
	
	@Comment({
		"Comma-separated list of difficulties.",
		"See the next config option for the purpose of this.",
	})
	@Example("easy, normal")
	@Use("difficultySet")
	public Set<Difficulty> difficultyLock = Collections.emptySet();
	
	@Comment({
		"If 'true', when 'difficultyLock' does not include the current world difficulty,",
		"mobs will always be allowed to attack the player."
	})
	public boolean difficultyLockEnabled = false;
	
	@Comment({
		"Should bosses always be allowed to attack the player?",
		"'Bossness' is defined by inclusion in the 'apathy:bosses' tag.",
		"By default, the tag includes the Ender Dragon and Wither."
	})
	public boolean bossBypass = true;
	
	@Comment("A comma-separated list of mobs. See the next config option for how this gets interpreted.")
	@Example("minecraft:creeper, minecraft:spider")
	@Use("entityTypeSet")
	public Set<EntityType<?>> mobSet = Collections.emptySet();
	
	@Comment({
		"How the mobSet is interpreted.",
		"May be one of:",
		"allow-list - The mobs in mobSet will always be allowed to attack the player.",
		"deny-list  - The mobs in mobSet will never be allowed to attack the player.",
		"disabled   - The mobSet will be ignored."
	})
	@Use("triStateAllowDenyDisabled")
	public TriState mobSetMode = TriState.DEFAULT;
	
	@Comment({
		"The name of a set of players that might exist.",
		"If this option is not provided, a player set is not created.",
		"The meaning of the player set is explained in the next two options."
	})
	@Use("optionalString")
	public Optional<String> playerSetName = Optional.of("no-mobs");
	
	@Comment({
		"If 'true', players can add themselves to the set, using '/apathy set join <playerListName>'.",
		"If 'false', only an operator can add them to the set, using '/apathy set-admin join <selector> <playerListName>'."
	})
	public boolean playerSetSelfSelect = true;
	
	@Comment({
		"What happens to players on the player set?",
		"May be one of:",
		"allow-list - Mobs are always allowed to attack players in the player set.",
		"deny-list  - Mobs are never allowed to attack players in the player set.",
		"disabled   - The player set will be ignored, and not created."
	})
	@Use("triStateAllowDenyDisabled")
	public TriState playerSetMode = TriState.FALSE;
	
	@Comment({
		"When you attack a mob, for how many ticks are they allowed to attack back?",
		"Or, set to -1 to disable this 'revenge' mechanic."
	})
	@Note({
		"The exact duration of the attack may be up to (<revengeTimer> + <recheckInterval>) ticks.",
		"Btw, the original mod had an option for 'eternal revenge', with an uncapped timer.",
		"I didn't port that, but the maximum value of the timer is " + Long.MAX_VALUE + " ticks.",
		"Make of that information what you will ;)"
	})
	@AtLeast(minLong = -1)
	public long revengeTimer = -1;
	
	@Comment({
		"What happens when none of the previous rules apply?",
		"May be one of:",
		"allow - By default, mobs are allowed to attack players.",
		"deny  - By default, mobs are not allowed to attack players."
	})
	@Use("boolAllowDeny")
	public boolean fallthrough;
	
	///////////////////////////////////////
	
	//Keys in the config file that I don't know how to parse.
	private transient HashMap<String, String> unknownKeys;
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //But it makes more sense that way!
	public boolean allowedToTargetPlayer(MobEntity attacker, ServerPlayerEntity player) {
		if(attacker.world.isClient) throw new IllegalStateException("Do not call on the client, please");
		
		if(useClojure) {
			TriState result = Init.clojureProxy.allowedToTargetPlayer(attacker, player);
			if(result != TriState.DEFAULT) {
				return result.get();
			}
		}
		
		return DefaultRule.allowedToAttackPlayer(this, attacker, player);
	}
	
	//Read the config file from this path, or save the default one to it.
	public static Config fromPath(Path configFilePath) throws IOException {
		if(Files.exists(configFilePath)) {
			//The config file exists, go load it.
			//Save over the original file as well.
			return parse(configFilePath).upgrade().save(configFilePath).finish();
		} else {
			//The config file does not exist (first time starting game?). Create one.
			Config defaultConfig = new Config();
			defaultConfig.save(configFilePath);
			
			return new Config().save(configFilePath).finish();
		}
	}
	
	//Update the config to the latest values.
	public Config upgrade() {
		if(unknownKeys != null) {
			//There haven't been any breaking changes to the config yet, so all unknown keys are probably a mistake.
			unknownKeys.forEach((key, value) -> Init.LOG.warn("Unknown config field: " + key));
			//We don't need to keep track of them anymore.
			unknownKeys = null;
		}
		
		configVersion = CURRENT_CONFIG_VERSION;
		
		return this;
	}
	
	//Create derived Java values from the config values.
	public Config finish() {
		return this;
	}
	
	//Parse the config from the file at this path.
	public static Config parse(Path configFilePath) throws IOException {
		Config config = new Config();
		
		List<String> lines = Files.readAllLines(configFilePath, StandardCharsets.UTF_8);
		
		for(int lineNo = 0; lineNo < lines.size(); lineNo++) {
			try {
				String line = lines.get(lineNo).trim();
				
				//Skip comments and blank lines
				if(line.isEmpty() || line.startsWith("#")) continue;
				
				//Config file entries look like "key: value". Pull that apart.
				int colonIdx = line.indexOf(':');
				if(colonIdx == -1) {
					throw new RuntimeException("No key-value pair (missing : character)");
				}
				
				String key = line.substring(0, colonIdx).trim();
				String value = line.substring(colonIdx + 1).trim();
				
				//Find the field associated with this key.
				Field keyField = findConfigField(key);
				if(keyField == null) {
					//Maybe this key was from an older version of the config file, and an upgrader knows what to do with it?
					if(config.unknownKeys == null) {
						config.unknownKeys = new HashMap<>();
					}
					config.unknownKeys.put(key, value);
					continue;
				}
				
				FieldSerde<?> parser = Types.find(keyField);
				keyField.set(config, parser.parse(keyField, value));
			} catch (RuntimeException e) {
				throw new ConfigParseException("Error in config file on line " + lineNo, e);
			} catch (IllegalAccessException e) {
				throw new ConfigParseException("quat's a doofus, line " + lineNo, e);
			}
		}
		
		return config;
	}
	
	private static @Nullable Field findConfigField(String name) {
		try {
			Field field = Config.class.getDeclaredField(name);
			//Skip static, final, and transient fields
			if((field.getModifiers() & (ACC_STATIC | ACC_FINAL | ACC_TRANSIENT)) != 0) return null;
			else return field;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
	
	//Save the config file to this path.
	public Config save(Path configFilePath) throws IOException {
		Config defaultConfig = new Config();
		
		List<String> lines = new ArrayList<>();
		
		for(Field field : Config.class.getDeclaredFields()) {
			//Skip static, final, and transient fields.
			if((field.getModifiers() & (ACC_STATIC | ACC_FINAL | ACC_TRANSIENT)) != 0) continue;
			
			//If the field requests a blank line, go print that.
			if(field.getDeclaredAnnotation(Blankline.class) != null) lines.add("");
			
			//If the field starts a section, print a big comment.
			Section section = field.getDeclaredAnnotation(Section.class);
			if(section != null) {
				String s = section.value();
				String bar = StringUtils.repeat('#', s.length() + 6);
				
				lines.add(bar);
				lines.add("## " + s + " ##");
				lines.add(bar);
			}
			
			//If the field has a comment, write that out first, prefixed with a comment character.
			Comment comment = field.getDeclaredAnnotation(Comment.class);
			if(comment != null) {
				for(String commentLine : comment.value()) {
					lines.add("# " + commentLine);
				}
			}
			
			//If the field has bounds, describe them in the comment.
			AtLeast atLeast = field.getDeclaredAnnotation(AtLeast.class);
			if(atLeast != null) {
				if(atLeast.minInt() != Integer.MIN_VALUE) {
					lines.add("# Must be at least " + atLeast.minInt() + ".");
				} else if(atLeast.minLong() != Long.MIN_VALUE) {
					lines.add("# Must be at least " + atLeast.minLong() + ".");
				}
			}
			
			//If the field has an example, include that too.
			Example example = field.getDeclaredAnnotation(Example.class);
			if(example != null) {
				for(String commentLine : example.value()) {
					lines.add("# Example: " + commentLine);
				}
			}
			
			//If the field has a note, include the note.
			Note note = field.getDeclaredAnnotation(Note.class);
			if(note != null) {
				boolean first = true;
				for(String noteLine : note.value()) {
					lines.add((first ? "# Note: " : "#       ") + noteLine);
					first = false;
				}
			}
			
			//Find the FieldSerde for this field.
			FieldSerde<?> ser = Types.find(field);
			
			try {
				if(field.getDeclaredAnnotation(NoDefault.class) == null) {
					//Write the "default" comment for this config field.
					String defaultValue = ser.writeErased(field, field.get(defaultConfig));
					lines.add("# Default: " + (defaultValue.isEmpty() ? "<empty>" : defaultValue));
				}
				
				//Write the field's name, a colon-space, then the field's value.
				lines.add(field.getName() + ": " + ser.writeErased(field, field.get(this)));
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Uh oh", e);
			}
			
			//Finally, write a blank line after the field.
			lines.add("");
		}
		
		//Now, save the file.
		Files.write(configFilePath, lines);
		
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Config config = (Config) o;
		
		if(configVersion != config.configVersion) return false;
		if(useClojure != config.useClojure) return false;
		if(recheckInterval != config.recheckInterval) return false;
		if(bossBypass != config.bossBypass) return false;
		if(!difficultyLock.equals(config.difficultyLock)) return false;
		if(!mobSet.equals(config.mobSet)) return false;
		if(mobSetMode != config.mobSetMode) return false;
		return unknownKeys.equals(config.unknownKeys);
	}
}
