package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.spec.Specs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NotPredicateSpec(PredicateSpec other) implements PredicateSpec {
	
	public static final Codec<NotPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.other)
	).apply(i, NotPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(other == AlwaysPredicateSpec.FALSE) return AlwaysPredicateSpec.TRUE;
		if(other == AlwaysPredicateSpec.TRUE) return AlwaysPredicateSpec.FALSE;
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
