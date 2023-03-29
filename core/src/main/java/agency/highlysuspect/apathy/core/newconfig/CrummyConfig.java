package agency.highlysuspect.apathy.core.newconfig;

import agency.highlysuspect.apathy.core.ApathyHell;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CrummyConfig implements CookedConfig {
	public CrummyConfig(ConfigSchema schema, Path path) {
		this.schema = schema;
		this.path = path;
	}
	
	private final ConfigSchema schema;
	private final Path path;
	
	private final Map<ConfigProperty<?>, Object> parsedValues = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(ConfigProperty<T> key) {
		return (T) parsedValues.computeIfAbsent(key, ConfigProperty::defaultValue);
	}
	
	@Override
	public boolean refresh() {
		try {
			parse();
			write();
			return true;
		} catch (Exception e) {
			ApathyHell.instance.log.error("Problem loading config at " + path + ": " + e.getMessage(), e);
			return false;
		}
	}
	
	public void parse() throws IOException {
		//CrummyConfig#get() has "get-or-default" semantics - it's fine to leave the map empty
		if(Files.notExists(path)) return;
		
		parsedValues.clear();
		
		Map<String, ConfigProperty<?>> props = schema.propertiesByName();
		
		Iterator<String> lineserator = Files.readAllLines(path, StandardCharsets.UTF_8).iterator();
		int lineNo = 0;
		while(lineserator.hasNext()) {
			lineNo++;
			String line = lineserator.next().trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			int colonIdx = line.indexOf(':');
			if(colonIdx == -1) {
				throw new IllegalArgumentException("On line " + lineNo + ", there's no colon to split a key-value pair.");
			}
			String key = line.substring(0, colonIdx).trim();
			String value = line.substring(colonIdx + 1).trim();
			
			ConfigProperty<?> prop = props.get(key);
			if(prop == null) {
				//TODO: keep track of these?
				ApathyHell.instance.log.warn("On line " + lineNo + ", there's no option named '" + key + "'.");
				continue;
			}
			
			Object parsed;
			try {
				parsed = parse(prop, value);
			} catch (Exception e) {
				throw new IllegalArgumentException("On line " + lineNo + ", there was a failure to parse option " + key + ": " + e.getMessage(), e);
			}
			
			parsedValues.put(prop, parsed);
		}
	}
	
	public void write() throws IOException {
		List<String> out = new ArrayList<>();
		
		schema.accept(new ConfigSchema.Visitor() {
			@Override
			public void visitSection(String section) {
				//String bar = "#".repeat(section.length() + 6); //Not in Java 8
				@SuppressWarnings("SuspiciousRegexArgument") //i really do want to replace every character
				String bar = "######" + section.replaceAll(".", "#");
				out.add(bar);
				out.add("## " + section + " ##");
				out.add(bar);
				out.add("");
			}
			
			@Override
			public <T> void visitOption(ConfigProperty<T> option) {
				for(String s : option.comment()) {
					out.add("# " + s);
				}
				
				List<String> note = option.note();
				boolean first = true;
				for(String noteLine : note) {
					out.add((first ? "# Note: " : "#       ") + noteLine);
					first = false;
				}
				
				List<String> example = option.example();
				first = true;
				for(String exampleLine : example) {
					out.add((first ? "# Example: " : "#          ") + exampleLine);
					first = false;
				}
				
				if(!option.name().equals("configVersion")) { //silly special-case
					T defaultValue = option.defaultValue();
					String writtenDefaultValue = option.write(defaultValue);
					if(writtenDefaultValue.isEmpty()) writtenDefaultValue = "<empty>";
					out.add("# Default: " + writtenDefaultValue);
				}
				
				T currentValue = get(option);
				String writtenCurrentValue = option.write(currentValue);
				out.add(option.name() + ": " + writtenCurrentValue);
				
				out.add("");
			}
		});
		
		Files.write(path, out, StandardCharsets.UTF_8);
	}
	
	private <T> T parse(ConfigProperty<T> prop, String value) {
		T parsed = prop.parse(value);
		prop.validate(parsed);
		return parsed;
	}
	
	public static class Bakery implements ConfigSchema.Bakery {
		public Bakery(Path path) {
			this.path = path;
		}
		
		private final Path path;
		
		@Override
		public CookedConfig cook(ConfigSchema schema) {
			return new CrummyConfig(schema, path);
		}
	}
}
