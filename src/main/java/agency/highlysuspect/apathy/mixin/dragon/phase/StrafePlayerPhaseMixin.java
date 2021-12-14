package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.StrafePlayerPhase;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StrafePlayerPhase.class)
public class StrafePlayerPhaseMixin {
	@Shadow private @Nullable LivingEntity target;
	
	@Inject(method = "serverTick", at = @At("HEAD"))
	private void onServerTick(CallbackInfo ci) {
		EnderDragonEntity dergon = ((AbstractPhaseAccessor) this).apathy$getDragon();
		
		if(target instanceof ServerPlayerEntity && !Init.mobConfig.allowedToTargetPlayer(dergon, (ServerPlayerEntity) target)) {
			//Will cause her to transition away from this phase
			target = null;
		}
	}
}
