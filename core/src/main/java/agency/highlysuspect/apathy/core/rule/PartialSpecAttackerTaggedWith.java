package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class PartialSpecAttackerTaggedWith implements Spec<Partial, PartialSpecAttackerTaggedWith> {
	public PartialSpecAttackerTaggedWith(Set<AttackerTag> tags) {
		this.tags = tags;
	}
	
	public final Set<AttackerTag> tags;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(tags.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			for(AttackerTag tag : tags) if(tag.apathy$is(attacker)) return true;
			return false;
		};
	}
	
	@Override
	public JsonSerializer<PartialSpecAttackerTaggedWith> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecAttackerTaggedWith> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecAttackerTaggedWith thing, JsonObject json) {
			json.add("tags", thing.tags.stream()
				.map(AttackerTag::apathy$id)
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecAttackerTaggedWith read(JsonObject json) {
			return new PartialSpecAttackerTaggedWith(StreamSupport.stream(json.getAsJsonArray("tags").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(Apathy.instance::parseAttackerTag)
				.collect(Collectors.toSet()));
		}
	}
}
