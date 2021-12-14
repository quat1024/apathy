package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin {
	@Inject(method = "launchLivingEntities", at = @At("HEAD"), cancellable = true)
	private void dontLaunch(List<Entity> entities, CallbackInfo ci) {
		if(!Init.bossConfig.dragonKnockback) {
			ci.cancel();
		}
	}
	
	@Inject(method = "damageLivingEntities", at = @At("HEAD"), cancellable = true)
	private void dontDamage(List<Entity> entities, CallbackInfo ci) {
		if(!Init.bossConfig.dragonDamage) {
			ci.cancel();
		}
	}
	
	@Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void copypasteFromLivingEntityMixin(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		//EnderDragonEntity overrides canTarget and doesn't call super()
		if((LivingEntity) (Object) this instanceof MobEntity mob && target instanceof ServerPlayerEntity player && !Init.mobConfig.allowedToTargetPlayer(mob, player)) {
			cir.setReturnValue(false);
		}
	}
}
