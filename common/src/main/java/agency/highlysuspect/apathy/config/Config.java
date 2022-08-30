package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.config.annotation.*;
import agency.highlysuspect.apathy.config.types.FieldSerde;
import agency.highlysuspect.apathy.config.types.Types;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Config {
	//Read the config file from this path, or save the default one to it.
	public static <T extends Config> T read(T inst, Path configFilePath) throws IOException {
		if(Files.exists(configFilePath)) {
			//The config file exists, go load it. Save over the original file as well.
			inst.parse(configFilePath).upgrade().save(configFilePath).finish();
		} else {
			//The config file does not exist (first time starting game?). Create one.
			inst.save(configFilePath).finish();
		}
		
		return inst;
	}
	
	//Keys in the config file that I don't know how to parse.
	//Maybe in the "upgrade" method, you can parse these using the older format, or print a warning.
	protected transient HashMap<String, String> unknownKeys;
	
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
					throw new RuntimeException("No key-value pair (missing : character)");
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
			} catch (Exception e) {
				e.addSuppressed(new RuntimeException("Problem in config file " + configFilePath + " on line " + lineNo));
				throw new RuntimeException(e);
			}
		}
		
		return this;
	}
	
	private @Nullable Field findConfigField(String name) {
		try {
			Field field = this.getClass().getDeclaredField(name);
			//Skip static, final, and transient fields
			if((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT)) != 0) return null;
			
			field.setAccessible(true);
			return field;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}
	
	//Save the config file to this path.
	protected Config save(Path configFilePath) throws IOException {
		Config defaultConfig;
		try {
			defaultConfig = this.getClass().getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Can't instantiate the default copy of " + this.getClass().toGenericString());
		}
		
		List<String> lines = new ArrayList<>();
		
		for(Field field : this.getClass().getDeclaredFields()) {
			//Skip static, final, and transient fields.
			if((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT)) != 0) continue;
			
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
			
			AtMost atMost = field.getDeclaredAnnotation(AtMost.class);
			if(atMost != null) {
				if(atMost.maxInt() != Integer.MAX_VALUE) {
					lines.add("# Must be at most " + atMost.maxInt() + ".");
				} else if(atMost.maxLong() != Long.MAX_VALUE) {
					lines.add("# Must be at most " + atMost.maxLong() + ".");
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
