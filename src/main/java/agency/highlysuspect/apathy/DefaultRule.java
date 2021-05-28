package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.list.PlayerSet;
import agency.highlysuspect.apathy.list.PlayerSetManager;
import agency.highlysuspect.apathy.revenge.VengeanceHandler;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.Tag;

public class DefaultRule {
	public static boolean allowedToAttackPlayer(Config config, MobEntity attacker, ServerPlayerEntity player) {
		//Rule priorities and class names correspond to the original Apathetic Mobs source.
		
		//Rule priority 0: DifficultyLockRule
		if(config.difficultySetIncluded != TriState.DEFAULT || config.difficultySetExcluded != TriState.DEFAULT) {
			if(config.difficultySet.contains(attacker.world.getDifficulty())) {
				if(config.difficultySetIncluded != TriState.DEFAULT) return config.difficultySetIncluded.get();
			} else {
				if(config.difficultySetExcluded != TriState.DEFAULT) return config.difficultySetExcluded.get();
			}
		}
		
		//Rule priority 1: BossRule
		if(config.bossBypass != TriState.DEFAULT && isBoss(attacker)) {
			return config.bossBypass.get();
		}
		
		//Rule priority 2: TargeterTypeRule
		if(config.mobSetIncluded != TriState.DEFAULT || config.mobSetExcluded != TriState.DEFAULT) {
			if(config.mobSet.contains(attacker.getType())) {
				if(config.mobSetIncluded != TriState.DEFAULT) return config.mobSetIncluded.get();
			} else {
				if(config.mobSetExcluded != TriState.DEFAULT) return config.mobSetExcluded.get();
			}
		}
		
		//Rule priority 3: PlayerWhitelistRule
		if(config.playerSetName.isPresent() && (config.playerSetIncluded != TriState.DEFAULT || config.playerSetExcluded != TriState.DEFAULT)) {
			//Locate the relevant player-set.
			String name = config.playerSetName.get();
			MinecraftServer server = player.getServer();
			assert server != null;
			
			PlayerSetManager setManager = PlayerSetManager.getFor(server);
			PlayerSet set;
			if(setManager.hasSet(name)) {
				set = setManager.get(name);
				//Make sure that (the player-set named in the config file)'s "self-select" option is always up-to-date with the config file.
				set.setSelfSelect(config.playerSetSelfSelect);
			} else {
				set = setManager.createSet(name, config.playerSetSelfSelect);
			}
			
			if(set.contains(player)) {
				if(config.playerSetIncluded != TriState.DEFAULT) return config.playerSetIncluded.get();
			} else {
				if(config.playerSetExcluded != TriState.DEFAULT) return config.playerSetExcluded.get();
			}
		}
		
		//Rule priority 4: RevengeRule
		if(config.revengeTimer > -1 && VengeanceHandler.wasProvoked(attacker) && VengeanceHandler.timeSinceProvocation(attacker) <= config.revengeTimer) {
			return true;
		}
		
		//TODO: In the original, what happens when you exhaust all the rules?
		return config.fallthrough;
	}
	
	private static final Tag<EntityType<?>> BOSS_TAG = TagRegistry.entityType(Init.id("bosses"));
	public static boolean isBoss(MobEntity attacker) {
		return BOSS_TAG.contains(attacker.getType());
	}
}
