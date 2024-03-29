package agency.highlysuspect.apathy.core.rule;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import com.google.gson.JsonObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RuleSpecDifficultyCase implements Spec<Rule, RuleSpecDifficultyCase> {
	public RuleSpecDifficultyCase(Map<ApathyDifficulty, Spec<Rule, ?>> ruleSpecs) {
		this.ruleSpecs = ruleSpecs;
	}
	
	public final Map<ApathyDifficulty, Spec<Rule, ?>> ruleSpecs;
	private static final Rule alwaysPasses = RuleSpecAlways.ALWAYS_PASS.build();
	
	@Override
	public Spec<Rule, ?> optimize() {
		return new RuleSpecDifficultyCase(ruleSpecs.entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getKey, p -> p.getValue().optimize())));
	}
	
	@Override
	public Rule build() {
		Map<ApathyDifficulty, Rule> built = new EnumMap<>(ApathyDifficulty.class);
		ruleSpecs.forEach((difficulty, ruleSpec) -> built.put(difficulty, ruleSpec.build()));
		
		return (attacker, defender) -> built.getOrDefault(attacker.apathy$getDifficulty(), alwaysPasses).apply(attacker, defender);
	}
	
	@Override
	public JsonSerializer<RuleSpecDifficultyCase> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<RuleSpecDifficultyCase> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(RuleSpecDifficultyCase thing, JsonObject json) {
			JsonObject cases = new JsonObject();
			thing.ruleSpecs.forEach((difficulty, diffRule) -> cases.add(difficulty.toString(), Apathy.instance.writeRule(diffRule)));
			json.add("cases", cases);
		}
		
		@Override
		public RuleSpecDifficultyCase read(JsonObject json) {
			Map<ApathyDifficulty, Spec<Rule, ?>> ruleSpecs = new HashMap<>();
			
			JsonObject cases = json.getAsJsonObject("cases");
			for(String key : CoolGsonHelper.keySet(cases)) {
				ApathyDifficulty diff = ApathyDifficulty.fromStringOrNull(key);
				if(diff == null) continue;
				
				ruleSpecs.put(diff, Apathy.instance.readRule(cases.getAsJsonObject(key)));
			}
			
			return new RuleSpecDifficultyCase(ruleSpecs);
		}
	}
}
