package agency.highlysuspect.apathy.hell.rule;

import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.wrapper.ApathyDifficulty;
import com.google.gson.JsonObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record RuleSpecDifficultyCase(Map<ApathyDifficulty, RuleSpec<?>> ruleSpecs) implements RuleSpec<RuleSpecDifficultyCase> {
	@Override
	public RuleSpec<?> optimize() {
		return new RuleSpecDifficultyCase(ruleSpecs.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().optimize())));
	}
	
	@Override
	public Rule build() {
		Map<ApathyDifficulty, Rule> built = new EnumMap<>(ApathyDifficulty.class);
		ruleSpecs.forEach((difficulty, ruleSpec) -> built.put(difficulty, ruleSpec.build()));
		
		return (attacker, defender) -> built.getOrDefault(attacker.apathy$getDifficulty(), alwaysPasses).apply(attacker, defender);
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
			rule.ruleSpecs.forEach((difficulty, diffRule) -> cases.add(difficulty.toString(), ApathyHell.instance.writeRule(diffRule)));
			json.add("cases", cases);
		}
		
		@Override
		public RuleSpecDifficultyCase read(JsonObject json) {
			Map<ApathyDifficulty, RuleSpec<?>> ruleSpecs = new HashMap<>();
			
			JsonObject cases = json.getAsJsonObject("cases");
			for(String key : cases.keySet()) {
				ApathyDifficulty diff = ApathyDifficulty.fromStringOrNull(key);
				if(diff == null) continue;
				
				ruleSpecs.put(diff, ApathyHell.instance.readRule(cases.getAsJsonObject(key)));
			}
			
			return new RuleSpecDifficultyCase(ruleSpecs);
		}
	}
}
