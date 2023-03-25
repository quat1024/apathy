package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoldingPatternPhase.class)
public class HoldingPatternPhaseMixin {
	@Inject(method = "method_6843", at = @At("HEAD"), cancellable = true)
	private void maybeDontStrafePlayer(PlayerEntity player, CallbackInfo ci) {
		EnderDragonEntity dergon = ((AbstractPhaseAccessor) this).apathy$getDragon();
		
		if(player instanceof ServerPlayerEntity && !Init.mobConfig.allowedToTargetPlayer(dergon, (ServerPlayerEntity) player)) {
			ci.cancel();
		}
	}
}
