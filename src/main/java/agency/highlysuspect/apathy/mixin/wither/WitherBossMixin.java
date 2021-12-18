package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.mixin.EntityGetterMixin;
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

/**
 * @see EntityGetterMixin for an additional mixin relevant to Wither targeting mechanics
 */
@Mixin(WitherBoss.class)
public class WitherBossMixin {
	@Shadow @Final @Mutable private static Predicate<LivingEntity> LIVING_ENTITY_SELECTOR;
	
	static {
		//Targeting these with mixin is always a huge pain...
		//Compose it with another predicate instead, how about that
		LIVING_ENTITY_SELECTOR = LIVING_ENTITY_SELECTOR.and((ent) -> {
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
	
	@Inject(method = "performRangedAttack(IDDDZ)V", at = @At("HEAD"), cancellable = true)
	private void noSkulls(int headIndex, double d, double e, double f, boolean charged, CallbackInfo ci) {
		if((!charged && !Init.bossConfig.blackWitherSkulls) || (charged && !Init.bossConfig.blueWitherSkulls)) {
			ci.cancel();
		}
	}
}
