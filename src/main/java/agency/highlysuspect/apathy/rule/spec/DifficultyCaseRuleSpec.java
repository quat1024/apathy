package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.world.Difficulty;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DifficultyCaseRuleSpec implements RuleSpec {
	//Doesn't seem to work
	//public static final Codec<DifficultyCaseRuleSpec> CODEC = Codec.unboundedMap(CodecUtil.DIFFICULTY, Specs.RULE_SPEC_CODEC).xmap(DifficultyCaseRuleSpec::new, x -> x.ruleSpecs);
	
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
		
		return (attacker, defender) -> built.getOrDefault(attacker.world.getDifficulty(), alwaysPasses).apply(attacker, defender);
	}
	
	private static final Rule alwaysPasses = (attacker, defender) -> TriState.DEFAULT;
	private final Map<Difficulty, RuleSpec> ruleSpecs;
	
	public DifficultyCaseRuleSpec(Map<Difficulty, RuleSpec> ruleSpecs) {this.ruleSpecs = ruleSpecs;}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
	
	public Map<Difficulty, RuleSpec> ruleSpecs() {return ruleSpecs;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (DifficultyCaseRuleSpec) obj;
		return Objects.equals(this.ruleSpecs, that.ruleSpecs);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(ruleSpecs);
	}
	
	@Override
	public String toString() {
		return "DifficultyCaseRuleSpec[" +
			"ruleSpecs=" + ruleSpecs + ']';
	}
	
}
