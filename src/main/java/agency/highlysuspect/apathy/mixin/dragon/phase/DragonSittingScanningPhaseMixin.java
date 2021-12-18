package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Init;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DragonSittingScanningPhase.class)
public class DragonSittingScanningPhaseMixin {
	@ModifyVariable(
		method = "doServerTick",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "net/minecraft/world/level/Level.getNearestPlayer(Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;DDD)Lnet/minecraft/world/entity/player/Player;",
			//first call is looking for the nearest player, to turn and face them
			//second call is the one that transitions to ChargingPlayer
			ordinal = 1
		)
	)
	private LivingEntity filterEntity(LivingEntity in) {
		EnderDragon dergon = ((AbstractDragonPhaseInstanceAccessor) this).apathy$getDragon();
		
		if(in instanceof ServerPlayer serverPlayer && !Init.mobConfig.allowedToTargetPlayer(dergon, serverPlayer)) {
			return null;
		} else return in;
	}
}
