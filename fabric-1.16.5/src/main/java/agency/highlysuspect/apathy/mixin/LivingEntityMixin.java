package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void patchCanTarget(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity THIS = (LivingEntity) (Object) this;
		if(THIS instanceof MobEntity && target instanceof ServerPlayerEntity && !Init.mobConfig.allowedToTargetPlayer((MobEntity) THIS, (ServerPlayerEntity) target)) {
			cir.setReturnValue(false);
		}
	}
}
