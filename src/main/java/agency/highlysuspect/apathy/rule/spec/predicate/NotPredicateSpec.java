package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.spec.Specs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public final class NotPredicateSpec implements PredicateSpec {
	
	public static final Codec<NotPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Specs.PREDICATE_SPEC_CODEC.fieldOf("predicate").forGetter(x -> x.other)
	).apply(i, NotPredicateSpec::new));
	private final PredicateSpec other;
	
	public NotPredicateSpec(PredicateSpec other) {this.other = other;}
	
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
	
	public PredicateSpec other() {return other;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (NotPredicateSpec) obj;
		return Objects.equals(this.other, that.other);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(other);
	}
	
	@Override
	public String toString() {
		return "NotPredicateSpec[" +
			"other=" + other + ']';
	}
	
}
