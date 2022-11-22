package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.Partial;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.hell.rule.PartialSpec;
import agency.highlysuspect.apathy.hell.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.hell.wrapper.ApathyDifficulty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecDifficultyIs(Set<ApathyDifficulty> difficulties) implements PartialSpec<PartialSpecDifficultyIs> {
	@Override
	public PartialSpec<?> optimize() {
		if(difficulties.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> difficulties.contains(attacker.apathy$getDifficulty());
	}
	
	@Override
	public PartialSerializer<PartialSpecDifficultyIs> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecDifficultyIs> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecDifficultyIs part, JsonObject json) {
			json.add("difficulties", part.difficulties.stream()
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
