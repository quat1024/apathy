package agency.highlysuspect.apathy.rule.spec.predicate;

import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.TriState;
import agency.highlysuspect.apathy.rule.CodecUtil;
import agency.highlysuspect.apathy.rule.Partial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public record LocationPredicateSpec(LocationPredicate pred, LocationGetter who, String uniqueId, int offsetX, int offsetY, int offsetZ) implements PredicateSpec {
	public static final Codec<LocationPredicateSpec> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.LOCATION_PREDICATE_CODEC.fieldOf("predicate").forGetter(LocationPredicateSpec::pred),
		LocationGetter.CODEC.optionalFieldOf("who", LocationGetter.ATTACKER_SPAWN_LOCATION).forGetter(LocationPredicateSpec::who),
		Codec.STRING.optionalFieldOf("uniqueId", "defaultUniqueId").forGetter(LocationPredicateSpec::uniqueId),
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
			
			return switch(who) {
				//Easy cases (that can't be cached anyways because the entities wander around the world)
				case ATTACKER -> test(slevel, pred, attacker.position());
				case DEFENDER -> test(slevel, pred, defender.position());
				//OK this one is fun!!
				case ATTACKER_SPAWN_LOCATION -> {
					//The spawn position is fixed, so there is no need to check the LocationPredicate every single tick
					//But more importantly, the entity might wander so far away from its spawn position that its not loaded anymore.
					//LocationPredicates return incorrect results for unloaded positions. I want to avoid the behavior of an entity
					//changing just because it walked a long distance. So, here we are
					
					//Look up the cached result. If one exists, yield it
					Map<String, TriState> cache = ((MobExt) attacker).apathy$getOrCreateLocationPredicateCache();
					TriState cachedResult = cache.getOrDefault(uniqueId, TriState.DEFAULT);
					if(cachedResult == TriState.TRUE) yield true;
					if(cachedResult == TriState.FALSE) yield false;
					
					//Begin computing the uncached result.
					//Look up the spawn position of this entity
					Vec3 vec = ((MobExt) attacker).apathy$getSpawnPosition();
					if(vec == null) {
						//The spawn position is unknown for this entity
						yield false;
					}
					
					//Compute and store the cached result, if the position is loaded
					if(slevel.isLoaded(new BlockPos(vec))) {
						boolean result = pred.matches(slevel, vec.x + offsetX, vec.y + offsetY, vec.z + offsetZ);
						cache.put(uniqueId, TriState.fromBoolean(result));
						yield result;
					}
					
					//If we're here, the spawn position is known, but it hasn't ever been loaded at the same time as this entity... somehow
					//Might be an entity that existed in a world created before the update that added this caching system.
					//We can't know for sure whether the location predicate passes or not so default to false
					yield false;
				}
			};
		};
	}
	
	private boolean test(ServerLevel slevel, LocationPredicate pred, Vec3 vec) {
		return pred.matches(slevel, vec.x + offsetX, vec.y + offsetY, vec.z + offsetZ);
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
		
		public static final Codec<LocationGetter> CODEC = CodecUtil.enumCodec("LocationGetter", LocationGetter.class);
	}
}
