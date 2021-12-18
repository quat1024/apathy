package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.platform.TriState;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.Difficulty;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public record DifficultyCaseRuleSpec(Map<Difficulty, RuleSpec> ruleSpecs) implements RuleSpec {
	public static final Codec<DifficultyCaseRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.unboundedMap(CodecUtil.DIFFICULTY, Specs.RULE_SPEC_CODEC).fieldOf("cases").forGetter(x -> x.ruleSpecs)
	).apply(i, DifficultyCaseRuleSpec::new));
	
	@Override
	public RuleSpec optimize() {
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
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
