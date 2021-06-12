package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.mixin.wither.LivingEntityInvoker;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
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
	private static boolean yeet(World world, Entity entity) {
		if(!Init.bossConfig.noWither) return world.spawnEntity(entity);
		
		if(entity instanceof WitherEntity) ((LivingEntityInvoker) entity).apathy$dropEquipment(DamageSource.ANVIL, 1, true);
		return false;
	}
}
