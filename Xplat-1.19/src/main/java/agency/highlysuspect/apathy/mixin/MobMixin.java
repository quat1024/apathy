package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.hell.TriState;
import agency.highlysuspect.apathy.CoolNbtUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
@Mixin(Mob.class)
public class MobMixin implements MobExt {
	@Shadow private LivingEntity target;
	
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	public void apathy$onSetTarget(@Nullable LivingEntity newTarget, CallbackInfo ci) {
		Mob thi$ = (Mob) (Object) this;
		if(thi$.level.isClientSide) return;
		
		//Check whether it's okay to target this player.
		if(newTarget instanceof ServerPlayer && !Apathy119.instance119.mobConfig.allowedToTargetPlayer(thi$, (ServerPlayer) newTarget)) {
			//Keep whatever old target was around.
			
			//Btw this is the reason i don't use the forge attack target event and use this mixin even on Forge too.
			//It's fired after the attack target was already overwritten, so the best I can do is set the target to `null`.
			//Apathetic Mobs did this - but I'd rather leave the target unchanged, rather than clear it unconditionally.
			//Consider something like, two skeletons fighting each other. I don't want them to stop fighting
			//just because one of them *thought* about setting their target to a player.
			ci.cancel();
		}
	}
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void apathy$onTick(CallbackInfo ci) {
		Mob thi$ = (Mob) (Object) this;
		if(thi$.level.isClientSide) return;
		
		//Record the first known position of this entity
		if(spawnPosition == null) spawnPosition = thi$.position();
		
		//If currently targeting a player, check to make sure it's still okay to do so.
		if((thi$.level.getGameTime() + thi$.getId()) % Apathy119.instance119.generalConfig.recheckInterval == 0
			&& target instanceof ServerPlayer
			&& !Apathy119.instance119.mobConfig.allowedToTargetPlayer(thi$, (ServerPlayer) target)) {
			target = null;
		}
	}
	
	///////////////
	
	@Unique private static final String PROVOCATION_KEY = "apathy-provocationTime";
	@Unique private static final String SPAWN_POSITION_KEY = "apathy-spawnPosition";
	@Unique private static final String LOCATION_PREDICATE_CACHE_KEY = "apathy-locationPredicateCache";
	
	@Unique long provocationTime = MobExt.NOT_PROVOKED;
	@Unique @Nullable Vec3 spawnPosition;
	@Unique @Nullable Map<String, TriState> locationPredicateCache;
	
	@Override
	public void apathy$setProvocationTime(long time) {
		provocationTime = time;
	}
	
	@Override
	public long apathy$getProvocationTime() {
		return provocationTime;
	}
	
	@Override
	public @Nullable Vec3 apathy$getSpawnPosition() {
		return spawnPosition;
	}
	
	@Override
	public @Nullable Map<String, TriState> apathy$getOrCreateLocationPredicateCache() {
		if(locationPredicateCache == null) locationPredicateCache = new HashMap<>();
		return locationPredicateCache;
	}
	
	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	public void apathy$whenSaving(CompoundTag tag, CallbackInfo ci) {
		if(apathy$wasProvoked()) {
			tag.putLong(PROVOCATION_KEY, provocationTime);
		}
		
		if(spawnPosition != null) {
			tag.put(SPAWN_POSITION_KEY, CoolNbtUtil.writeVec3(spawnPosition));
		}
		
		if(locationPredicateCache != null) {
			CompoundTag real = new CompoundTag();
			locationPredicateCache.forEach((k, v) -> real.putString(k, v.toString()));
			tag.put(LOCATION_PREDICATE_CACHE_KEY, real);
		}
	}
	
	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	public void apathy$whenLoading(CompoundTag tag, CallbackInfo ci) {
		if(tag.contains(PROVOCATION_KEY)) {
			provocationTime = tag.getLong(PROVOCATION_KEY);
		} else {
			provocationTime = NOT_PROVOKED;
		}
		
		if(tag.contains(SPAWN_POSITION_KEY)) {
			spawnPosition = CoolNbtUtil.readVec3(tag.getList(SPAWN_POSITION_KEY, CoolNbtUtil.VEC3_LIST_ID));
		} else {
			spawnPosition = null;
		}
		
		if(tag.contains(LOCATION_PREDICATE_CACHE_KEY)) {
			locationPredicateCache = new HashMap<>();
			
			CompoundTag real = tag.getCompound(LOCATION_PREDICATE_CACHE_KEY);
			for(String k : real.getAllKeys()) {
				locationPredicateCache.put(k, TriState.fromString(real.getString(k)));
			}
		} else {
			locationPredicateCache = null;
		}
	}
}
