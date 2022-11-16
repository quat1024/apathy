package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.DragonDuck;
import agency.highlysuspect.apathy.config.BossConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Explosion;
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
	@Shadow @Final private ObjectArrayList<Integer> gateways;
	@Shadow private boolean dragonKilled;
	@Shadow private boolean previouslyKilled;
	@Shadow private BlockPos portalLocation;
	@Shadow private List<EndCrystal> respawnCrystals;
	
	@Shadow protected abstract boolean isArenaLoaded();
	@Shadow protected abstract void spawnNewGateway();
	@Shadow protected abstract void spawnExitPortal(boolean previouslyKilled);
	
	//my additions
	
	@Unique private boolean createdApathyPortal = false;
	@Unique private int gatewayTimer = NOT_RUNNING;
	@Unique private static final int NOT_RUNNING = -100;
	
	@Unique private static final String APATHY_CREATEDPORTAL = "apathy-created-exit-portal";
	@Unique private static final String APATHY_GATEWAYTIMER = "apathy-gateway-timer";
	
	@Unique private boolean apathyIsManagingTheInitialPortalVanillaDontLookPlease = false;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void apathy$onInit(ServerLevel world, long l, CompoundTag tag, CallbackInfo ci) {
		createdApathyPortal = tag.getBoolean(APATHY_CREATEDPORTAL);
		
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
		CompoundTag tag = cir.getReturnValue();
		
		tag.putBoolean(APATHY_CREATEDPORTAL, createdApathyPortal);
		tag.putInt(APATHY_GATEWAYTIMER, gatewayTimer);
	}
	
	//runs BEFORE vanilla tick().
	@Inject(method = "tick", at = @At("HEAD"))
	void apathy$dontTick(CallbackInfo ci) {
		//Vanilla tick() adds a chunk ticket that loads a region around the main End Island if there's anyone standing nearby.
		if(!isArenaLoaded()) return;
		
		//First-run tasks.
		if(!createdApathyPortal) {
			//1. If the End Portal was requested to be open by default, honor that.
			if(Apathy.INSTANCE.bossConfig.portalInitialState.isOpenByDefault()) {
				//boolean prop is "whether it's open or not".
				//this has computeIfAbsent semantics regarding the position of the portal - if the portal position is not already known,
				//it is computed from the heightmap (which is totally busted if !isArenaLoaded(), btw)
				spawnExitPortal(true);
				
				if(Apathy.INSTANCE.bossConfig.portalInitialState.hasEgg()) {
					this.level.setBlockAndUpdate(this.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.defaultBlockState());
				}
			}
			
			//2. If any End Gateways were requested to be open by default, generate those too. 
			for(int i = 0; i < Apathy.INSTANCE.bossConfig.initialEndGatewayCount; i++) {
				spawnNewGateway();
			}
			
			createdApathyPortal = true;
		}
		
		//3. Handle the ticker for the ResummonSequence.SPAWN_GATEWAY mechanic.
		if(gatewayTimer != NOT_RUNNING) {
			if(gatewayTimer > 0) {
				gatewayTimer--;
			} else {
				doGatewaySpawn();
				gatewayTimer = NOT_RUNNING;
			}
		}
		
		//4. Handle simulacra advancements.
		if(Apathy.INSTANCE.bossConfig.simulacraDragonAdvancements && Apathy.INSTANCE.bossConfig.dragonInitialState == BossConfig.DragonInitialState.CALM) {
			//this grants the "Free the End" advancement, in a kind of clunky way
			EnderDragon rarrrh = EntityType.ENDER_DRAGON.create(level);
			for(ServerPlayer player : level.getPlayers(VALID_PLAYER)) {
				CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, rarrrh, DamageSource.ANVIL);
			}
		}
	}
	
	//wait wait gimme a sec, i can explain
	@Inject(method = "scanState", at = @At("HEAD"))
	void apathy$startScanningState(CallbackInfo ci) {
		apathyIsManagingTheInitialPortalVanillaDontLookPlease = Apathy.INSTANCE.bossConfig.portalInitialState != BossConfig.PortalInitialState.CLOSED;
	}
	
	@Inject(method = "scanState", at = @At("RETURN"))
	void apathy$finishScanningState(CallbackInfo ci) {
		apathyIsManagingTheInitialPortalVanillaDontLookPlease = false;
		
		//scanState is called ONCE, EVER, the very first time any player loads the End. It is never called again.
		//It is also called before vanilla code spawns the initial Ender Dragon.
		//This is the perfect time to set the magic "do not automatically spawn an enderdragon" variable if the
		//player has requested for the initial dragon to be removed.
		if(Apathy.INSTANCE.bossConfig.dragonInitialState == BossConfig.DragonInitialState.CALM) {
			dragonKilled = true; //This is the magic variable.
			previouslyKilled = true;
		}
	}
	
	//the SUPER AWESOME ULTRA TURBO MEGA HACK:
	//so if Apathy creates an already-opened End portal, it tends to confuse the shit out of the vanilla scanState logic
	//it takes the existence of any End Portal block entities at all to mean "the dragon was already killed" and it does not
	//spawn a dragon on first login. Because "already opened end portal" + "dragon" is a valid setup in apathy, i need to bop
	//this shit on the head, the solution is to prevent endportals from being discovered in scanState.
	@Inject(method = "hasActiveExitPortal", at = @At("HEAD"), cancellable = true)
	void apathy$bopActiveExitPortal(CallbackInfoReturnable<Boolean> cir) {
		if(apathyIsManagingTheInitialPortalVanillaDontLookPlease) cir.setReturnValue(false);
	}
	
	@Inject(method = "createNewDragon", at = @At("RETURN"))
	void apathy$whenCreatingDragon(CallbackInfoReturnable<EnderDragon> cir) {
		if(!previouslyKilled && Apathy.INSTANCE.bossConfig.dragonInitialState == BossConfig.DragonInitialState.PASSIVE_DRAGON) {
			((DragonDuck) cir.getReturnValue()).apathy$disallowAttackingPlayers();
		}
	}
	
	//tryRespawn handles detecting the 4 end crystals by the exit portal.
	//respawnDragon gets called with the list of end crystals if there are four, and actually summons the boss.
	@Inject(method = "respawnDragon(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
	void apathy$whenBeginningRespawnSequence(List<EndCrystal> crystals, CallbackInfo ci) {
		switch(Apathy.INSTANCE.bossConfig.resummonSequence) {
			case DEFAULT -> {} //Nothing to do.
			case DISABLED -> ci.cancel();
			case SPAWN_GATEWAY -> {
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
				level.explode(crystal, crystal.getX(), crystal.getY(), crystal.getZ(), 6.0F, Explosion.BlockInteraction.NONE);
				crystal.discard();
			}
		}
		
		//Grant the advancement for resummoning the Ender Dragon (close enough)
		if(Apathy.INSTANCE.bossConfig.simulacraDragonAdvancements) {
			EnderDragon secretDragn = EntityType.ENDER_DRAGON.create(level);
			for(ServerPlayer player : level.getPlayers(VALID_PLAYER)) {
				CriteriaTriggers.SUMMONED_ENTITY.trigger(player, secretDragn);
			}
		}
	}
	
	//Copypaste of "spawnNewGateway()", but simply returns the BlockPos instead of continuing on to actually creating a gateway.
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
