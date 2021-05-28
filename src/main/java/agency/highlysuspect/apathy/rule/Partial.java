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

import java.util.Set;
import java.util.function.BiPredicate;

public interface Partial extends BiPredicate<MobEntity, ServerPlayerEntity> {
	static Partial inDifficultySet(Set<Difficulty> difficultySet) {
		return (attacker, defender) -> difficultySet.contains(attacker.world.getDifficulty());
	}
	
	static Partial isBoss() {
		Tag<EntityType<?>> BOSS_TAG = TagRegistry.entityType(Init.id("bosses"));
		return (attacker, defender) -> BOSS_TAG.contains(attacker.getType());
	}
	
	static Partial inMobSet(Set<EntityType<?>> mobSet) {
		return (attacker, defender) -> mobSet.contains(attacker.getType());
	}
	
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
	
	static Partial revengeTimer(long timer) {
		return (attacker, defender) -> VengeanceHandler.wasProvoked(attacker) && VengeanceHandler.timeSinceProvocation(attacker) <= timer;
	}
}
