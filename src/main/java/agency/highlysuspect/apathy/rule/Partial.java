package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.list.PlayerSet;
import agency.highlysuspect.apathy.list.PlayerSetManager;
import agency.highlysuspect.apathy.revenge.VengeanceHandler;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.world.Difficulty;

import java.util.*;
import java.util.function.BiPredicate;

public interface Partial extends BiPredicate<MobEntity, ServerPlayerEntity> {
	Partial ALWAYS_TRUE = (attacker, defender) -> true;
	Partial ALWAYS_FALSE = (attacker, defender) -> false;
	
	default Partial not() {
		if(this == ALWAYS_TRUE) return ALWAYS_FALSE;
		if(this == ALWAYS_FALSE) return ALWAYS_TRUE;
		return (attacker, defender) -> !this.test(attacker, defender);
	}
	
	default Partial and(Partial other) {
		if(this == ALWAYS_FALSE || other == ALWAYS_FALSE) return ALWAYS_FALSE;
		if(this == ALWAYS_TRUE) return other;
		if(other == ALWAYS_TRUE) return this;
		return (attacker, defender) -> this.test(attacker, defender) && other.test(attacker, defender);
	}
	
	static Partial all(Collection<Partial> partials) {
		return all(partials.toArray(new Partial[0]));
	}
	
	static Partial all(Partial... partials) {
		if(partials.length == 0) return ALWAYS_FALSE;
		if(partials.length == 1) return partials[0];
		
		List<Partial> partialList = new ArrayList<>();
		for(Partial p : partials) {
			if(p == ALWAYS_TRUE) continue;
			if(p == ALWAYS_FALSE) return ALWAYS_FALSE;
			partialList.add(p);
		}
		
		if(partialList.size() == 0) return ALWAYS_FALSE;
		if(partialList.size() == 1) return partialList.get(0);
		if(partialList.size() == 2) return partialList.get(0).and(partialList.get(1));
		
		Partial[] reducedPartials = partialList.toArray(new Partial[0]);
		return (attacker, defender) -> {
			for(Partial p : reducedPartials) {
				if(!p.test(attacker, defender)) return false;
			}
			return true;
		};
	}
	
	default Partial or(Partial other) {
		if(this == ALWAYS_TRUE || other == ALWAYS_TRUE) return ALWAYS_TRUE;
		if(this == ALWAYS_FALSE) return other;
		if(other == ALWAYS_FALSE) return this;
		return (attacker, defender) -> this.test(attacker, defender) || other.test(attacker, defender);
	}
	
	static Partial any(Collection<Partial> partials) {
		return any(partials.toArray(new Partial[0]));
	}
	
	static Partial any(Partial... partials) {
		if(partials.length == 0) return ALWAYS_FALSE;
		if(partials.length == 1) return partials[0];
		
		List<Partial> partialList = new ArrayList<>();
		for(Partial p : partials) {
			if(p == ALWAYS_TRUE) return ALWAYS_TRUE;
			if(p == ALWAYS_FALSE) continue;
			partialList.add(p);
		}
		
		if(partialList.size() == 0) return ALWAYS_FALSE;
		if(partialList.size() == 1) return partialList.get(0);
		if(partialList.size() == 2) return partialList.get(0).or(partialList.get(1));
		
		Partial[] reducedPartials = partialList.toArray(new Partial[0]);
		return (attacker, defender) -> {
			for(Partial p : reducedPartials) {
				if(p.test(attacker, defender)) return true;
			}
			return false;
		};
	}
	
	default Partial xor(Partial other) {
		if(this == ALWAYS_FALSE) return other;
		if(other == ALWAYS_FALSE) return this;
		if(this == ALWAYS_TRUE) return other.not();
		if(other == ALWAYS_TRUE) return this.not();
		return (attacker, defender) -> this.test(attacker, defender) ^ other.test(attacker, defender);
	}
	
