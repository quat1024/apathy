package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.math.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.DragonRespawnAnimation;
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
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("SameParameterValue")
@Mixin(EndDragonFight.class)
public abstract class EnderDragonFightMixin {
	//a zillion shadows
	@Shadow @Final private static Predicate<Entity> VALID_ENTITY;
	@Shadow @Final private ServerBossEvent bossBar;
	@Shadow @Final private ServerLevel world;
	@Shadow @Final private List<Integer> gateways;
	@Shadow private boolean dragonKilled;
	@Shadow private UUID dragonUuid;
	@Shadow private boolean doLegacyCheck;
	@Shadow private BlockPos exitPortalLocation;
	@Shadow private DragonRespawnAnimation dragonSpawnState;
	@Shadow private List<EndCrystal> crystals;
	
	@Shadow protected abstract void generateEndPortal(boolean previouslyKilled);
	@Shadow protected abstract boolean loadChunks();
	@Shadow protected abstract void generateNewEndGateway();
	
	//my additions
	
	@Unique private boolean createdApathyPortal;
	@Unique private int gatewayTimer = NOT_RUNNING;
	@Unique private static final int NOT_RUNNING = -100;
	
	@Unique private static final String APATHY_CREATEDPORTAL = "apathy-created-exit-portal";
	@Unique private static final String APATHY_GATEWAYTIMER = "apathy-gateway-timer";
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void onInit(ServerLevel world, long l, CompoundTag tag, CallbackInfo ci) {
		createdApathyPortal = tag.getBoolean(APATHY_CREATEDPORTAL);
		if(tag.contains(APATHY_GATEWAYTIMER)) {
			gatewayTimer = tag.getInt(APATHY_GATEWAYTIMER);
		} else {
			gatewayTimer = NOT_RUNNING;
		}
		
		if(Init.bossConfig.noDragon) {
			dragonKilled = true; //sigh
			dragonUuid = null;
			doLegacyCheck = false;
			dragonSpawnState = null;
		}
	}
	
	@Inject(method = "toNbt", at = @At(value = "RETURN"))
	void whenTagging(CallbackInfoReturnable<CompoundTag> cir) {
		CompoundTag tag = cir.getReturnValue();
		
		tag.putBoolean(APATHY_CREATEDPORTAL, createdApathyPortal);
		tag.putInt(APATHY_GATEWAYTIMER, gatewayTimer);
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	void dontTick(CallbackInfo ci) {
		if(Init.bossConfig.noDragon) {
			ci.cancel();
			
			//Just to be like triple sure there's no ender dragons around
			this.bossBar.removeAllPlayers();
			this.bossBar.setVisible(false);
			
			for(EnderDragon dragon : world.getDragons()) {
				dragon.discard();
			}
			
			//Issue a chunk ticket if there's anyone nearby. Same as how chunks are normally loaded during the boss.
			//Special mechanics like the apathy exit portal & the gateway mechanic require chunks to be loaded.
			List<ServerPlayer> players = world.getPlayers(VALID_ENTITY);
			if(players.isEmpty()) {
				this.world.getChunkSource().removeRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
			} else {
				this.world.getChunkSource().addRegionTicket(TicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
				
				//Also automatically grant "Free the End" advancement
				//(this also grants "monster hunter" if you don't have it already but w/e)
				EnderDragon dummy = EntityType.ENDER_DRAGON.create(world);
				for(ServerPlayer player : players) {
					CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, dummy, DamageSource.ANVIL);
				}
			}
			
			boolean chunksReady = loadChunks();
			if(chunksReady) {
				createApathyPortal();
				gatewayTimerTick();
			}
		}
	}
	
	@Inject(method = "setSpawnState", at = @At("HEAD"), cancellable = true)
	void dontSetSpawnState(DragonRespawnAnimation enderDragonSpawnState, CallbackInfo ci) {
		//This mixin is required if createDragon is overridden to return 'null'; it calls createDragon and would NPE
		if(Init.bossConfig.noDragon) {
			this.dragonSpawnState = null;
			ci.cancel();
		}
	}
	
	@Inject(method = "createDragon", at = @At("HEAD"), cancellable = true)
	void dontCreateDragon(CallbackInfoReturnable<EnderDragon> cir) {
		if(Init.bossConfig.noDragon) {
			cir.setReturnValue(null);
		}
	}
	
	@Inject(method = "respawnDragon(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
	void dontRespawnDragon(List<EndCrystal> crystals, CallbackInfo ci) {
		//respawnDragon-no-args handles detecting the 4 end crystals by the exit portal.
		//respawnDragon-list-arg gets called with the list of end crystals, if there are four, and normally actually summons the boss.
		if(Init.bossConfig.noDragon) {
			ci.cancel();
			
			tryEnderCrystalGateway(crystals);
		}
	}
	
	@Unique private void createApathyPortal() {
		//Generate a readymade exit End portal, just like after the boss fight.
		//("generateEndPortal" updates the "exit portal location" blockpos variable btw.)
		//Ensure chunks are loaded before calling this, or the portal will generate at y = -1 for some reason.
		if(!createdApathyPortal) {
			generateEndPortal(true);
			this.world.setBlockAndUpdate(this.world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.END_PODIUM_LOCATION), Blocks.DRAGON_EGG.defaultBlockState());
			createdApathyPortal = true;
		}
	}
	
	@Unique private void gatewayTimerTick() {
		//Tick down the timer for the "special gateway creation" timer.
		//Spawns the gateway when it reaches 0.
		//Ensure chunks are loaded before calling this.
		if(gatewayTimer != NOT_RUNNING) {
			if(gatewayTimer > 0) {
				gatewayTimer--;
			} else {
				doGatewaySpawn();
				gatewayTimer = NOT_RUNNING;
			}
		}
	}
	
	//The end of the "spawn gateway" cutscene
	@Unique private void doGatewaySpawn() {
		generateNewEndGateway(); //Actually generate it now
		
		//Blow up the crystals located on the end portal.
		//(Yes, this means you can smuggle them away with a piston, just like vanilla lol.)
		BlockPos exitPos = exitPortalLocation;
		BlockPos oneAboveThat = exitPos.above();
		for(Direction d : Direction.Plane.HORIZONTAL) {
			for(EndCrystal crystal : this.world.getEntitiesOfClass(EndCrystal.class, new AABB(oneAboveThat.relative(d, 2)))) {
				crystal.setBeamTarget(null);
				world.explode(crystal, crystal.getX(), crystal.getY(), crystal.getZ(), 6.0F, Explosion.BlockInteraction.NONE);
				crystal.discard();
			}
		}
		
		//Grant the advancement for resummoning the Ender Dragon (close enough)
		EnderDragon dummy = EntityType.ENDER_DRAGON.create(world);
		for(ServerPlayer player : world.getPlayers(VALID_ENTITY)) {
			CriteriaTriggers.SUMMONED_ENTITY.trigger(player, dummy);
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
				
				this.crystals = crystalsAroundEndPortal;
				gatewayTimer = 100; //5 seconds
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
