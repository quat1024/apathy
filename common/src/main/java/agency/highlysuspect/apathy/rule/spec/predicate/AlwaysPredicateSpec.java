package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AlwaysPredicateSpec(boolean always) implements PredicateSpec {
	public static final AlwaysPredicateSpec TRUE = new AlwaysPredicateSpec(true);
	public static final AlwaysPredicateSpec FALSE = new AlwaysPredicateSpec(false);
	
	public static AlwaysPredicateSpec get(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	public static final Codec<AlwaysPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.BOOL.fieldOf("value").forGetter(x -> x.always)
	).apply(i, AlwaysPredicateSpec::get));
	
	@Override
	public Partial build() {
		return (attacker, defender) -> always;
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
