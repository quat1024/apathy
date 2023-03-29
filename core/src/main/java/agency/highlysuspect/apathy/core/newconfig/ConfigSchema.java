package agency.highlysuspect.apathy.core.newconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigSchema {
	List<Object> entries = new ArrayList<>();
	
	public void section(String sectionName) {
		entries.add(sectionName);
	}
	
	public void option(ConfigProperty<?>... options) {
		entries.addAll(Arrays.asList(options));
	}
	
	public void section(String sectionName, ConfigProperty<?>... options) {
		section(sectionName);
		option(options);
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
