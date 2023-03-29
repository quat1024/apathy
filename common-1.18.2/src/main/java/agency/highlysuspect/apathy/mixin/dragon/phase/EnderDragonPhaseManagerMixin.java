package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.CoreOptions;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderDragonPhaseManager.class)
public class EnderDragonPhaseManagerMixin {
	@ModifyVariable(method = "setPhase", at = @At("HEAD"))
	private EnderDragonPhase<?> whenSettingPhase(EnderDragonPhase<?> type) {
		boolean noFlies = !ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.dragonFlies);
		boolean noSits = !ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.dragonSits);
		
		if(noFlies && (type == EnderDragonPhase.STRAFE_PLAYER || type == EnderDragonPhase.CHARGING_PLAYER)) return EnderDragonPhase.LANDING_APPROACH;
		else if(noSits && (type == EnderDragonPhase.SITTING_FLAMING || type == EnderDragonPhase.SITTING_ATTACKING)) return EnderDragonPhase.SITTING_SCANNING;
		else return type;
	}
}
