package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NotPredicateSpec extends PredicateSpec {
	public NotPredicateSpec(PredicateSpec other) {
		this.other = other;
	}
	
	private final PredicateSpec other;
	
	public static final Codec<NotPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		PredicateSpec.SPEC_CODEC.fieldOf("value").forGetter(x -> x.other)
	).apply(i, NotPredicateSpec::new)); 
	
	@Override
	public Partial buildPartial() {
		return other.buildPartial().not();
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
