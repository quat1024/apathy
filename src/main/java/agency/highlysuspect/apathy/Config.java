package agency.highlysuspect.apathy;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class Config implements Opcodes {
	@SuppressWarnings("unused")
	public int configVersion = 0;
	@Blankline
	@Section("Clojure")
	@Comment({
		"Enable the Clojure API for configuring the mod. See the README on github for more information."
	})
	public boolean useClojure = false; //False by default. Sorry Eutro.
	
	@Blankline
	@Section("Optimization")
	@Comment({
		"As an optimization, mobs that are currently attacking a player do not check every tick if it's still okay to do so.",
		"This is how often the mob will check. Set this to 1, to check every tick."
	})
	@AtLeast(1)
	public int recheckInterval = 20;
	
	private transient HashMap<String, String> unknownKeys = new HashMap<>();
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //But it makes more sense that way!
	public boolean allowedToTargetPlayer(MobEntity attacker, PlayerEntity player) {
		if(attacker.world.isClient) throw new IllegalStateException("Do not call on the client, please");
		
		if(useClojure) {
			TriState result = Init.clojureProxy.allowedToTargetPlayer(attacker, player);
			if(result != TriState.DEFAULT) {
				return result.get();
			}
		}
		
		return false;
	}
	
	public static Config fromPath(Path configFilePath) throws IOException {
		if(Files.exists(configFilePath)) {
			//The config file exists, go load it.
			return parse(configFilePath).upgrade();
		} else {
			//The config file does not exist (first time starting game?). Create one.
			Config defaultConfig = new Config();
			defaultConfig.save(configFilePath);
			return defaultConfig.upgrade();
		}
	}
	
	public Config upgrade() {
		//There haven't been any breaking changes to the config yet, so all unknown keys are probably a mistake.
		unknownKeys.forEach((key, value) -> Init.LOG.warn("Unknown config field: " + key));
		//We don't need to keep track of them anymore.
		unknownKeys = null;
		return this;
	}
	
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
					//Maybe this key was from an older version of the config file, and an upgrader will take care of it?
					config.unknownKeys.put(key, value);
					continue;
				}
				
				//Parse the value and set it in the built config.
				if(keyField.getType() == Boolean.TYPE) keyField.setBoolean(config, parseBool(value));
				else if(keyField.getType() == Integer.TYPE) {
					int val = parseInt(value);
					AtLeast atLeast = keyField.getDeclaredAnnotation(AtLeast.class);
					if(atLeast != null && val < atLeast.value()) {
						Init.LOG.warn("Value " + key + " not at least " + atLeast.value());
						val = atLeast.value();
					}
					
					keyField.setInt(config, val);
				}
				else if(keyField.getType() == String.class) keyField.set(config, value);
				else throw new RuntimeException("dunno how to parse " + keyField.toGenericString() + ", ruh roh");
				
			} catch (RuntimeException e) {
				throw new LinedConfigException("Error in config file on line " + lineNo, e);
			} catch (IllegalAccessException e) {
				throw new LinedConfigException("quat's a doofus, line " + lineNo, e);
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
	
	private static boolean parseBool(String value) {
		if(value.equalsIgnoreCase("true")) return true;
		else if(value.equalsIgnoreCase("false")) return false;
		else throw new RuntimeException("Cannot parse " + value + " as a bool");
	}
	
	private static int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse " + value + " as an integer", e);
		}
	}
	
	public void save(Path configFilePath) throws IOException {
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
				lines.add("");
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
				lines.add("# Must be at least " + atLeast.value() + ".");
			}
			
			try {
				//Write the "default" comment for this config field.
				lines.add("# Default: " + field.get(defaultConfig).toString());
				
				//Write the field's name, a colon-space, then the field's value.
				lines.add(field.getName() + ": " + field.get(this).toString());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Uh oh", e);
			}
			
			//Finally, write a blank line after the field.
			lines.add("");
		}
		
		//Now, save the file.
		Files.write(configFilePath, lines);
	}
	
	//Prints a big section header.
	public @interface Section {
		String value();
	}
	
	//Prints a comment before printing this config value.
	public @interface Comment {
		String[] value();
	}
	
	//Prints a blank line before this config option.
	public @interface Blankline {}
	
	//Require the integer to be at least this much.
	public @interface AtLeast {
		int value();
	}
	
	//This exception is always a rethrow of another exception, my own stacktrace is noise in this case.
	static class LinedConfigException extends RuntimeException {
		public LinedConfigException(String message, Throwable cause) {
			super(message, cause);
			setStackTrace(null);
		}
	}
}
