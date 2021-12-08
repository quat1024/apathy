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
		method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/SkullBlockEntity;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
		)
	)
	private static boolean yeet(Level world, Entity entity) {
		if(!Init.bossConfig.noWither) return world.addFreshEntity(entity);
		
		if(entity instanceof WitherBoss) ((LivingEntityInvoker) entity).apathy$dropEquipment(DamageSource.ANVIL, 1, true);
		return false;
	}
}
