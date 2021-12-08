package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Init;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(WitherBoss.class)
public class WitherEntityMixin {
	@Shadow @Final @Mutable private static Predicate<LivingEntity> CAN_ATTACK_PREDICATE;
	
	static {
		//Targeting these with mixin is always a huge pain...
		//Compose it with another predicate instead, how about that
		CAN_ATTACK_PREDICATE = CAN_ATTACK_PREDICATE.and((ent) -> {
			if(ent instanceof Player) return Init.bossConfig.witherTargetsPlayers;
			else return Init.bossConfig.witherTargetsMobs;
		});
	}
	
	@Inject(method = "canDestroy", at = @At("HEAD"), cancellable = true)
	private static void cantDestroy(BlockState block, CallbackInfoReturnable<Boolean> cir) {
		if(!Init.bossConfig.witherBreaksBlocks) {
			cir.setReturnValue(false);
		}
	}
	
	@Inject(method = "shootSkullAt(IDDDZ)V", at = @At("HEAD"), cancellable = true)
	private void noSkulls(int headIndex, double d, double e, double f, boolean charged, CallbackInfo ci) {
		if((!charged && !Init.bossConfig.blackWitherSkulls) || (charged && !Init.bossConfig.blueWitherSkulls)) {
			ci.cancel();
		}
	}
}
