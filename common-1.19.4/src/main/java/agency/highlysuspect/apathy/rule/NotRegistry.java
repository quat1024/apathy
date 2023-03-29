package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Portage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NotRegistry<T> {
	public NotRegistry(String name) {
		this.name = name;
	}
	
	private final String name;
	private final Map<ResourceLocation, T> byName = new LinkedHashMap<>();
	private final Map<T, ResourceLocation> byThing = new LinkedHashMap<>();
	
	public T register(ResourceLocation name, T thing) {
		byName.put(name, thing);
		byThing.put(thing, name);
		return thing;
	}
	
	public T get(ResourceLocation name) {
		return byName.get(name);
	}
	
	public ResourceLocation getName(T thing) {
		return byThing.get(thing);
	}
	
	///
	
	public DataResult<T> getOrFail(ResourceLocation name) {
		T result = get(name);
		if(result == null) {
			String validItems = byName.keySet().stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
			return Portage.dataResultError("Unknown item " + name + " in NotRegistry " + this.name + ". Valid items: " + validItems);
		}
		else return DataResult.success(result);
	}
	
	public Codec<T> byNameCodec() {
		//no DataResult on the right half, i would simply never write a bug and make an unregistered item :clueless:
		return ResourceLocation.CODEC.comapFlatMap(this::getOrFail, this::getName);
	}
}
