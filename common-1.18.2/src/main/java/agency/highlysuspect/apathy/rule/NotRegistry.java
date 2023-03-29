package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Portage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class NotRegistry<T> {
	public NotRegistry(String name) {
		this.name = name;
	}
	
	private final String name;
	private final Map<String, T> byName = new LinkedHashMap<>();
	private final Map<T, String> byThing = new LinkedHashMap<>();
	
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
	
	///
	
	public DataResult<T> getOrFail(String name) {
		T result = get(name);
		if(result == null) return Portage.dataResultError("Unknown item " + name + " in NotRegistry " + this.name + ". Valid items: " + String.join(", ", byName.keySet()));
		else return DataResult.success(result);
	}
	
	public Codec<T> byNameCodec() {
		//no DataResult on the right half, i would simply never write a bug and make an unregistered item :clueless:
		return Codec.STRING.comapFlatMap(this::getOrFail, this::getName);
	}
}
