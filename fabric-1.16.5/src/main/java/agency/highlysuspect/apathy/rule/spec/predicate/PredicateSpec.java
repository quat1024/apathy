package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;

import java.util.Collection;
import java.util.function.Function;

public interface PredicateSpec {
	default PredicateSpec optimize() {
		return this;
	}
	
	Partial build();
	Codec<? extends PredicateSpec> codec();
	
	static <X, R, C extends Collection<X>> R sizeSpecializeNotEmpty(C things, Function<X, R> single, Function<C, R> multi) {
		if(things.size() == 0) {
			throw new IllegalArgumentException("Empty collection");
		} else if(things.size() == 1) {
			return single.apply(things.iterator().next());
		} else {
			return multi.apply(things);
		}
	}
}
