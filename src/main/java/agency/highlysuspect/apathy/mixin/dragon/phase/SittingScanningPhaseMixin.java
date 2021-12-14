package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.SittingScanningPhase;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SittingScanningPhase.class)
public class SittingScanningPhaseMixin {
	@ModifyVariable(
		method = "serverTick",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;DDD)Lnet/minecraft/entity/player/PlayerEntity;",
			//first call is looking for the nearest player to turn and face them
			//second call is the one that transitions to ChargingPlayer
			ordinal = 1
		)
	)
	private LivingEntity filterEntity(LivingEntity in) {
		EnderDragonEntity dergon = ((AbstractPhaseAccessor) this).apathy$getDragon();
		
		if(in instanceof ServerPlayerEntity serverPlayer && !Init.mobConfig.allowedToTargetPlayer(dergon, serverPlayer)) {
			return null;
		} else return in;
	}
}
