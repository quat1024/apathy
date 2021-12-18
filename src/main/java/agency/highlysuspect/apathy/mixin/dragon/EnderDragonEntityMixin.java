package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Mixin(EnderDragon.class)
public class EnderDragonEntityMixin {
	@ModifyVariable(method = "launchLivingEntities", at = @At("HEAD"), argsOnly = true)
	private List<Entity> filterLaunch(List<Entity> entities) {
		if(!Init.bossConfig.dragonKnockback) {
			return Collections.emptyList();
		}
		
		EnderDragon dergon = (EnderDragon) (Object) this;
		List<Entity> copy = new ArrayList<>(entities); //unneeded copies, reh reh, it's fine
		copy.removeIf(e -> e instanceof ServerPlayer player && !Init.mobConfig.allowedToTargetPlayer(dergon, player));
		return copy;
	}
	
	@ModifyVariable(method = "damageLivingEntities", at = @At("HEAD"), argsOnly = true)
	private List<Entity> filterDamage(List<Entity> entities) {
		if(!Init.bossConfig.dragonDamage) {
			return Collections.emptyList();
		}
		
		EnderDragon dergon = (EnderDragon) (Object) this;
		List<Entity> copy = new ArrayList<>(entities);
		copy.removeIf(e -> e instanceof ServerPlayer player && !Init.mobConfig.allowedToTargetPlayer(dergon, player));
		return copy;
	}
	
	@Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void copypasteFromLivingEntityMixin(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		//EnderDragonEntity overrides canTarget and doesn't call super()
		if((LivingEntity) (Object) this instanceof Mob mob && target instanceof ServerPlayer player && !Init.mobConfig.allowedToTargetPlayer(mob, player)) {
			cir.setReturnValue(false);
		}
	}
}
