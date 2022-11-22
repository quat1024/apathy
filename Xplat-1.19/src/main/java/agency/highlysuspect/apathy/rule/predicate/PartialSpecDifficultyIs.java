package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.hell.rule.CoolGsonHelper;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.world.Difficulty;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record PartialSpecDifficultyIs(Set<Difficulty> difficulties) implements PartialSpec<PartialSpecDifficultyIs> {
	@Override
	public PartialSpec<?> optimize() {
		if(difficulties.isEmpty()) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> difficulties.contains(attacker.level.getDifficulty());
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
				.map(Serializer::difficultyToString)
				.map(JsonPrimitive::new)
				.collect(CoolGsonHelper.toJsonArray()));
		}
		
		@Override
		public PartialSpecDifficultyIs read(JsonObject json) {
			return new PartialSpecDifficultyIs(StreamSupport.stream(json.getAsJsonArray("difficulties").spliterator(), false)
				.map(JsonElement::getAsString)
				.map(Serializer::difficultyFromString)
				.collect(Collectors.toSet()));
		}
		
		private static String difficultyToString(Difficulty d) {
			return d.getKey();
		}
		
		private static Difficulty difficultyFromString(String s) {
			Difficulty d = Difficulty.byName(s.toLowerCase(Locale.ROOT));
			if(d == null) throw new IllegalArgumentException(s + " is not a difficulty name.");
			return d;
		}
	}
}