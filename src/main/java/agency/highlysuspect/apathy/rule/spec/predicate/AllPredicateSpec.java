package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.spec.Specs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AllPredicateSpec implements PredicateSpec {
	public AllPredicateSpec(Set<PredicateSpec> others) {
		this.others = others;
	}
	
	private final Set<PredicateSpec> others;
	
	public static final Codec<AllPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Specs.PREDICATE_SPEC_CODEC).fieldOf("values").forGetter(x -> x.others)
	).apply(i, AllPredicateSpec::new));
	
	@Override
	public Partial build() {
		Set<Partial> builtParts = others.stream().map(PredicateSpec::build).collect(Collectors.toSet());
		
		//If an always-false predicate is here, surely this predicate will never match.
		if(builtParts.stream().anyMatch(part -> part == Partial.ALWAYS_FALSE)) return Partial.ALWAYS_FALSE;
		
		//If an always-true predicate is here, it can be ignored
		builtParts.removeIf(part -> part == Partial.ALWAYS_TRUE);
		
		//If there are no specs left, ?? always fail I guess??
		if(builtParts.size() == 0) return Partial.ALWAYS_FALSE;
		
		//If there is one spec left, we don't need the wrapping
		if(builtParts.size() == 1) return builtParts.iterator().next();
		
		Partial[] arrayParts = builtParts.toArray(new Partial[0]);
		return (attacker, defender) -> {
			for(Partial p : arrayParts) {
				if(!p.test(attacker, defender)) return false;
			}
			return true;
		};
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
