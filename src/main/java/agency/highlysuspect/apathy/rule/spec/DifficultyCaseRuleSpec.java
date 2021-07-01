package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class DifficultyCaseRuleSpec implements RuleSpec {
	public DifficultyCaseRuleSpec(Map<Difficulty, RuleSpec> ruleSpecs) {
		this.ruleSpecs = ruleSpecs;
	}
	
	private final Map<Difficulty, RuleSpec> ruleSpecs;
	
	public static final Codec<DifficultyCaseRuleSpec> CODEC = Codec.unboundedMap(CodecUtil.DIFFICULTY, Specs.RULE_SPEC_CODEC)
		.xmap(DifficultyCaseRuleSpec::new, x -> x.ruleSpecs);
	
	@Override
	public Rule build() {
		Map<Difficulty, Rule> built = new EnumMap<>(Difficulty.class);
		ruleSpecs.forEach((difficulty, ruleSpec) -> built.put(difficulty, ruleSpec.build()));
		
		return (attacker, defender) -> built.getOrDefault(attacker.world.getDifficulty(), Rule.ALWAYS_PASS).apply(attacker, defender); 
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
