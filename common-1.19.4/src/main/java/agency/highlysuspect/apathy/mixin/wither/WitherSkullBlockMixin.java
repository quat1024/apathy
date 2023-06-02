package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreBossOptions;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import agency.highlysuspect.apathy.coreplusminecraft.MinecraftConv;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WitherSkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherSkullBlock.class)
public class WitherSkullBlockMixin {
	@Redirect(
		method = "checkSpawn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
		)
	)
	private static boolean apathy$yeet(Level level, Entity entity) {
		//If the difficulty is contained within the set, call the normal spawn method
		if(Apathy.instance.bossCfg.get(CoreBossOptions.witherDifficulties).contains(MinecraftConv.toApathyDifficulty(level.getDifficulty()))) return level.addFreshEntity(entity);
		
		//Else, simulate a kill and don't spawn the entity. I have no idea why this grants the advancement btw.
		if(entity instanceof WitherBoss) ((LivingEntityInvoker) entity).apathy$dropAllDeathLoot(ApathyPlusMinecraft.instanceMinecraft.comicalAnvilSound(entity));
		return false;
	}
}
