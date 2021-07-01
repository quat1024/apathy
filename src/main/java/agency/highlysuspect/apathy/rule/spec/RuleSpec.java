package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.rule.Rule;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.util.TriState;

public interface RuleSpec {
	default RuleSpec optimize() {
		return this;
	}
	
	Rule build();
	Codec<? extends RuleSpec> codec();
	
	static AlwaysRuleSpec always(TriState which) {
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
	
	AlwaysRuleSpec ALWAYS_ALLOW = new AlwaysRuleSpec(TriState.TRUE);
	AlwaysRuleSpec ALWAYS_DENY = new AlwaysRuleSpec(TriState.FALSE);
	AlwaysRuleSpec ALWAYS_PASS = new AlwaysRuleSpec(TriState.DEFAULT);
	
	default boolean alwaysAllows() {
		return this == ALWAYS_ALLOW;
	}
	
	default boolean alwaysDenies() {
		return this == ALWAYS_DENY;
	}
	
	default boolean alwaysPasses() {
		return this == ALWAYS_PASS;
	}
}
