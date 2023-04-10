package agency.highlysuspect.apathy.mixin.dragon.phase;

import agency.highlysuspect.apathy.Apathy116;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
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
	private LivingEntity apathy$filterEntity(LivingEntity in) {
		EnderDragon dergon = ((AbstractDragonPhaseInstanceAccessor) this).apathy$getDragon();
		DragonDuck dragn = (DragonDuck) dergon;
		
		if(in instanceof ServerPlayer && (!dragn.apathy$canTargetPlayers() || !Apathy116.instance116.allowedToTargetPlayer(dergon, (ServerPlayer) in))) {
			return null;
		} else return in;
	}
}
