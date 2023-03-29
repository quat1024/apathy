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
	
	public T register(String name, T thing) {
		byName.put(name, thing);
		byThing.put(thing, name);
		return thing;
	}
	
	public T get(String name) {
		return byName.get(name);
	}
	
	public String getName(T thing) {
		return byThing.get(thing);
	}
}