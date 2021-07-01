package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;
import java.util.stream.Collectors;

public class AnyPredicateSpec extends PredicateSpec {
	public AnyPredicateSpec(Set<PredicateSpec> others) {
		this.others = others;
	}
	
	private final Set<PredicateSpec> others;
	
	public static final Codec<AnyPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(PredicateSpec.SPEC_CODEC).fieldOf("values").forGetter(x -> x.others)
	).apply(i, AnyPredicateSpec::new));
	
	@Override
	public Partial buildPartial() {
		return Partial.any(others.stream().map(PredicateSpec::buildPartial).collect(Collectors.toList()));
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
