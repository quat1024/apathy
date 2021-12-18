package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public final class RevengeTimerPredicateSpec implements PredicateSpec {
	public static final Codec<RevengeTimerPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.LONG.fieldOf("timeout").forGetter(x -> x.timer)
	).apply(i, RevengeTimerPredicateSpec::new));
	private final long timer;
	
	public RevengeTimerPredicateSpec(long timer) {this.timer = timer;}
	
	@Override
	public PredicateSpec optimize() {
		if(timer <= 0) return AlwaysPredicateSpec.FALSE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> MobExt.cast(attacker).apathy$lastAttackedWithin(timer);
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	public long timer() {return timer;}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		var that = (RevengeTimerPredicateSpec) obj;
		return this.timer == that.timer;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(timer);
	}
	
	@Override
	public String toString() {
		return "RevengeTimerPredicateSpec[" +
			"timer=" + timer + ']';
	}
	
}
