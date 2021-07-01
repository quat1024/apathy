package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class AlwaysPredicateSpec implements PredicateSpec {
	public static final AlwaysPredicateSpec TRUE = new AlwaysPredicateSpec(true);
	public static final AlwaysPredicateSpec FALSE = new AlwaysPredicateSpec(false);
	
	public static AlwaysPredicateSpec get(boolean b) {
		return b ? TRUE : FALSE;
	}
	
	private AlwaysPredicateSpec(boolean always) {
		this.always = always;
	}
	
	private final boolean always;
	
	public static final Codec<AlwaysPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.BOOL.fieldOf("value").forGetter(x -> x.always)
	).apply(i, AlwaysPredicateSpec::get));
	
	@Override
	public Partial build() {
		return Partial.always(always);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
