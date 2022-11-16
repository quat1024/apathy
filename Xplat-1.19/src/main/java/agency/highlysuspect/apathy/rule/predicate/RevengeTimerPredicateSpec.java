package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.MobExt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RevengeTimerPredicateSpec(long timer) implements PartialSpec {
	public static final Codec<RevengeTimerPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.LONG.fieldOf("timeout").forGetter(x -> x.timer)
	).apply(i, RevengeTimerPredicateSpec::new));
	
	@Override
	public PartialSpec optimize() {
		if(timer <= 0) return PartialSpecAlways.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> ((MobExt) attacker).apathy$lastAttackedWithin(timer);
	}
	
	@Override
	public Codec<? extends PartialSpec> codec() {
		return CODEC;
	}
}
