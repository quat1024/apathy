package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;

public final record JsonRuleSpec() implements RuleSpec {
	public static final JsonRuleSpec INSTANCE = new JsonRuleSpec();
	public static final Codec<JsonRuleSpec> CODEC = Codec.unit(INSTANCE);
	
	@Override
	public Rule build() {
		return (attacker, defender) -> Apathy119.instance119.jsonRule == null ? TriState.DEFAULT : Apathy119.instance119.jsonRule.apply(attacker, defender);
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
}