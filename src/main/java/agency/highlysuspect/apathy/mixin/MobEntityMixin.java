package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.clojure.Api;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	public void whenSettingTarget(@Nullable LivingEntity newTarget, CallbackInfo ci) {
		MobEntity thi$ = (MobEntity) (Object) this;
		if(thi$.world.isClient) return;
		
		//Check whether it's okay to target this player.
		if(newTarget instanceof PlayerEntity && !Init.config.allowedToTargetPlayer(thi$, (PlayerEntity) newTarget)) {
			//Keep whatever old target was around.
			ci.cancel();
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void whenTicking(CallbackInfo ci) {
		MobEntity thi$ = (MobEntity) (Object) this;
		if(thi$.world.isClient) return;
		
		//If currently targeting a player, check to make sure it's still okay to do so.
		if((thi$.world.getTime() + thi$.getEntityId()) % Init.config.recheckInterval == 0
			&& target instanceof PlayerEntity
			&& !Init.config.allowedToTargetPlayer(thi$, (PlayerEntity) target)) {
			target = null;
		}
	}
	
	@Shadow private LivingEntity target;
}
