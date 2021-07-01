package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;

public interface PredicateSpec {
	default PredicateSpec optimize() {
		return this;
	}
	
	Partial build();
	Codec<? extends PredicateSpec> codec();
	
	PredicateSpec ALWAYS_TRUE = AlwaysPredicateSpec.TRUE;
	PredicateSpec ALWAYS_FALSE = AlwaysPredicateSpec.FALSE;
	
	default boolean isAlwaysTrue() {
		return this == ALWAYS_TRUE;
	}
	
	default boolean isAlwaysFalse() {
		return this == ALWAYS_FALSE;
	}
}
