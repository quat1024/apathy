package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RevengeTimerPredicateSpec(long timer) implements PredicateSpec {
	public static final Codec<RevengeTimerPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.LONG.fieldOf("timeout").forGetter(x -> x.timer)
	).apply(i, RevengeTimerPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(timer <= 0) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> ((MobExt) attacker).apathy$lastAttackedWithin(timer);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
