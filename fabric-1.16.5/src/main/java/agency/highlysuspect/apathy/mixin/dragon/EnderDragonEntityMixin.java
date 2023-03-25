package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin {
	@ModifyVariable(method = "launchLivingEntities", at = @At("HEAD"), argsOnly = true)
	private List<Entity> filterLaunch(List<Entity> entities) {
		if(!Init.bossConfig.dragonKnockback) {
			return Collections.emptyList();
		}
		
		EnderDragonEntity dergon = (EnderDragonEntity) (Object) this;
		List<Entity> copy = new ArrayList<>(entities); //unneeded copies, reh reh, it's fine
		copy.removeIf(e -> e instanceof ServerPlayerEntity && !Init.mobConfig.allowedToTargetPlayer(dergon, (ServerPlayerEntity) e));
		return copy;
	}
	
	@ModifyVariable(method = "damageLivingEntities", at = @At("HEAD"), argsOnly = true)
	private List<Entity> filterDamage(List<Entity> entities) {
		if(!Init.bossConfig.dragonDamage) {
			return Collections.emptyList();
		}
		
		EnderDragonEntity dergon = (EnderDragonEntity) (Object) this;
		List<Entity> copy = new ArrayList<>(entities);
		copy.removeIf(e -> e instanceof ServerPlayerEntity && !Init.mobConfig.allowedToTargetPlayer(dergon, (ServerPlayerEntity) e));
		return copy;
	}
	
//	@Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
//	private void copypasteFromLivingEntityMixin(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
//		//EnderDragonEntity overrides canTarget and doesn't call super()
	//hey, it's me, after a big cherry-pick. doesnt override it in 1.16! Lol
//		if((LivingEntity) (Object) this instanceof MobEntity mob && target instanceof ServerPlayerEntity player && !Init.mobConfig.allowedToTargetPlayer(mob, player)) {
//			cir.setReturnValue(false);
//		}
//	}
}
