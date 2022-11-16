package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Specs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;
import java.util.stream.Collectors;

public record AnyPredicateSpec(Set<PartialSpec> others) implements PartialSpec {
	public static final Codec<AnyPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Specs.PREDICATE_SPEC_CODEC).fieldOf("predicates").forGetter(x -> x.others)
	).apply(i, AnyPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		Set<PartialSpec> loweredSpecs = others.stream().map(PartialSpec::optimize).collect(Collectors.toSet());
		
		//If an always-true spec is present, surely this spec is also always true.
		if(loweredSpecs.stream().anyMatch(pred -> pred == PartialSpecAlways.TRUE)) return PartialSpecAlways.TRUE;
		
		//Always-false specs can be ignored.
		loweredSpecs.removeIf(pred -> pred == PartialSpecAlways.FALSE);
		
		//If there are no specs left, uhh
		if(loweredSpecs.size() == 0) return PartialSpecAlways.FALSE;
		
		//If there is one spec left, we don't need the wrapping
		if(loweredSpecs.size() == 1) return loweredSpecs.iterator().next();
		
		return new AnyPredicateSpec(loweredSpecs);
	}
	
	@Override
	public Partial build() {
		Partial[] arrayParts = others.stream().map(PartialSpec::build).toArray(Partial[]::new);
		return (attacker, defender) -> {
			for(Partial p : arrayParts) {
				if(p.test(attacker, defender)) return true;
			}
			return false;
		};
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
