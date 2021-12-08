package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderDragonPhaseManager.class)
public class EnderDragonPhaseManagerMixin {
	@ModifyVariable(method = "setPhase", at = @At("HEAD"))
	private EnderDragonPhase<?> whenSettingPhase(EnderDragonPhase<?> type) {
		if(!Init.bossConfig.dragonFlies && (type == EnderDragonPhase.STRAFE_PLAYER || type == EnderDragonPhase.CHARGING_PLAYER)) return EnderDragonPhase.LANDING_APPROACH;
		else if(!Init.bossConfig.dragonSits && (type == EnderDragonPhase.SITTING_FLAMING || type == EnderDragonPhase.SITTING_ATTACKING)) return EnderDragonPhase.SITTING_SCANNING;
		else return type;
	}
}
