package agency.highlysuspect.apathy.core.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigSchema {
	private final Map<String, List<ConfigProperty<?>>> entries = new LinkedHashMap<>();
	
	private static final String SECTIONLESS = "\ud83d\udc09";
	private String currentSection = SECTIONLESS;
	
	public void section(String sectionName) {
		currentSection = sectionName == null ? SECTIONLESS : sectionName;
	}
	
	public void option(ConfigProperty<?>... options) {
		getSection(currentSection).addAll(Arrays.asList(options));
	}
	
	public void section(String sectionName, ConfigProperty<?>... options) {
		section(sectionName);
		option(options);
	}
	
	///
	
	public List<ConfigProperty<?>> getSection(String name) {
		return entries.computeIfAbsent(name, __ -> new ArrayList<>());
	}
	
	///
	
	public interface Visitor {
		default void visitSection(String section) {}
		default <T> void visitOption(ConfigProperty<T> option) {}
	}
	
	public void accept(Visitor visitor) {
		entries.forEach((section, options) -> {
			if(!SECTIONLESS.equals(section)) visitor.visitSection(section);
			options.forEach(visitor::visitOption);
		});
	}
	
	public Map<String, ConfigProperty<?>> propertiesByName() {
		Map<String, ConfigProperty<?>> props = new LinkedHashMap<>();
		accept(new Visitor() {
			@Override
			public <T> void visitOption(ConfigProperty<T> option) {
				props.put(option.name(), option);
			}
		});
		return props;
	}
	
	///
	
	public interface Bakery {
		CookedConfig cook(ConfigSchema schema);
	}
}
