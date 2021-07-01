package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.etc.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Set;

public class DefenderInPlayerSetPredicateSpec extends PredicateSpec {
	public DefenderInPlayerSetPredicateSpec(Set<String> playerSetNames) {
		this.playerSetNames = playerSetNames;
	}
	
	private final Set<String> playerSetNames;
	
	public static final Codec<DefenderInPlayerSetPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.setOf(Codec.STRING).fieldOf("player_sets").forGetter(x -> x.playerSetNames)
	).apply(i, DefenderInPlayerSetPredicateSpec::new));
	
	@Override
	public Partial buildPartial() {
		return Partial.inAnyPlayerSetNamed(playerSetNames);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
