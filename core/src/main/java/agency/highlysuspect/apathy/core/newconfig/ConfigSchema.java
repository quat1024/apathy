package agency.highlysuspect.apathy.core.newconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConfigSchema {
	List<Object> entries = new ArrayList<>();
	
	public void section(String sectionName) {
		entries.add(sectionName);
	}
	
	public void option(ConfigProperty<?> option) {
		entries.add(option);
	}
	
	public <B extends ConfigProperty.Builder<?, B>> void option(B builder) {
		option(builder.build());
	}
	
	public void section(String sectionName, Consumer<Consumer<ConfigProperty<?>>> optionsInSection) {
		section(sectionName);
		optionsInSection.accept(this::option);
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
	
	public void accept(Visitor visitor) {
		for(Object thing : entries) {
			if(thing instanceof String) visitor.visitSection((String) thing);
			else if(thing instanceof ConfigProperty<?>) visitor.visitOption((ConfigProperty<?>) thing);
		}
	}
	
	public interface Visitor {
		default void visitSection(String section) {}
		default <T> void visitOption(ConfigProperty<T> option) {}
	}
	
	public interface Bakery {
		CookedConfig cook(ConfigSchema schema);
	}
}
