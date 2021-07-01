package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.revenge.VengeanceHandler;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RevengeTimerPredicateSpec implements PredicateSpec {
	public RevengeTimerPredicateSpec(long timer) {
		this.timer = timer;
	}
	
	private final long timer;
	public static final Codec<RevengeTimerPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.LONG.fieldOf("timer").forGetter(x -> x.timer)
	).apply(i, RevengeTimerPredicateSpec::new));
	
	@Override
	public Partial build() {
		if(timer > 0)	return (attacker, defender) -> VengeanceHandler.lastAttackedWithin(attacker, timer);
		else return Partial.ALWAYS_FALSE;
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
