package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Apathy120;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonHoldingPatternPhase.class)
public class DragonHoldingPatternPhaseMixin {
	@Inject(method = "strafePlayer", at = @At("HEAD"), cancellable = true)
	private void apathy$maybeDontStrafePlayer(Player player, CallbackInfo ci) {
		EnderDragon dergon = ((AbstractDragonPhaseInstanceAccessor) this).apathy$getDragon();
		DragonDuck dragn = (DragonDuck) dergon;
		
		if(player instanceof ServerPlayer serverPlayer && (!dragn.apathy$canTargetPlayers() || !Apathy120.instance120.allowedToTargetPlayer(dergon, serverPlayer))) {
			ci.cancel();
		}
	}
}
