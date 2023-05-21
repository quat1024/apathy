package agency.highlysuspect.apathy.core.rule;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class PartialSpecSpawnType implements Spec<Partial, PartialSpecSpawnType> {
	public PartialSpecSpawnType(Set<String> spawnTypes) {
		this.spawnTypes = spawnTypes;
	}
	
	private final Set<String> spawnTypes;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(spawnTypes.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		Set<String> spawnTypes = this.spawnTypes.size() == 1 ?
			Collections.singleton(this.spawnTypes.iterator().next()) :
			this.spawnTypes;
		
		return (attacker, defender) -> {
			@Nullable String spawnType = attacker.apathy$getSpawnType();
			if(spawnType == null) spawnType = "unknown";
			return spawnTypes.contains(spawnType);
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecSpawnType> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static final class Serializer implements JsonSerializer<PartialSpecSpawnType> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecSpawnType thing, JsonObject json) {
			json.add("types", thing.spawnTypes.stream().map(JsonPrimitive::new).collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecSpawnType read(JsonObject json) {
			return new PartialSpecSpawnType(CoolGsonHelper.streamArray(json.getAsJsonArray("types")).map(JsonElement::getAsString).collect(Collectors.toSet()));
		}
	}
}
