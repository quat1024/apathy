package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Init;
import net.minecraft.world.damagesource.DamageSource;
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
	private static boolean yeet(Level world, Entity entity) {
		if(!Init.bossConfig.noWither) return world.addFreshEntity(entity);
		
		if(entity instanceof WitherBoss) ((LivingEntityInvoker) entity).apathy$dropAllDeathLoot(DamageSource.ANVIL);
		return false;
	}
}
