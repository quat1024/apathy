package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;

public interface PredicateSpec {
	Partial build();
	Codec<? extends PredicateSpec> codec();
}
