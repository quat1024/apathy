package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Apathy118;
import agency.highlysuspect.apathy.CoreConv;
import agency.highlysuspect.apathy.Portage;
import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.CoreOptions;
import net.minecraft.world.level.Level;
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
public class WitherBossMixin {
	@Shadow @Final @Mutable private static Predicate<LivingEntity> LIVING_ENTITY_SELECTOR;
	
	static {
		//Targeting these with mixin is always a huge pain...
		//Compose it with another predicate instead, how about that
		LIVING_ENTITY_SELECTOR = LIVING_ENTITY_SELECTOR.and((ent) -> {
			if(ent instanceof Player) return ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.witherTargetsPlayers);
			else return ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.witherTargetsMobs);
		});
	}
	
	@Inject(method = "canDestroy", at = @At("HEAD"), cancellable = true)
	private static void cantDestroy(BlockState block, CallbackInfoReturnable<Boolean> cir) {
		if(!ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.witherBreaksBlocks)) {
			cir.setReturnValue(false);
		}
	}
	
	@Inject(method = "performRangedAttack(IDDDZ)V", at = @At("HEAD"), cancellable = true)
	private void noSkulls(int headIndex, double d, double e, double f, boolean charged, CallbackInfo ci) {
		if((!charged && !ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.blackWitherSkulls)) || (charged && !ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.blueWitherSkulls))) {
			ci.cancel();
		}
	}
	
	//Erase any stray Withers if they are turned off in this difficulty 
	@Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
	private void maybeDelete(CallbackInfo ci) {
		WitherBoss me = (WitherBoss) (Object) this;
		Level level = me.level;
		if(!ApathyHell.instance.bossConfigCooked.get(CoreOptions.Boss.witherDifficulties).contains(CoreConv.toApathyDifficulty(level.getDifficulty()))) {
			((LivingEntityInvoker) me).apathy$dropAllDeathLoot(Portage.comicalAnvilSound(me));
			me.discard();
			ci.cancel();
		}
	}
}
