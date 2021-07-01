package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RevengeTimerPredicateSpec extends PredicateSpec {
	public RevengeTimerPredicateSpec(long timer) {
		this.timer = timer;
	}
	
	private final long timer;
	public static final Codec<RevengeTimerPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.LONG.fieldOf("timer").forGetter(x -> x.timer)
	).apply(i, RevengeTimerPredicateSpec::new));
	
	@Override
	public Partial buildPartial() {
		return Partial.revengeTimer(timer);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return null;
	}
}
