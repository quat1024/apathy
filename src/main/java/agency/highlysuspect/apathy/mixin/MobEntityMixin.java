package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.MobEntityExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class MobEntityMixin implements MobEntityExt {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	public void whenSettingTarget(@Nullable LivingEntity newTarget, CallbackInfo ci) {
		Mob thi$ = (Mob) (Object) this;
		if(thi$.level.isClientSide) return;
		
		//Check whether it's okay to target this player.
		if(newTarget instanceof ServerPlayer && !Init.mobConfig.allowedToTargetPlayer(thi$, (ServerPlayer) newTarget)) {
			//Keep whatever old target was around.
			ci.cancel();
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void whenTicking(CallbackInfo ci) {
		Mob thi$ = (Mob) (Object) this;
		if(thi$.level.isClientSide) return;
		
		//If currently targeting a player, check to make sure it's still okay to do so.
		if((thi$.level.getGameTime() + thi$.getId()) % Init.generalConfig.recheckInterval == 0
			&& target instanceof ServerPlayer
			&& !Init.mobConfig.allowedToTargetPlayer(thi$, (ServerPlayer) target)) {
			target = null;
		}
	}
	
	@Shadow private LivingEntity target;
	
	///////////////
	
	@Unique private static final long NOT_PROVOKED = Long.MIN_VALUE;
	@Unique private static final String PROVOCATION_KEY = "apathy-provocationTime";
	@Unique long provocationTime = NOT_PROVOKED;
	
	@Override
	public void apathy$provokeNow() {
		provocationTime = ((Mob) (Object) this).level.getGameTime();
	}
	
	@Override
	public long apathy$timeSinceProvocation() {
		return ((Mob) (Object) this).level.getGameTime() - provocationTime;
	}
	
	@Override
	public boolean apathy$wasProvoked() {
		return provocationTime != NOT_PROVOKED;
	}
	
	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	public void whenSaving(CompoundTag tag, CallbackInfo ci) {
		if(apathy$wasProvoked()) {
			tag.putLong(PROVOCATION_KEY, provocationTime);
		}
	}
	
	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	public void whenLoading(CompoundTag tag, CallbackInfo ci) {
		if(tag.contains(PROVOCATION_KEY)) {
			provocationTime = tag.getLong(PROVOCATION_KEY);
		} else {
			provocationTime = NOT_PROVOKED;
		}
	}
}
