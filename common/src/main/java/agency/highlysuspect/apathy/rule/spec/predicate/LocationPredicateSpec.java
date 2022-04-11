package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import agency.highlysuspect.apathy.rule.Who;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record LocationPredicateSpec(LocationPredicate pred, Who who, int offsetX, int offsetY, int offsetZ) implements PredicateSpec {
	public static final Codec<LocationPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.LOCATION_PREDICATE_CODEC.fieldOf("predicate").forGetter(LocationPredicateSpec::pred),
		Who.CODEC.optionalFieldOf("who", Who.ATTACKER).forGetter(LocationPredicateSpec::who),
		Codec.INT.optionalFieldOf("offsetX", 0).forGetter(LocationPredicateSpec::offsetX),
		Codec.INT.optionalFieldOf("offsetY", 0).forGetter(LocationPredicateSpec::offsetY),
		Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(LocationPredicateSpec::offsetZ)
	).apply(i, LocationPredicateSpec::new));
	
	@Override
	public PredicateSpec optimize() {
		if(pred == LocationPredicate.ANY) return AlwaysPredicateSpec.TRUE;
		else return this;
	}
	
	@Override
	public Partial build() {
		 return (attacker, defender) -> {
			 Level level = defender.level;
			 if(!(level instanceof ServerLevel slevel)) return false;
			 
			 Entity which = who.choose(attacker, defender);
			 return pred.matches(slevel, which.getX() + offsetX, which.getY() + offsetY, which.getZ() + offsetZ);
		 };
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
}
