package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonSpawnState;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {
	//@Shadow @Final private static Logger LOGGER;
	//@Shadow @Final private static Predicate<Entity> VALID_ENTITY;
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
	//@Shadow private BlockPos exitPortalLocation;
	@Shadow private EnderDragonSpawnState dragonSpawnState;
	//@Shadow private int spawnStateTimer;
	@Shadow private List<EndCrystalEntity> crystals;
	
	@Shadow protected abstract void generateEndPortal(boolean previouslyKilled);
	@Shadow protected abstract boolean loadChunks();
	@Shadow protected abstract void generateNewEndGateway();
	
	@Shadow private BlockPos exitPortalLocation;
	@Unique private boolean createdApathyPortal;
	@Unique private int specialCreateGatewayTimer;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	void onInit(ServerWorld world, long l, CompoundTag tag, CallbackInfo ci) {
		createdApathyPortal = tag.getBoolean("apathy-createdPortal");
		specialCreateGatewayTimer = tag.getInt("apathy-specialGatewayCreate");
		
		if(Init.bossConfig.noDragon) {
			dragonKilled = true; //sigh
			dragonUuid = null;
			doLegacyCheck = false;
			dragonSpawnState = null;
		}
	}
	
	@Inject(method = "toTag", at = @At(value = "RETURN"))
	void whenTagging(CallbackInfoReturnable<CompoundTag> cir) {
		CompoundTag tag = cir.getReturnValue();
		
		tag.putBoolean("apathy-createdPortal", createdApathyPortal);
		tag.putInt("apathy-specialGatewayCreate", specialCreateGatewayTimer);
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	void dontTick(CallbackInfo ci) {
		if(Init.bossConfig.noDragon) {
			ci.cancel();
			
			//Just to be sure...
			this.bossBar.clearPlayers();
			this.bossBar.setVisible(false);
			
			for(EnderDragonEntity dragon : world.getAliveEnderDragons()) {
				dragon.remove();
			}
			
			createApathyPortal();
			specialGatewayTick();
		}
	}
	
	@Inject(method = "setSpawnState", at = @At("HEAD"), cancellable = true)
	void dontSetSpawnState(EnderDragonSpawnState enderDragonSpawnState, CallbackInfo ci) {
		//This mixin is required if createDragon is overridden to return 'null'
		//it calls createDragon and would NPE
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
		if(Init.bossConfig.noDragon) {
			ci.cancel();
			
			trySpecialGatewayCreate(crystals);
		}
	}
	
	@Unique private void createApathyPortal() {
		if(!createdApathyPortal && loadChunks()) {
			generateEndPortal(true);
			this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN), Blocks.DRAGON_EGG.getDefaultState());
			createdApathyPortal = true;
		}
	}
	
	@Unique private void specialGatewayTick() {
		if(specialCreateGatewayTimer > 0 && loadChunks()) {
			specialCreateGatewayTimer--;
			if(specialCreateGatewayTimer == 0) {
				doSpecialGatewaySpawn();
			}
		}
	}
	
	@Unique private void doSpecialGatewaySpawn() {
		generateNewEndGateway(); //Actually generate it now
		
		//Blow up the crystals located on the end portal.
		//(Yes, this means you can smuggle them away with a piston, just like vanilla lol.)
		BlockPos exitPos = exitPortalLocation;
		BlockPos oneAboveThat = exitPos.up();
		for(Direction d : Direction.Type.HORIZONTAL) {
			for(EndCrystalEntity crystal : this.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(oneAboveThat.offset(d, 2)))) {
				crystal.setBeamTarget(null);
				world.createExplosion(crystal, crystal.getX(), crystal.getY(), crystal.getZ(), 6.0F, Explosion.DestructionType.NONE);
				crystal.remove();
			}
		}
	}
	
	@Unique private void trySpecialGatewayCreate(List<EndCrystalEntity> crystalsAroundEndPortal) {
		BlockPos pos = gatewayDryRun();
		if(pos != null) {
			//The "gateway position" as returned by that method, is the top piece of bedrock.
			//Shift down to the actual location of the gateway portal block.
			//(Makes it look a tiny bit better.)
			BlockPos downABit = pos.down(2);
			for(EndCrystalEntity crystal : crystalsAroundEndPortal) {
				crystal.setBeamTarget(downABit);
			}
			
			this.crystals = crystalsAroundEndPortal;
			specialCreateGatewayTimer = 100;
		}
	}
	
	//Based on "createNewEndGateway" ,but simply returns the BlockPos instead of creating a gateway there.
	//Also peeks the gateway list with "get" instead of calling "remove".
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
