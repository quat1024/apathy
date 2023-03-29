package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.DragonDuck;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
public class EnderDragonMixin implements DragonDuck {
	@ModifyVariable(method = "knockBack", at = @At("HEAD"), argsOnly = true)
	private List<Entity> filterKnockBack(List<Entity> entities) {
		if(!Apathy.INSTANCE.bossConfig.dragonKnockback) {
			return Collections.emptyList();
		}
		
		EnderDragon dergon = (EnderDragon) (Object) this;
		List<Entity> copy = new ArrayList<>(entities); //unneeded copies, reh reh, it's fine
		copy.removeIf(e -> e instanceof ServerPlayer player && !Apathy.INSTANCE.allowedToTargetPlayer(dergon, player));
		return copy;
	}
	
	@ModifyVariable(method = "hurt(Ljava/util/List;)V", at = @At("HEAD"), argsOnly = true)
	private List<Entity> filterHurt(List<Entity> entities) {
		if(!Apathy.INSTANCE.bossConfig.dragonDamage) {
			return Collections.emptyList();
		}
		
		EnderDragon dergon = (EnderDragon) (Object) this;
		List<Entity> copy = new ArrayList<>(entities);
		copy.removeIf(e -> e instanceof ServerPlayer player && (!allowedToTargetPlayers || !Apathy.INSTANCE.allowedToTargetPlayer(dergon, player)));
		return copy;
	}
	
	@Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
	private void copypasteFromLivingEntityMixin(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		//EnderDragonEntity overrides canTarget and doesn't call super()
		if((LivingEntity) (Object) this instanceof Mob mob && target instanceof ServerPlayer player && (!allowedToTargetPlayers || !Apathy.INSTANCE.allowedToTargetPlayer(mob, player))) {
			cir.setReturnValue(false);
		}
	}
	
	//Special handling for BossConfig.dragonInitialState.PASSIVE_DRAGON
	@Unique private boolean allowedToTargetPlayers = true;
	@Unique private static final String KEY = "apathy-allowed-to-target-players";
	
	@Override
	public void apathy$allowAttackingPlayers() {
		allowedToTargetPlayers = true;
	}
	
	@Override
	public void apathy$disallowAttackingPlayers() {
		allowedToTargetPlayers = false;
	}
	
	@Override
	public boolean apathy$canTargetPlayers() {
		return allowedToTargetPlayers;
	}
	
	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	private void saveMoreData(CompoundTag tag, CallbackInfo ci) {
		tag.putBoolean(KEY, allowedToTargetPlayers);
	}
	
	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	private void loadMoreData(CompoundTag tag, CallbackInfo ci) {
		if(tag.contains(KEY)) allowedToTargetPlayers = tag.getBoolean(KEY);
		else allowedToTargetPlayers = true; //Default to true as preexisting dagns won't have this tag
	}
}
