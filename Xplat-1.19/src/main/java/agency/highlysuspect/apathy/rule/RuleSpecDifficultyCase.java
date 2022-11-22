package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import com.google.gson.JsonObject;
import net.minecraft.world.Difficulty;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record RuleSpecDifficultyCase(Map<Difficulty, RuleSpec<?>> ruleSpecs) implements RuleSpec<RuleSpecDifficultyCase> {
	@Override
	public RuleSpec<?> optimize() {
		return new RuleSpecDifficultyCase(ruleSpecs.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().optimize())));
	}
	
	@Override
	public Rule build() {
		Map<Difficulty, Rule> built = new EnumMap<>(Difficulty.class);
		ruleSpecs.forEach((difficulty, ruleSpec) -> built.put(difficulty, ruleSpec.build()));
		
		return (attacker, defender) -> built.getOrDefault(attacker.level.getDifficulty(), alwaysPasses).apply(attacker, defender);
	}
	
	private static final Rule alwaysPasses = (attacker, defender) -> TriState.DEFAULT;
	
	@Override
	public RuleSerializer<RuleSpecDifficultyCase> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements RuleSerializer<RuleSpecDifficultyCase> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecDifficultyCase rule, JsonObject json) {
			JsonObject cases = new JsonObject();
			rule.ruleSpecs.forEach((difficulty, diffRule) -> cases.add(difficulty.getKey(), Apathy119.instance119.writeRule(diffRule)));
			json.add("cases", cases);
		}
		
		@Override
		public RuleSpecDifficultyCase read(JsonObject json) {
			Map<Difficulty, RuleSpec<?>> ruleSpecs = new HashMap<>();
			
			JsonObject cases = json.getAsJsonObject("cases");
			for(String key : cases.keySet()) {
				Difficulty diff = Difficulty.byName(key);
				if(diff == null) continue;
				
				ruleSpecs.put(diff, Apathy119.instance119.readRule(cases.getAsJsonObject(key)));
			}
			
			return new RuleSpecDifficultyCase(ruleSpecs);
		}
	}
}
