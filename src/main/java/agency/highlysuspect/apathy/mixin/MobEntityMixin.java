package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import agency.highlysuspect.apathy.clojure.Api;
import agency.highlysuspect.apathy.revenge.MobEntityExt;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin implements MobEntityExt {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	public void whenSettingTarget(@Nullable LivingEntity newTarget, CallbackInfo ci) {
		MobEntity thi$ = (MobEntity) (Object) this;
		if(thi$.world.isClient) return;
		
		//Check whether it's okay to target this player.
		if(newTarget instanceof ServerPlayerEntity && !Init.config.allowedToTargetPlayer(thi$, (ServerPlayerEntity) newTarget)) {
			//Keep whatever old target was around.
			ci.cancel();
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void whenTicking(CallbackInfo ci) {
		MobEntity thi$ = (MobEntity) (Object) this;
		if(thi$.world.isClient) return;
		
		//If currently targeting a player, check to make sure it's still okay to do so.
		if((thi$.world.getTime() + thi$.getEntityId()) % Init.config.recheckInterval == 0
			&& target instanceof ServerPlayerEntity
			&& !Init.config.allowedToTargetPlayer(thi$, (ServerPlayerEntity) target)) {
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
		provocationTime = ((MobEntity) (Object) this).world.getTime();
	}
	
	@Override
	public long apathy$timeSinceProvocation() {
		return ((MobEntity) (Object) this).world.getTime() - provocationTime;
	}
	
	@Override
	public boolean apathy$wasProvoked() {
		return provocationTime != NOT_PROVOKED;
	}
	
	@Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
	public void whenSaving(CompoundTag tag, CallbackInfo ci) {
		if(apathy$wasProvoked()) {
			tag.putLong(PROVOCATION_KEY, provocationTime);
		}
	}
	
	@Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
	public void whenLoading(CompoundTag tag, CallbackInfo ci) {
		if(tag.contains(PROVOCATION_KEY)) {
			provocationTime = tag.getLong(PROVOCATION_KEY);
		} else {
			provocationTime = NOT_PROVOKED;
		}
	}
}
