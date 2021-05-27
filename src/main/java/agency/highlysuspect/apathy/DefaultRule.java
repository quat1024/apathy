package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.Config;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;

public class DefaultRule {
	public static boolean allowedToAttackPlayer(Config config, MobEntity attacker, PlayerEntity player) {
		//Rule priorities and class names correspond to the original Apathetic Mobs source.
		
		//Rule priority 0: DifficultyLockRule
		if(!config.difficulties.isEmpty() && config.difficulties.contains(attacker.world.getDifficulty())) {
			return false;
		}
		
		//Rule priority 1: BossRule
		if(config.bossBypass && isBoss(attacker)) return true;
		
		//Rule priority 2: TargeterTypeRule
		if(config.mobSetMode != TriState.DEFAULT) {
			if(config.mobSet.contains(attacker.getType())) return config.mobSetMode.get();
		}
		
		//Rule priority 3: PlayerWhitelistRule
		//TODO: Not implemented.
		
		//Rule priority 4: RevengeRule
		//TODO: Not implemented.
		
		//TODO: In the original, what happens when you exhaust all the rules?
		return true;
	}
	
	private static final Tag<EntityType<?>> BOSS_TAG = TagRegistry.entityType(Init.id("bosses"));
	public static boolean isBoss(MobEntity attacker) {
		return BOSS_TAG.contains(attacker.getType());
	}
}
