package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.TriState;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DebugRuleSpec(RuleSpec rule, String message) implements RuleSpec {
	public static final Codec<DebugRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.RULE_SPEC_CODEC.fieldOf("rule").forGetter(x -> x.rule),
		Codec.STRING.fieldOf("message").forGetter(x -> x.message)
	).apply(i, DebugRuleSpec::new));
	
	@Override
	public RuleSpec optimize() {
		return new DebugRuleSpec(rule.optimize(), message);
	}
	
	@Override
	public Rule build() {
		Rule built = rule.build();
		
		return (attacker, defender) -> {
			Apathy119.LOG.info("rule: " + message);
			TriState result = built.apply(attacker, defender);
			Apathy119.LOG.info("returned: " + showTriState(result));
			return result;
		};
	}
	
	private static String showTriState(TriState state) {
		return switch(state) {
			case FALSE -> "deny";
			case DEFAULT -> "pass";
			case TRUE -> "allow";
		};
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
