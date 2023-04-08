package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.core.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecDifficultyIs(Set<ApathyDifficulty> difficulties) implements Spec<Partial, PartialSpecDifficultyIs> {
	@Override
	public Spec<Partial, ?> optimize() {
		if(difficulties.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> difficulties.contains(attacker.apathy$getDifficulty());
	}
	
	@Override
	public JsonSerializer<PartialSpecDifficultyIs> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecDifficultyIs> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecDifficultyIs thing, JsonObject json) {
			json.add("difficulties", thing.difficulties.stream()
				.map(ApathyDifficulty::toString)
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecDifficultyIs read(JsonObject json) {
			return new PartialSpecDifficultyIs(StreamSupport.stream(json.getAsJsonArray("difficulties").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(ApathyDifficulty::fromString)
				.collect(Collectors.toSet()));
		}
	}
}
