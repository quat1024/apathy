package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherEntity.class)
public class WitherEntityMixin {
	@SuppressWarnings("UnresolvedMixinReference") //WitherEntity.CAN_ATTACK_PREDICATE's method body
	@Inject(method = "method_6873", at = @At("HEAD"), cancellable = true)
	private static void peacefulWither(LivingEntity ent, CallbackInfoReturnable<Boolean> cir) {
		if(ent instanceof PlayerEntity) {
			if(!Init.bossConfig.witherTargetsPlayers) {
				cir.setReturnValue(false);
			}
		} else if(!Init.bossConfig.witherTargetsMobs) cir.setReturnValue(false);
	}
	
	@Inject(method = "method_6877", at = @At("HEAD"), cancellable = true)
	private void noSkulls(int headIndex, double d, double e, double f, boolean charged, CallbackInfo ci) {
		if((!charged && !Init.bossConfig.blackWitherSkulls) || (charged && !Init.bossConfig.blueWitherSkulls)) {
			ci.cancel();
		}
	}
}
