package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin {
	@Inject(method = "launchLivingEntities", at = @At("HEAD"), cancellable = true)
	private void dontLaunch(List<Entity> entities, CallbackInfo ci) {
		if(!Apathy.bossConfig.dragonKnockback) {
			ci.cancel();
		}
	}
	
	@Inject(method = "damageLivingEntities", at = @At("HEAD"), cancellable = true)
	private void dontDamage(List<Entity> entities, CallbackInfo ci) {
		if(!Apathy.bossConfig.dragonDamage) {
			ci.cancel();
		}
	}
}
