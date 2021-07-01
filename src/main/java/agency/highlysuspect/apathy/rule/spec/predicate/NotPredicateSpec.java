package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.spec.Specs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NotPredicateSpec implements PredicateSpec {
	public NotPredicateSpec(PredicateSpec other) {
		this.other = other;
	}
	
	private final PredicateSpec other;
	
	public static final Codec<NotPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("value").forGetter(x -> x.other)
	).apply(i, NotPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(other.isAlwaysFalse()) return ALWAYS_TRUE;
		if(other.isAlwaysTrue()) return ALWAYS_FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		Partial built = other.build();
		return (attacker, defender) -> !built.test(attacker, defender);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
