package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public final class AlwaysPredicateSpec implements PredicateSpec {
	public static final AlwaysPredicateSpec TRUE = new AlwaysPredicateSpec(true);
	public static final AlwaysPredicateSpec FALSE = new AlwaysPredicateSpec(false);
	
	public static AlwaysPredicateSpec get(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	public static final Codec<AlwaysPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.BOOL.fieldOf("value").forGetter(x -> x.always)
	).apply(i, AlwaysPredicateSpec::get));
	private final boolean always;
	
	public AlwaysPredicateSpec(boolean always) {this.always = always;}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> always;
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	public boolean always() {return always;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (AlwaysPredicateSpec) obj;
		return this.always == that.always;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(always);
	}
	
	@Override
	public String toString() {
		return "AlwaysPredicateSpec[" +
			"always=" + always + ']';
	}
	
}
