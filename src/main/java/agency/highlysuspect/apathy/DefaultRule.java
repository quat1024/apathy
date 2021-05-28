package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.revenge.VengeanceHandler;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.Tag;

public class DefaultRule {
	public static boolean allowedToAttackPlayer(Config config, MobEntity attacker, ServerPlayerEntity player) {
		//Rule priorities and class names correspond to the original Apathetic Mobs source.
		
		//Rule priority 0: DifficultyLockRule
		if(config.difficultyLockEnabled && !config.difficultyLock.contains(attacker.world.getDifficulty())) {
			return true;
		}
		
		//Rule priority 1: BossRule
		if(config.bossBypass && isBoss(attacker)) {
			return true;
		}
		
		//Rule priority 2: TargeterTypeRule
		if(config.mobSetMode != TriState.DEFAULT && config.mobSet.contains(attacker.getType())) {
			return config.mobSetMode.get();
		}
		
		//Rule priority 3: PlayerWhitelistRule
		if(config.playerListMode != TriState.DEFAULT && config.configPlayerList != null && config.configPlayerList.contains(player)) {
			return config.playerListMode.get();
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
