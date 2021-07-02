package agency.highlysuspect.apathy.mixin.dragon;

import agency.highlysuspect.apathy.Init;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonSpawnState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.gen.feature.EndPortalFeature;
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
@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {
	//@Shadow @Final private static Logger LOGGER;
	@Shadow @Final private static Predicate<Entity> VALID_ENTITY;
	@Shadow @Final private ServerBossBar bossBar;
	@Shadow @Final private ServerWorld world;
	@Shadow @Final private List<Integer> gateways;
	//@Shadow @Final private BlockPattern endPortalPattern;
	//@Shadow private int dragonSeenTimer;
	//@Shadow private int endCrystalsAlive;
	//@Shadow private int crystalCountTimer;
	//@Shadow private int playerUpdateTimer;
	@Shadow private boolean dragonKilled;
	//@Shadow private boolean previouslyKilled;
	@Shadow private UUID dragonUuid;
	@Shadow private boolean doLegacyCheck;
	@Shadow private BlockPos exitPortalLocation;
	@Shadow private EnderDragonSpawnState dragonSpawnState;
	//@Shadow private int spawnStateTimer;
	@Shadow private List<EndCrystalEntity> crystals;
	
	@Shadow protected abstract void generateEndPortal(boolean previouslyKilled);
	@Shadow protected abstract boolean loadChunks();
	@Shadow protected abstract void generateNewEndGateway();
	
	@Unique private boolean createdApathyPortal;
	@Unique private int gatewayTimer = NOT_RUNNING;
	@Unique private static final int NOT_RUNNING = -100;
	
	@Unique private static final String APATHY_CREATEDPORTAL = "apathy-created-exit-portal";
	@Unique private static final String APATHY_GATEWAYTIMER = "apathy-gateway-timer";
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void onInit(ServerWorld world, long l, NbtCompound tag, CallbackInfo ci) {
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
	void whenTagging(CallbackInfoReturnable<NbtCompound> cir) {
		NbtCompound tag = cir.getReturnValue();
		
		tag.putBoolean(APATHY_CREATEDPORTAL, createdApathyPortal);
		tag.putInt(APATHY_GATEWAYTIMER, gatewayTimer);
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	void dontTick(CallbackInfo ci) {
		if(Init.bossConfig.noDragon) {
			ci.cancel();
			
			//Just to be like triple sure there's no ender dragons around
			this.bossBar.clearPlayers();
			this.bossBar.setVisible(false);
			
			for(EnderDragonEntity dragon : world.getAliveEnderDragons()) {
				dragon.discard();
			}
			
			//Issue a chunk ticket if there's anyone nearby. Same as how chunks are normally loaded during the boss.
			//Special mechanics like the apathy exit portal & the gateway mechanic require chunks to be loaded.
			List<ServerPlayerEntity> players = world.getPlayers(VALID_ENTITY);
			if(players.isEmpty()) {
				this.world.getChunkManager().removeTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
			} else {
				this.world.getChunkManager().addTicket(ChunkTicketType.DRAGON, new ChunkPos(0, 0), 9, Unit.INSTANCE);
				
				//Also automatically grant "Free the End" advancement
				//(this also grants "monster hunter" if you don't have it already but w/e)
				EnderDragonEntity dummy = EntityType.ENDER_DRAGON.create(world);
				for(ServerPlayerEntity player : players) {
					Criteria.PLAYER_KILLED_ENTITY.trigger(player, dummy, DamageSource.ANVIL);
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
	void dontSetSpawnState(EnderDragonSpawnState enderDragonSpawnState, CallbackInfo ci) {
		//This mixin is required if createDragon is overridden to return 'null'; it calls createDragon and would NPE
		if(Init.bossConfig.noDragon) {
			this.dragonSpawnState = null;
			ci.cancel();
		}
	}
	
	@Inject(method = "createDragon", at = @At("HEAD"), cancellable = true)
	void dontCreateDragon(CallbackInfoReturnable<EnderDragonEntity> cir) {
		if(Init.bossConfig.noDragon) {
			cir.setReturnValue(null);
		}
	}
	
	@Inject(method = "respawnDragon(Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
	void dontRespawnDragon(List<EndCrystalEntity> crystals, CallbackInfo ci) {
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
			this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN), Blocks.DRAGON_EGG.getDefaultState());
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
		BlockPos oneAboveThat = exitPos.up();
		for(Direction d : Direction.Type.HORIZONTAL) {
			for(EndCrystalEntity crystal : this.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(oneAboveThat.offset(d, 2)))) {
				crystal.setBeamTarget(null);
				world.createExplosion(crystal, crystal.getX(), crystal.getY(), crystal.getZ(), 6.0F, Explosion.DestructionType.NONE);
				crystal.discard();
			}
		}
		
		//Grant the advancement for resummoning the Ender Dragon (close enough)
		EnderDragonEntity dummy = EntityType.ENDER_DRAGON.create(world);
		for(ServerPlayerEntity player : world.getPlayers(VALID_ENTITY)) {
			Criteria.SUMMONED_ENTITY.trigger(player, dummy);
		}
	}
	
	@Unique private void tryEnderCrystalGateway(List<EndCrystalEntity> crystalsAroundEndPortal) {
		if(gatewayTimer == NOT_RUNNING) {
			BlockPos pos = gatewayDryRun();
			if(pos != null) {
				BlockPos downABit = pos.down(2); //where the actual gateway block will be
				for(EndCrystalEntity crystal : crystalsAroundEndPortal) {
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
			int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
			int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
			return new BlockPos(j, 75, k);
		}
	}
}
