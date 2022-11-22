package agency.highlysuspect.apathy.rule.predicate;

import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.rule.CodecUtil;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Map;

public record PartialSpecLocation(LocationPredicate pred, LocationGetter who, String uniqueId, int offsetX, int offsetY, int offsetZ) implements PartialSpec<PartialSpecLocation> {
	@Override
	public PartialSpec<?> optimize() {
		if(pred == LocationPredicate.ANY) return PartialSpecAlways.TRUE;
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
					//The spawn position is fixed, so there is no need to check the LocationPredicate every single tick.
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
						boolean result = test(slevel, pred, vec);
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
	public PartialSerializer<PartialSpecLocation> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements PartialSerializer<PartialSpecLocation> {
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public JsonObject write(PartialSpecLocation part, JsonObject json) {
			json.add("predicate", part.pred.serializeToJson());
			
			json.addProperty("who", part.who.toString()); //optional but unconditionally serialized
			json.addProperty("uniqueId", part.uniqueId); //optional but unconditionally serialized
			
			if(part.offsetX != 0) json.addProperty("offsetX", part.offsetX);
			if(part.offsetY != 0) json.addProperty("offsetY", part.offsetY);
			if(part.offsetZ != 0) json.addProperty("offsetZ", part.offsetZ);
			
			return json;
		}
		
		@Override
		public PartialSpecLocation read(JsonObject json) {
			LocationPredicate pred = LocationPredicate.fromJson(json.get("predicate"));
			
			LocationGetter who = json.has("who") ? LocationGetter.fromString(json.get("who").getAsString()) : LocationGetter.ATTACKER_SPAWN_LOCATION;
			String uniqueId = json.has("uniqueId") ? json.get("uniqueId").getAsString() : "defaultUniqueId";
			
			int offsetX = json.has("offsetX") ? json.get("offsetX").getAsInt() : 0;
			int offsetY = json.has("offsetY") ? json.get("offsetY").getAsInt() : 0;
			int offsetZ = json.has("offsetZ") ? json.get("offsetZ").getAsInt() : 0;
			
			return new PartialSpecLocation(pred, who, uniqueId, offsetX, offsetY, offsetZ);
		}
	}
	
	//idk
	public enum LocationGetter {
		ATTACKER,
		ATTACKER_SPAWN_LOCATION,
		DEFENDER;
		
		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}
		
		public static LocationGetter fromString(String name) {
			return switch(name.toLowerCase(Locale.ROOT)) {
				case "attacker" -> ATTACKER;
				case "attacker_spawn_location" -> ATTACKER_SPAWN_LOCATION;
				case "defender" -> DEFENDER;
				default -> throw new IllegalArgumentException("expected 'attacker', 'defender', or 'attacker_spawn_location");
			};
		}
		
		@Deprecated(forRemoval = true)
		public static final Codec<LocationGetter> CODEC = CodecUtil.enumCodec("LocationGetter", LocationGetter.class);
	}
	
	/// CODEC HELL ///
	
	@Deprecated(forRemoval = true)
	public static final Codec<PartialSpecLocation> CODEC = RecordCodecBuilder.create(i -> i.group(
		CodecUtil.LOCATION_PREDICATE_CODEC.fieldOf("predicate").forGetter(PartialSpecLocation::pred),
		LocationGetter.CODEC.optionalFieldOf("who", LocationGetter.ATTACKER_SPAWN_LOCATION).forGetter(PartialSpecLocation::who),
		Codec.STRING.optionalFieldOf("uniqueId", "defaultUniqueId").forGetter(PartialSpecLocation::uniqueId),
		Codec.INT.optionalFieldOf("offsetX", 0).forGetter(PartialSpecLocation::offsetX),
		Codec.INT.optionalFieldOf("offsetY", 0).forGetter(PartialSpecLocation::offsetY),
		Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(PartialSpecLocation::offsetZ)
	).apply(i, PartialSpecLocation::new));
	
	@Deprecated(forRemoval = true)
	@Override
	public Codec<? extends PartialSpec<?>> codec() {
		return CODEC;
	}
}