	static Partial odd(Collection<Partial> partials) {
		if(partials.size() == 0) return ALWAYS_FALSE;
		if(partials.size() == 1) return RuleUtil.extractSingleton(partials);
		return partials.stream().reduce(Partial::xor).get();
	}
	
	static Partial odd(Partial... others) {
		//TODO: replace this with a optimized implementation
		// Although I mean, honestly?
		// I could make this method unconditionally crash, and nobody would ever report the bug...
		return odd(Arrays.asList(others));
	}
	
	// After those fifteen pages of combinators, here are the actual friccin partials
	
	static Partial difficultyIs(Difficulty difficulty) {
		return (attacker, defender) -> attacker.world.getDifficulty() == difficulty;
	}
	
	//(
	// sizeSpecialize takes a collection and switches off the size of the collection.
	// An empty collection takes the first path.
	// A singleton collection unwraps the one element and passes it to the second path.
	// Finally, a collection with more elements passes it unchanged to the third path.
	// This is all in the name of reducing the amount of work to be done at runtime.
	// An == check is (likely?) faster than a set inclusion check, even in the best case.
	//)
	static Partial difficultyIsAny(Set<Difficulty> difficultySet) {
		return RuleUtil.sizeSpecialize(difficultySet,
			() -> Partial.ALWAYS_FALSE,
			Partial::difficultyIs,
			set -> (attacker, defender) -> difficultySet.contains(attacker.world.getDifficulty())
		);
	}
	
	//Tags
	static Partial attackerTaggedWith(Tag<EntityType<?>> tag) {
		return (attacker, defender) -> tag.contains(attacker.getType());
	}
	
	static Partial attackerTaggedWithAny(Set<Tag<EntityType<?>>> tags) {
		return RuleUtil.sizeSpecialize(tags,
			() -> Partial.ALWAYS_FALSE,
			Partial::attackerTaggedWith,
			set -> (attacker, defender) -> {
				for(Tag<EntityType<?>> tag : set) {
					if(tag.contains(attacker.getType())) return true;
				}
				return false;
			}
		);
	}
	
	//Common tags usecase.
	Tag<EntityType<?>> BOSS_TAG = TagRegistry.entityType(Init.id("bosses"));
	static Partial attackerIsBoss() {
		return attackerTaggedWith(BOSS_TAG);
	}
	
	//Types
	static Partial attackerIs(EntityType<?> type) {
		return (attacker, defender) -> attacker.getType().equals(type);
	} 
	
	static Partial attackerIsAny(Set<EntityType<?>> mobSet) {
		return RuleUtil.sizeSpecialize(mobSet,
			() -> Partial.ALWAYS_FALSE,
			Partial::attackerIs,
			set -> (attacker, defender) -> set.contains(attacker.getType())
		);
	}
	
	//Player sets
	static Partial inPlayerSetNamed(String playerSetName) {
		return (attacker, defender) -> {
			MinecraftServer server = defender.getServer();
			assert server != null; //it's a ServerPlayerEntity
			
			PlayerSetManager setManager = PlayerSetManager.getFor(server);
			PlayerSet set = setManager.get(playerSetName);
			if(set == null) return false;
			else return set.contains(defender);
		};
	}
	
	static Partial inAnyPlayerSetNamed(Set<String> playerSetNames) {
		return RuleUtil.sizeSpecialize(playerSetNames,
			() -> ALWAYS_FALSE,
			Partial::inPlayerSetNamed,
			set -> (attacker, defender) -> {
				MinecraftServer server = defender.getServer();
				assert server != null;
				
				PlayerSetManager setManager = PlayerSetManager.getFor(server);
				for(String playerSetName : set) {
					PlayerSet playerSet = setManager.get(playerSetName);
					if(playerSet == null) continue;
					if(playerSet.contains(defender)) return true;
				}
				return false;
			}
		);
	}
	
	//Revenge timer
	static Partial revengeTimer(long timer) {
		return (attacker, defender) -> VengeanceHandler.wasProvoked(attacker) && VengeanceHandler.timeSinceProvocation(attacker) <= timer;
	}
}
