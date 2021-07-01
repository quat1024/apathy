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
		Codec.LONG.fieldOf("timeout").forGetter(x -> x.timer)
	).apply(i, RevengeTimerPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(timer <= 0) return ALWAYS_FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> VengeanceHandler.lastAttackedWithin(attacker, timer);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
