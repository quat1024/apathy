package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PartialSpecAttackerIs implements Spec<Partial, PartialSpecAttackerIs> {
	public PartialSpecAttackerIs(Set<AttackerType> mobSet) {
		this.mobSet = mobSet;
	}
	
	public final Set<AttackerType> mobSet;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(mobSet.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> mobSet.contains(attacker.apathy$getType());
	}
	
	@Override
	public JsonSerializer<PartialSpecAttackerIs> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAttackerIs> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerIs thing, JsonObject json) {
			json.add("mobs", thing.mobSet.stream()
				.map(AttackerType::apathy$id)
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecAttackerIs read(JsonObject json) {
			return new PartialSpecAttackerIs(StreamSupport.stream(json.getAsJsonArray("mobs").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(Apathy.instance::parseAttackerType)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet()));
		}
	}
}
