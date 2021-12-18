package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Init;
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
public class StrafePlayerPhaseMixin {
	@Shadow private @Nullable LivingEntity target;
	
	@Inject(method = "serverTick", at = @At("HEAD"))
	private void onServerTick(CallbackInfo ci) {
		EnderDragon dergon = ((AbstractPhaseAccessor) this).apathy$getDragon();
		
		if(target instanceof ServerPlayer serverPlayer && !Init.mobConfig.allowedToTargetPlayer(dergon, serverPlayer)) {
			//Will cause her to transition away from this phase
			target = null;
		}
	}
}
