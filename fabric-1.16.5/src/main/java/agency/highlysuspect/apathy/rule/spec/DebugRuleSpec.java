package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

public class DebugRuleSpec implements RuleSpec {
	public DebugRuleSpec(RuleSpec rule, String message) {
		this.rule = rule;
		this.message = message;
	}
	
	private final RuleSpec rule;
	private final String message;
	
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
			Init.LOG.info("rule: " + message);
			TriState result = built.apply(attacker, defender);
			Init.LOG.info("returned: " + showTriState(result));
			return result;
		};
	}
	
	private static String showTriState(TriState state) {
		switch(state) {
			case FALSE: return "deny";
			case DEFAULT: return "pass";
			case TRUE: return "allow";
			default: throw new IllegalStateException(state.toString());
		}
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
