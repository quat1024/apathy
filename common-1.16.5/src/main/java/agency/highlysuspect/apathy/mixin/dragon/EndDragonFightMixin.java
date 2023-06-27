package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreBossOptions;
import agency.highlysuspect.apathy.core.etc.PortalInitialState;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("SameParameterValue")
@Mixin(EndDragonFight.class)
public abstract class EndDragonFightMixin {
	//a zillion shadows
	@Shadow @Final private static Predicate<Entity> VALID_PLAYER;
	@Shadow @Final private ServerLevel level;
	@Shadow @Final private List<Integer> gateways;
	@Shadow private boolean dragonKilled;
	@Shadow private boolean previouslyKilled;
	@Shadow private BlockPos portalLocation;
	@Shadow private List<EndCrystal> respawnCrystals;
	
	@Shadow protected abstract boolean isArenaLoaded();
	@Shadow protected abstract void spawnNewGateway();
	@Shadow protected abstract void spawnExitPortal(boolean previouslyKilled);
	
	//my additions
	@Unique private int gatewayTimer = NOT_RUNNING;
	@Unique private static final int NOT_RUNNING = -100;
	
	@Unique private static final String APATHY_GATEWAYTIMER = "apathy-gateway-timer";
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void apathy$onInit(ServerLevel world, long l, CompoundTag tag, CallbackInfo ci) {
		if(tag.contains(APATHY_GATEWAYTIMER)) {
			gatewayTimer = tag.getInt(APATHY_GATEWAYTIMER);
		} else {
			gatewayTimer = NOT_RUNNING;
		}
		
		//COPY PASTE from vanilla.
		//In vanilla, this code only runs if dragonKilled is true.
		//In Apathy, it's possible for the location of the exit portal to be decided before killing the first dragon.
		//In this situation we should still honor the ExitPortalLocation from disk. (Vanilla unconditionally saves it, no edits are needed there.)
		if(tag.contains("ExitPortalLocation", 10)) {
			this.portalLocation = NbtUtils.readBlockPos(tag.getCompound("ExitPortalLocation"));
		}
	}
	
	@Inject(method = "saveData", at = @At(value = "RETURN"))
	void apathy$whenTagging(CallbackInfoReturnable<CompoundTag> cir) {
		cir.getReturnValue().putInt(APATHY_GATEWAYTIMER, gatewayTimer);
	}
	
