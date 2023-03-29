package agency.highlysuspect.apathy.core.newconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Newconfig {
	List<Object> entries = new ArrayList<>();
	Map<ConfigProperty<?>, ?> parsedConfigValues = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public <T> T get(ConfigProperty<T> prop) {
		return (T) parsedConfigValues.get(prop);
	}
}
