package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void patchCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if((LivingEntity) (Object) this instanceof Mob mob && target instanceof ServerPlayer player && !Apathy.mobConfig.allowedToTargetPlayer(mob, player)) {
			cir.setReturnValue(false);
		}
	}
}
