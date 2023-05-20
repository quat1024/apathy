package agency.highlysuspect.apathy.core;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Really really basic registry type, string keys.
 * 
 * This also lets callers refer to things with or without a prefix of "apathy:".
 * This prefix was formerly required in the mod's registries, but it's completely historical vestige, so it's optional now
 */
public class NotRegistry<T> {
	protected final Map<String, T> byName = new HashMap<>();
	protected final Map<T, String> byThing = new IdentityHashMap<>();
	
	private static final String OPTIONAL_PREFIX = "apathy:";
	private static final int OPTIONAL_PREFIX_LENGTH = OPTIONAL_PREFIX.length();
	
	public T register(String name, T thing) {
		if(name.startsWith(OPTIONAL_PREFIX)) name = name.substring(OPTIONAL_PREFIX_LENGTH);
		
		byName.put(name, thing);
		byThing.put(thing, name);
		return thing;
	}
	
	public T get(String name) {
		if(name.startsWith(OPTIONAL_PREFIX)) name = name.substring(OPTIONAL_PREFIX_LENGTH);
		
		return byName.get(name);
	}
	
	public String getName(T thing) {
		return byThing.get(thing);
	}
	
	public Set<String> names() {
		return byName.keySet();
	}
	
	//weird time
	
	public void unregister(String name) {
		if(name.startsWith(OPTIONAL_PREFIX)) name = name.substring(OPTIONAL_PREFIX_LENGTH);
		
		T thing = get(name);
		if(thing == null) return;
		byThing.remove(thing);
		byName.remove(name);
	}
}