package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy118;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void patchCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if((LivingEntity) (Object) this instanceof Mob mob && target instanceof ServerPlayer player && !Apathy118.instance118.allowedToTargetPlayer(mob, player)) {
			cir.setReturnValue(false);
		}
	}
	
	@SuppressWarnings("ConstantConditions") //this -> mob instanceof
	@Inject(
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE",
			//Method that happens relatively late in the actuallyHurt code after invulnerability checks are applied
			target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;)V"
		)
	)
	private void whenActuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
		if(((Object) this) instanceof Mob haplessMob && source.getEntity() instanceof ServerPlayer recklessPlayer) {
			Apathy118.instance118.noticePlayerAttack(recklessPlayer, haplessMob);
		}
	}
}