	//runs BEFORE vanilla tick().
	@Inject(method = "tick", at = @At("HEAD"))
	void apathy$tick(CallbackInfo ci) {
		if(!isArenaLoaded()) return;
		
		//Tick the ResummonSequence.SPAWN_GATEWAY mechanic.
		if(gatewayTimer != NOT_RUNNING && portalLocation != null) {
			if(gatewayTimer > 0) {
				gatewayTimer--;
			} else {
				doGatewaySpawn();
				gatewayTimer = NOT_RUNNING;
			}
		}
		
		//Handle simulacra advancements.
		boolean simulacra = Apathy.instance.bossCfg.get(CoreBossOptions.simulacraDragonAdvancements);
		boolean startCalm = Apathy.instance.bossCfg.get(CoreBossOptions.dragonInitialState).isCalm();
		if(simulacra && startCalm) {
			//this grants the "Free the End" advancement, in a kind of clunky way
			EnderDragon rarrrh = EntityType.ENDER_DRAGON.create(level);
			for(ServerPlayer player : level.getPlayers(VALID_PLAYER)) {
				CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, rarrrh, ApathyPlusMinecraft.instanceMinecraft.comicalAnvilSound(rarrrh));
			}
		}
	}
	
	@Inject(method = "scanState", at = @At("RETURN"))
	void apathy$finishScanningState(CallbackInfo ci) {
		//scanState is called ONCE, EVER, the very first time any player loads the End.
		//It is never called again (the `needsStateScanning` variable makes sure of that).
		//So this is a good time to do "first-run" tasks.
		
		//Player requested the initial dragon to be removed. At this point the dragon has not
		//been spawned yet. It will be spawned later in the callee of scanState (tick) only if dragonKilled is unset.
		if(Apathy.instance.bossCfg.get(CoreBossOptions.dragonInitialState).isCalm()) {
			dragonKilled = true;
			previouslyKilled = true;
		}
		
		PortalInitialState portalInitialState = Apathy.instance.bossCfg.get(CoreBossOptions.portalInitialState);
		
		//If the player requested the exit End portal to generate open, do that.
		if(portalInitialState.isOpenByDefault()) {
			//vanilla method, boolean prop is "whether it's open or not".
			//Since the portal location is known, this should cleanly overwrite the vanilla portal.
			spawnExitPortal(true);
			
			//styled after setDragonKilled
			if(portalInitialState.hasEgg()) {
				this.level.setBlockAndUpdate(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.defaultBlockState());
			}
		}
		
		//Generate any End Portals players requested to be opened by default.
		int initialEndGatewayCount = Apathy.instance.bossCfg.get(CoreBossOptions.initialEndGatewayCount);
		for(int i = 0; i < initialEndGatewayCount; i++) {
			spawnNewGateway();
		}
	}
	
	@Inject(method = "createNewDragon", at = @At("RETURN"))
	void apathy$whenCreatingDragon(CallbackInfoReturnable<EnderDragon> cir) {
		if(!previouslyKilled && Apathy.instance.bossCfg.get(CoreBossOptions.dragonInitialState).isPassive()) {
			((DragonDuck) cir.getReturnValue()).apathy$disallowAttackingPlayers();
		}
	}
	
	//tryRespawn handles detecting the 4 end crystals by the exit portal.
	//respawnDragon gets called with the list of end crystals if there are four, and actually summons the boss.
	@Inject(method = "respawnDragon(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
	void apathy$whenBeginningRespawnSequence(List<EndCrystal> crystals, CallbackInfo ci) {
		switch(Apathy.instance.bossCfg.get(CoreBossOptions.resummonSequence)) {
			case DEFAULT: break; //Nothing to do.
			case DISABLED: ci.cancel(); break;
			case SPAWN_GATEWAY: {
				ci.cancel();
				tryEnderCrystalGateway(crystals);
			}
		}
	}
	
	@Unique private void tryEnderCrystalGateway(List<EndCrystal> crystalsAroundEndPortal) {
		if(gatewayTimer == NOT_RUNNING) {
			BlockPos pos = gatewayDryRun();
			if(pos != null) {
				BlockPos downABit = pos.below(2); //where the actual gateway block will be
				for(EndCrystal crystal : crystalsAroundEndPortal) {
					crystal.setBeamTarget(downABit);
				}
				
				this.respawnCrystals = crystalsAroundEndPortal;
				gatewayTimer = 100; //5 seconds
			}
		}
	}
	
	//The end of the "spawn gateway" cutscene
	@Unique private void doGatewaySpawn() {
		spawnNewGateway(); //Actually generate it now
		
		//Blow up the crystals located on the end portal.
		//(Yes, this means you can smuggle them away with a piston, just like vanilla lol.)
		BlockPos exitPos = portalLocation;
		BlockPos oneAboveThat = exitPos.above();
		for(Direction d : Direction.Plane.HORIZONTAL) {
			for(EndCrystal crystal : this.level.getEntitiesOfClass(EndCrystal.class, new AABB(oneAboveThat.relative(d, 2)))) {
				crystal.setBeamTarget(null);
				ApathyPlusMinecraft.instanceMinecraft.explodeNoBlockInteraction(level, crystal, crystal.getX(), crystal.getY(), crystal.getZ(), 6f);
				crystal.remove();
			}
		}
		
		//Grant the advancement for resummoning the Ender Dragon (close enough)
		if(Apathy.instance.bossCfg.get(CoreBossOptions.simulacraDragonAdvancements)) {
			EnderDragon dummy = EntityType.ENDER_DRAGON.create(level);
			for(ServerPlayer player : level.getPlayers(VALID_PLAYER)) {
				CriteriaTriggers.SUMMONED_ENTITY.trigger(player, dummy);
			}
		}
	}
	
	//Copypaste of "createNewEndGateway", but simply returns the BlockPos instead of actually creating a gateway there.
	//Also peeks the gateway list with "get" instead of popping with "remove".
	@Unique private @Nullable BlockPos gatewayDryRun() {
		if(this.gateways.isEmpty()) return null;
		else {
			int i = this.gateways.get(this.gateways.size() - 1);
			int j = Mth.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
			int k = Mth.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
			return new BlockPos(j, 75, k);
		}
	}
}
