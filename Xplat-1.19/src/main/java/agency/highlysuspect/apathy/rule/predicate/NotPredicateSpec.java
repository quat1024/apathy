package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.rule.Specs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NotPredicateSpec(PartialSpec other) implements PartialSpec {
	public static final Codec<NotPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.other)
	).apply(i, NotPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		if(other == PartialSpecAlways.FALSE) return PartialSpecAlways.TRUE;
		if(other == PartialSpecAlways.TRUE) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		Partial built = other.build();
		return (attacker, defender) -> !built.test(attacker, defender);
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
