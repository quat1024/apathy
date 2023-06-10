package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreBossOptions;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderDragonPhaseManager.class)
public class EnderDragonPhaseManagerMixin {
	@ModifyVariable(method = "setPhase", at = @At("HEAD"), argsOnly = true)
	private EnderDragonPhase<?> apathy$whenSettingPhase(EnderDragonPhase<?> type) {
		boolean noFlies = !Apathy.instance.bossCfg.get(CoreBossOptions.dragonFlies);
		boolean noSits = !Apathy.instance.bossCfg.get(CoreBossOptions.dragonSits);
		
		if(noFlies && (type == EnderDragonPhase.STRAFE_PLAYER || type == EnderDragonPhase.CHARGING_PLAYER)) return EnderDragonPhase.LANDING_APPROACH;
		else if(noSits && (type == EnderDragonPhase.SITTING_FLAMING || type == EnderDragonPhase.SITTING_ATTACKING)) return EnderDragonPhase.SITTING_SCANNING;
		else return type;
	}
}
