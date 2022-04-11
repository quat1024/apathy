package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record LocationPredicateSpec(LocationPredicate pred, LocationGetter who, int offsetX, int offsetY, int offsetZ) implements PredicateSpec {
	public static final Codec<LocationPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.LOCATION_PREDICATE_CODEC.fieldOf("predicate").forGetter(LocationPredicateSpec::pred),
		LocationGetter.CODEC.optionalFieldOf("who", LocationGetter.ATTACKER_SPAWN_LOCATION).forGetter(LocationPredicateSpec::who),
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
			 
			 Vec3 vec = who.chooseLocation(attacker, defender);
			 BlockPos pos = new BlockPos(vec);
			 return slevel.isLoaded(pos) && pred.matches(slevel, vec.x + offsetX, vec.y + offsetY, vec.z + offsetZ);
		 };
	}
	
	@Override
	public Codec<? extends PredicateSpec> codec() {
		return CODEC;
	}
	
	//idk
	public enum LocationGetter {
		ATTACKER,
		ATTACKER_SPAWN_LOCATION,
		DEFENDER;
		
		public Vec3 chooseLocation(Mob attacker, ServerPlayer defender) {
			return switch(this) {
				case ATTACKER -> attacker.position();
				case ATTACKER_SPAWN_LOCATION -> {
					if(attacker instanceof MobExt ext) {
						Vec3 spawnPosition = ext.apathy$getSpawnPosition();
						if(spawnPosition != null) yield spawnPosition;
					}
					
					//fallback
					yield attacker.position();
				}
				case DEFENDER -> defender.position();
			};
		}
		
		public static final Codec<LocationGetter> CODEC = CodecUtil.enumCodec("LocationGetter", LocationGetter.class);
	}
}
