package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.block.BlockState;
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
			if(!Apathy.bossConfig.witherTargetsPlayers) {
				cir.setReturnValue(false);
			}
		} else if(!Apathy.bossConfig.witherTargetsMobs) cir.setReturnValue(false);
	}
	
	@Inject(method = "canDestroy", at = @At("HEAD"), cancellable = true)
	private static void cantDestroy(BlockState block, CallbackInfoReturnable<Boolean> cir) {
		if(!Apathy.bossConfig.witherBreaksBlocks) {
			cir.setReturnValue(false);
		}
	}
	
	@Inject(method = "shootSkullAt(IDDDZ)V", at = @At("HEAD"), cancellable = true)
	private void noSkulls(int headIndex, double d, double e, double f, boolean charged, CallbackInfo ci) {
		if((!charged && !Apathy.bossConfig.blackWitherSkulls) || (charged && !Apathy.bossConfig.blueWitherSkulls)) {
			ci.cancel();
		}
	}
}
