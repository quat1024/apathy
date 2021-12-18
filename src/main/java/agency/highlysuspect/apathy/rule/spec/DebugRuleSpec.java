package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

import java.util.Objects;

public final class DebugRuleSpec implements RuleSpec {
	public static final Codec<DebugRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.RULE_SPEC_CODEC.fieldOf("rule").forGetter(x -> x.rule),
		Codec.STRING.fieldOf("message").forGetter(x -> x.message)
	).apply(i, DebugRuleSpec::new));
	private final RuleSpec rule;
	private final String message;
	
	public DebugRuleSpec(RuleSpec rule, String message) {
		this.rule = rule;
		this.message = message;
	}
	
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
	
	public RuleSpec rule() {return rule;}
	
	public String message() {return message;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (DebugRuleSpec) obj;
		return Objects.equals(this.rule, that.rule) &&
			Objects.equals(this.message, that.message);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(rule, message);
	}
	
	@Override
	public String toString() {
		return "DebugRuleSpec[" +
			"rule=" + rule + ", " +
			"message=" + message + ']';
	}
	
}
