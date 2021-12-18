package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Init;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonHoldingPatternPhase.class)
public class HoldingPatternPhaseMixin {
	@Inject(method = "strafePlayer", at = @At("HEAD"), cancellable = true)
	private void maybeDontStrafePlayer(Player player, CallbackInfo ci) {
		EnderDragon dergon = ((AbstractPhaseAccessor) this).apathy$getDragon();
		
		if(player instanceof ServerPlayer serverPlayer && !Init.mobConfig.allowedToTargetPlayer(dergon, serverPlayer)) {
			ci.cancel();
		}
	}
}
