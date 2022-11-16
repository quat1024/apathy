package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record DifficultyCaseRuleSpec(Map<Difficulty, RuleSpec<?>> ruleSpecs) implements RuleSpec<DifficultyCaseRuleSpec> {
	@Override
	public RuleSpec<?> optimize() {
		return new DifficultyCaseRuleSpec(ruleSpecs.entrySet().stream()
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
	public RuleSerializer<DifficultyCaseRuleSpec> getSerializer() {
		return DifficultyCaseRuleSerializer.INSTANCE;
	}
	
	public static class DifficultyCaseRuleSerializer implements RuleSerializer<DifficultyCaseRuleSpec> {
		public static final DifficultyCaseRuleSerializer INSTANCE = new DifficultyCaseRuleSerializer();
		
		@Override
		public JsonObject write(DifficultyCaseRuleSpec rule, JsonObject json) {
			JsonObject cases = new JsonObject();
			rule.ruleSpecs.forEach((difficulty, diffRule) -> cases.add(difficulty.getKey(), Apathy119.instance119.writeRule(diffRule)));
			json.add("cases", cases);
			
			return json;
		}
		
		@Override
		public DifficultyCaseRuleSpec read(JsonObject json) {
			Map<Difficulty, RuleSpec<?>> ruleSpecs = new HashMap<>();
			
			JsonObject cases = json.getAsJsonObject("cases");
			for(String key : cases.keySet()) {
				Difficulty diff = Difficulty.byName(key);
				if(diff == null) continue;
				
				ruleSpecs.put(diff, Apathy119.instance119.readRule(cases.getAsJsonObject(key)));
			}
			
			return new DifficultyCaseRuleSpec(ruleSpecs);
		}
	}
	
	///CODEC HELLZONE///
	
	@Deprecated(forRemoval = true)
	public static final Codec<DifficultyCaseRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.unboundedMap(CodecUtil.DIFFICULTY, Specs.RULE_SPEC_CODEC).fieldOf("cases").forGetter(x -> x.ruleSpecs)
	).apply(i, DifficultyCaseRuleSpec::new));
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends RuleSpec<?>> codec() {
		return CODEC;
	}
}
