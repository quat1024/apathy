package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.util.TriState;

public class AlwaysRuleSpec implements RuleSpec {
	public static final AlwaysRuleSpec ALWAYS_ALLOW = new AlwaysRuleSpec(TriState.TRUE);
	public static final AlwaysRuleSpec ALWAYS_DENY = new AlwaysRuleSpec(TriState.FALSE);
	public static final AlwaysRuleSpec ALWAYS_PASS = new AlwaysRuleSpec(TriState.DEFAULT);
	
	public static AlwaysRuleSpec always(TriState which) {
		switch(which) {
			case FALSE:
				return ALWAYS_DENY;
			case DEFAULT:
				return ALWAYS_PASS;
			case TRUE:
				return ALWAYS_ALLOW;
			default:
				throw new IllegalStateException(which.name());
		}
	}
	
	private AlwaysRuleSpec(TriState value) {
		this.value = value;
	}
	
	public static final Codec<AlwaysRuleSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.TRISTATE_ALLOW_DENY_PASS.fieldOf("value").forGetter(a -> a.value)
	).apply(i, AlwaysRuleSpec::always));
	
	public final TriState value;
	
	@Override
	public Rule build() {
		return (attacker, defender) -> value;
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}
