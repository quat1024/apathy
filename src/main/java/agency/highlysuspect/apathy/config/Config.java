package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.config.annotation.*;
import agency.highlysuspect.apathy.config.types.FieldSerde;
import agency.highlysuspect.apathy.config.types.Types;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public abstract class Config implements Opcodes {
	protected Config() {
		path = null;
	}
	
	//Read the config file from this path, or save the default one to it.
	public Config(Path configFilePath) throws IOException {
		path = configFilePath;
		
		if(Files.exists(configFilePath)) {
			//The config file exists, go load it. Save over the original file as well.
			parse(configFilePath)
				.upgrade()
				.save(configFilePath)
				.finish();
		} else {
			//The config file does not exist (first time starting game?). Create one.
			defaultConfig()
				.save(configFilePath)
				.finish();
		}
	}
	
	//Point this at your zero-argument constructor.
	protected abstract Config defaultConfig();
	
	//Keys in the config file that I don't know how to parse.
	//Maybe in the "upgrade" method, you can parse these using the older format, or print a warning.
	protected transient HashMap<String, String> unknownKeys;
	
	//This config file.
	protected transient final Path path;
	
	//Update the config to the latest values.
	protected Config upgrade() {
		return this;
	}
	
	//Create derived Java values from the config values.
	protected Config finish() {
		return this;
	}
	
	//Parse the config from the file at this path.
	protected Config parse(Path configFilePath) throws IOException {
		List<String> lines = Files.readAllLines(configFilePath, StandardCharsets.UTF_8);
		
		for(int lineNo = 0; lineNo < lines.size(); lineNo++) {
			try {
				String line = lines.get(lineNo).trim();
				
				//Skip comments and blank lines
				if(line.isEmpty() || line.startsWith("#")) continue;
				
				//Config file entries look like "key: value". Pull that apart.
				int colonIdx = line.indexOf(':');
				if(colonIdx == -1) {
					throw new ConfigParseException(lineNo, new RuntimeException("No key-value pair (missing : character)"));
				}
				
				String key = line.substring(0, colonIdx).trim();
				String value = line.substring(colonIdx + 1).trim();
				
				//Find the field associated with this key.
				Field keyField = findConfigField(key);
				if(keyField == null) {
					//Maybe this key was from an older version of the config file, and an upgrader knows what to do with it?
					if(unknownKeys == null) {
						unknownKeys = new HashMap<>();
					}
					unknownKeys.put(key, value);
					continue;
				}
				
				FieldSerde<?> parser = Types.find(keyField);
				keyField.set(this, parser.parse(keyField, value));
			} catch (RuntimeException | IllegalAccessException e) {
				throw new ConfigParseException(lineNo, e);
			}
		}
		
		return this;
	}
	
	protected /* non-static */ class ConfigParseException extends RuntimeException {
		public ConfigParseException(int lineNo, Throwable cause) {
			super("Problem in config file " + path + " on line " + lineNo, cause);
			setStackTrace(new StackTraceElement[0]); //remove some chaff
		}
	}
	
	private @Nullable Field findConfigField(String name) {
		try {
			Field field = this.getClass().getDeclaredField(name);
			//Skip static, final, and transient fields
			if((field.getModifiers() & (ACC_STATIC | ACC_FINAL | ACC_TRANSIENT)) != 0) return null;
			
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
	
	//Save the config file to this path.
	protected Config save(Path configFilePath) throws IOException {
		Config defaultConfig = defaultConfig();
		
		List<String> lines = new ArrayList<>();
		
		for(Field field : this.getClass().getDeclaredFields()) {
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
}
