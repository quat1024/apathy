package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy119;
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
	@SuppressWarnings("ConstantConditions") //Cast funny
	@Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void apathy$onCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if((LivingEntity) (Object) this instanceof Mob mob && target instanceof ServerPlayer player && !Apathy119.INSTANCE.allowedToTargetPlayer(mob, player)) {
			cir.setReturnValue(false);
		}
	}
	
	@SuppressWarnings("ConstantConditions") //this -> mob instanceof
	@Inject(
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE",
			//Method that happens relatively late in the actuallyHurt code after invulnerability checks are applied
			target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V"
		)
	)
	private void apathy$onActuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
		if(((Object) this) instanceof Mob haplessMob && source.getEntity() instanceof ServerPlayer recklessPlayer) {
			Apathy119.INSTANCE.noticePlayerAttack(recklessPlayer, haplessMob);
		}
	}
}
