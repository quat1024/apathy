package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.TriState;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AlwaysRuleSpec(TriState value) implements RuleSpec {
	public static final AlwaysRuleSpec ALWAYS_ALLOW = new AlwaysRuleSpec(TriState.TRUE);
	public static final AlwaysRuleSpec ALWAYS_DENY = new AlwaysRuleSpec(TriState.FALSE);
	public static final AlwaysRuleSpec ALWAYS_PASS = new AlwaysRuleSpec(TriState.DEFAULT);
	
	public static AlwaysRuleSpec always(TriState which) {
		return switch(which) {
			case FALSE -> ALWAYS_DENY;
			case DEFAULT -> ALWAYS_PASS;
			case TRUE -> ALWAYS_ALLOW;
		};
	}
	
	public static final Codec<AlwaysRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.fieldOf("value").forGetter(a -> a.value)
	).apply(i, AlwaysRuleSpec::always));
	
	@Override
	public Rule build() {
		return (attacker, defender) -> value;
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
