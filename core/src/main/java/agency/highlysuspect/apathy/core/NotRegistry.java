package agency.highlysuspect.apathy.core;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Really really basic registry type.
 */
public class NotRegistry<T> {
	protected final Map<String, T> byName = new LinkedHashMap<>();
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
}