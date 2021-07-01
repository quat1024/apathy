package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;

public class AttackerIsBossPredicateSpec extends PredicateSpec {
	public static final AttackerIsBossPredicateSpec INSTANCE = new AttackerIsBossPredicateSpec();
	public static final Codec<AttackerIsBossPredicateSpec> CODEC = Codec.unit(INSTANCE);
	
	@Override
	public Partial buildPartial() {
		return Partial.attackerIsBoss();
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
