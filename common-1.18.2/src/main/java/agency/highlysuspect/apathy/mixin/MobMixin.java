package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy118;
import agency.highlysuspect.apathy.VerConv;
import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreGenOptions;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import agency.highlysuspect.apathy.core.wrapper.VecThree;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
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

@Mixin(Mob.class)
public class MobMixin implements Attacker {
	/// basic compatibility stuff ///
	@Override
	public Object apathy$underlyingObject() {
		return this;
	}
	
	@Override
	public ApathyDifficulty apathy$getDifficulty() {
		return VerConv.toApathyDifficulty(((Mob) (Object) this).level.getDifficulty());
	}
	
	@Override
	public AttackerType apathy$getType() {
		return (AttackerType) ((Mob) (Object) this).getType();
	}
	
	/// attacking and defending logic ///
	
	@Shadow private LivingEntity target;
	
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	public void apathy$whenSettingTarget(@Nullable LivingEntity newTarget, CallbackInfo ci) {
		Mob thi$ = (Mob) (Object) this;
		if(thi$.level.isClientSide) return;
		
		//Check whether it's okay to target this player.
		if(newTarget instanceof ServerPlayer && !Apathy118.instance118.allowedToTargetPlayer(thi$, (ServerPlayer) newTarget)) {
			//Keep whatever old target was around.
			
			//Btw this is the reason i don't use the forge attack target event and use this mixin even on Forge too.
			//It's fired after the attack target was already overwritten, so the best I can do is set the target to `null`.
			//Apathetic Mobs did this - but I'd rather leave the target unchanged, rather than clear it unconditionally.
			//Consider something like, two skeletons fighting each other. I don't want them to stop fighting
			//just because one of them *thought* about setting their target to a player.
			ci.cancel();
		}
	}
	
	@Unique private int recheckIntervalCache = Integer.MIN_VALUE;
	
	@Inject(method = "tick", at = @At("HEAD"))
	public void apathy$whenTicking(CallbackInfo ci) {
		Mob thi$ = (Mob) (Object) this;
		if(thi$.level.isClientSide) return;
		
		//Record the first known position of this entity
		if(spawnPosition == null) spawnPosition = thi$.position();
		
		//If currently targeting a player, check to make sure it's still okay to do so.
		//Avoid hitting the config every single tick for this thing that's supposed to be an optimization.
		long gametime = thi$.level.getGameTime();
		if(recheckIntervalCache == Integer.MIN_VALUE || gametime % 400 == 0) {
			recheckIntervalCache = Apathy.instance.generalCfg.get(CoreGenOptions.recheckInterval);
		}
		if((gametime + thi$.getId()) % recheckIntervalCache == 0 && target instanceof ServerPlayer && !Apathy118.instance118.allowedToTargetPlayer(thi$, (ServerPlayer) target)) {
			target = null;
		}
	}
	
	/// provocation time ///
	
	@Unique private static final String PROVOCATION_KEY = "apathy-provocationTime";
	@Unique long provocationTime = Attacker.NOT_PROVOKED;
	
	@Override
	public void apathy$setProvocationTime(long time) {
		provocationTime = time;
	}
	
	@Override
	public long apathy$getProvocationTime() {
		return provocationTime;
	}
	
	@Override
	public long apathy$now() {
		return ((Mob) (Object) this).level.getGameTime();
	}
	
	/// spawn position stuff ///
	
	@Unique private static final String SPAWN_POSITION_KEY = "apathy-spawnPosition";
	@Unique private static final String LOCATION_PREDICATE_CACHE_KEY = "apathy-locationPredicateCache";
	
	@Unique @Nullable Vec3 spawnPosition;
	@Unique @Nullable Map<String, TriState> locationPredicateCache;
	
	@Override
	public @Nullable VecThree apathy$getSpawnPosition() {
		return VerConv.toVecThree(spawnPosition);
	}
	
	@Override
	public @Nullable Map<String, TriState> apathy$getOrCreateLocationPredicateCache() {
		if(locationPredicateCache == null) locationPredicateCache = new HashMap<>();
		return locationPredicateCache;
	}
	
	/// persistence of the above ///
	
	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	public void apathy$whenSaving(CompoundTag tag, CallbackInfo ci) {
		if(apathy$getProvocationTime() != Attacker.NOT_PROVOKED) {
			tag.putLong(PROVOCATION_KEY, provocationTime);
		}
		
		if(spawnPosition != null) {
			ListTag asdf = new ListTag();
			asdf.add(DoubleTag.valueOf(spawnPosition.x));
			asdf.add(DoubleTag.valueOf(spawnPosition.y));
			asdf.add(DoubleTag.valueOf(spawnPosition.z));
			tag.put(SPAWN_POSITION_KEY, asdf);
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
		} else provocationTime = NOT_PROVOKED;
		
		if(tag.contains(SPAWN_POSITION_KEY)) {
			ListTag asdf = tag.getList(SPAWN_POSITION_KEY, DoubleTag.valueOf(69420).getId());
			spawnPosition = new Vec3(asdf.getDouble(0), asdf.getDouble(1), asdf.getDouble(2));
		} else spawnPosition = null;
		
		if(tag.contains(LOCATION_PREDICATE_CACHE_KEY)) {
			locationPredicateCache = new HashMap<>();
			
			CompoundTag real = tag.getCompound(LOCATION_PREDICATE_CACHE_KEY);
			for(String k : real.getAllKeys()) {
				locationPredicateCache.put(k, TriState.fromString(real.getString(k)));
			}
		} else locationPredicateCache = null;
	}
	
	/// PartialSpecRandom ///
	
	@Override
	public int apathy$uuidBits() {
		return ((Mob) (Object) this).getUUID().hashCode();
	}
}
