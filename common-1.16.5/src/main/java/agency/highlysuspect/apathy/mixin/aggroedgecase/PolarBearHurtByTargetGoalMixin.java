package agency.highlysuspect.apathy.mixin.aggroedgecase;

import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.mixin.TargetGoalAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.PolarBear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.animal.PolarBear$PolarBearHurtByTargetGoal") //Pkg private class
public class PolarBearHurtByTargetGoalMixin {
	@Inject(method = "alertOther", at = @At("HEAD"))
	private void apathy$whenAlerting(Mob other, LivingEntity target, CallbackInfo ci) {
		if(other instanceof PolarBear) {
			PolarBear otherBear = (PolarBear) other;
			if(!otherBear.isBaby() && other instanceof Attacker) {
				Attacker selfExt = (Attacker) ((TargetGoalAccessor) this).apathy$getMob(); //i love Type safety tbh
				((Attacker) other).apathy$setProvocationTime(selfExt.apathy$getProvocationTime());
			}
		}
	}
}
