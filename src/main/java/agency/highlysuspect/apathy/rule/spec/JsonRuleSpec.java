package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.JsonRule;
import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.util.TriState;

public final class JsonRuleSpec implements RuleSpec {
	public static final JsonRuleSpec INSTANCE = new JsonRuleSpec();
	public static final Codec<JsonRuleSpec> CODEC = Codec.unit(INSTANCE);
	
	public JsonRuleSpec() {}
	
	@Override
	public Rule build() {
		return (attacker, defender) -> JsonRule.jsonRule == null ? TriState.DEFAULT : JsonRule.jsonRule.apply(attacker, defender);
	}
	
	@Override
	public Codec<? extends RuleSpec> codec() {
		return CODEC;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || obj != null && obj.getClass() == this.getClass();
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public String toString() {
		return "JsonRuleSpec[]";
	}
	
}
