package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Mixin(EnderDragon.class)
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
}
