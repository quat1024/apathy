package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Portage;
import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.wrapper.VecThree;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class PartialSpecLocation implements Spec<Partial, PartialSpecLocation> {
	public PartialSpecLocation(LocationPredicate pred, LocationGetter who, String uniqueId, int offsetX, int offsetY, int offsetZ) {
		this.pred = pred;
		this.who = who;
		this.uniqueId = uniqueId;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}
	
	private final LocationPredicate pred;
	private final LocationGetter who;
	private final String uniqueId;
	private final int offsetX, offsetY, offsetZ;
	
	@Override
	public Spec<Partial, ?> optimize() {
		if(pred == LocationPredicate.ANY) return PartialSpecAlways.TRUE;
		else return this;
	}
	
	@Override
	public Partial build() {
		return (attacker, defender) -> {
			ServerLevel slevel = VerConv.level(defender);
			
			//too hard to port im lazy
			Vec3 attackerPos = VerConv.mob(attacker).position();
			Vec3 defenderPos = VerConv.player(defender).position();
			
			switch(who) {
				//Easy cases (that can't be cached anyways because the entities wander around the world)
				case ATTACKER:
					return test(slevel, pred, attackerPos.x, attackerPos.y, attackerPos.z);
				case DEFENDER:
					return test(slevel, pred, defenderPos.x, defenderPos.y, defenderPos.z);
				//OK this one is fun!!
				case ATTACKER_SPAWN_LOCATION:
					//The spawn position is fixed, so there is no need to check the LocationPredicate every single tick.
					//But more importantly, the entity might wander so far away from its spawn position that its not loaded anymore.
					//LocationPredicates return incorrect results for unloaded positions. I want to avoid the behavior of an entity
					//changing just because it walked a long distance. So, here we are
					
					//Look up the cached result. If one exists, yield it
					Map<String, TriState> cache = attacker.apathy$getOrCreateLocationPredicateCache();
					TriState cachedResult = cache.getOrDefault(uniqueId, TriState.DEFAULT);
					if(cachedResult == TriState.TRUE) return true;
					if(cachedResult == TriState.FALSE) return false;
					
					//Begin computing the uncached result.
					//Look up the spawn position of this entity
					VecThree vecThree = attacker.apathy$getSpawnPosition();
					if(vecThree == null) {
						//The spawn position is unknown for this entity
						return false;
					}
					
					//Compute and store the cached result, if the position is loaded
					if(slevel.isLoaded(Portage.blockPosContaining(vecThree.x(), vecThree.y(), vecThree.z()))) {
						boolean result = test(slevel, pred, vecThree.x(), vecThree.y(), vecThree.z());
						cache.put(uniqueId, TriState.fromBoolean(result));
						return result;
					}
					
					//If we're here, the spawn position is known, but it hasn't ever been loaded at the same time as this entity... somehow
					//Might be an entity that existed in a world created before the update that added this caching system.
					//We can't know for sure whether the location predicate passes or not so default to false
					return false;
			}
			return false; //unreachable
		};
	}
	
	private boolean test(ServerLevel slevel, LocationPredicate pred, double x, double y, double z) {
		return pred.matches(slevel, x + offsetX, y + offsetY, z + offsetZ);
	}
	
	@Override
	public JsonSerializer<PartialSpecLocation> getSerializer() {
		return Serializer.INSTANCE;
	}
	
	public static class Serializer implements JsonSerializer<PartialSpecLocation> {
		private Serializer() {}
		public static final Serializer INSTANCE = new Serializer();
		
		@Override
		public void write(PartialSpecLocation thing, JsonObject json) {
			json.add("predicate", thing.pred.serializeToJson());
			
			json.addProperty("who", thing.who.toString()); //optional but unconditionally serialized
			json.addProperty("uniqueId", thing.uniqueId); //optional but unconditionally serialized
			
			if(thing.offsetX != 0) json.addProperty("offsetX", thing.offsetX);
			if(thing.offsetY != 0) json.addProperty("offsetY", thing.offsetY);
			if(thing.offsetZ != 0) json.addProperty("offsetZ", thing.offsetZ);
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
			switch(name.toLowerCase(Locale.ROOT)) {
				case "attacker": return ATTACKER;
				case "attacker_spawn_location": return ATTACKER_SPAWN_LOCATION;
				case "defender": return DEFENDER;
				default: throw new IllegalArgumentException("expected 'attacker', 'defender', or 'attacker_spawn_location");
			}
		}
	}
}
