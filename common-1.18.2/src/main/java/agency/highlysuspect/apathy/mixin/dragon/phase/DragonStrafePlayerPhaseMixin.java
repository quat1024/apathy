package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Apathy118;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public class DragonStrafePlayerPhaseMixin {
	@Shadow private @Nullable LivingEntity attackTarget;
	
	@Inject(method = "doServerTick", at = @At("HEAD"))
	private void onServerTick(CallbackInfo ci) {
		EnderDragon dergon = ((AbstractDragonPhaseInstanceAccessor) this).apathy$getDragon();
		DragonDuck dragn = (DragonDuck) dergon;
		
		if(attackTarget instanceof ServerPlayer serverPlayer && (!dragn.apathy$canTargetPlayers() || !Apathy118.instance118.allowedToTargetPlayer(dergon, serverPlayer))) {
			//Will cause her to transition away from this phase
			attackTarget = null;
		}
	}
}
