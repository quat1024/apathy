package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.TriState;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.core.wrapper.DragonDuck;
import agency.highlysuspect.apathy.core.wrapper.MobExt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Apathy118 extends ApathyHell {
	public static Apathy118 instance118;
	
	public GeneralConfig generalConfig = new GeneralConfig();
	public MobConfig mobConfig = new MobConfig();
	public BossConfig bossConfig = new BossConfig();
	public @Nullable Rule jsonRule;
	
	public Apathy118(Path configPath) {
		super(configPath, CoreConv.toLogFacade(LogManager.getLogger(MODID)));
		
		Apathy118.instance118 = this;
	}
	
	public boolean loadConfig() {
		boolean ok = true;
		
		GeneralConfig newGeneralConfig = generalConfig;
		try {
			newGeneralConfig = Config.read(new GeneralConfig(), configPath.resolve("general.cfg"));
		} catch (Exception e) {
			log.error("Problem reading general.cfg:", e);
			ok = false;
		} finally {
			generalConfig = newGeneralConfig;
		}
		
		MobConfig newMobConfig = mobConfig;
		try {
			newMobConfig = Config.read(new MobConfig(), configPath.resolve("mobs.cfg"));
		} catch (Exception e) {
			log.error("Problem reading mobs.cfg: ", e);
			ok = false;
		} finally {
			mobConfig = newMobConfig;
		}
		
		BossConfig newBossConfig = bossConfig;
		try {
			newBossConfig = Config.read(new BossConfig(), configPath.resolve("boss.cfg"));
		} catch (Exception e) {
			log.error("Problem reading boss.cfg: ", e);
			ok = false;
		} finally {
			bossConfig = newBossConfig;
		}
		
		Rule newJsonRule = jsonRule;
		try {
			newJsonRule = JsonRule.loadJson(configPath.resolve("mobs.json"));
		} catch (Exception e) {
			log.error("Problem reading mobs.json: ", e);
			ok = false;
		} finally {
			jsonRule = newJsonRule;
		}
		
		return ok;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //But it makes more sense that way!
	public boolean allowedToTargetPlayer(Mob attacker, ServerPlayer player) {
		if(attacker.level.isClientSide) throw new IllegalStateException("Do not call on the client, please");
		
		TriState result = mobConfig.rule.apply((Attacker) attacker, (Defender) player);
		if(result != TriState.DEFAULT) return result.get();
		else return mobConfig.fallthrough;
	}
	
	public void noticePlayerAttack(Player player, Entity provoked) {
		Level level = player.level;
		if(level.isClientSide) return;
		
		if(provoked instanceof MobExt ext) {
			//Set the revengetimer on the hit entity
			ext.apathy$provokeNow();
			
			if(generalConfig.sameTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntitiesOfClass(provoked.getClass(), provoked.getBoundingBox().inflate(generalConfig.sameTypeRevengeSpread))) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
			
			if(generalConfig.differentTypeRevengeSpread > 0) {
				//kinda grody sorry
				for(Entity nearby : level.getEntities((Entity) null, provoked.getBoundingBox().inflate(generalConfig.differentTypeRevengeSpread), ent -> ent instanceof MobExt)) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
		}
		
		//handle the "peaceful-at-the-start dragon" option
		if(provoked instanceof DragonDuck dragn) dragn.apathy$allowAttackingPlayers();
	}
	
	//Random util crap
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public static <T extends Enum<?>> Set<T> allOf(Class<T> enumClass) {
		Set<T> set = new HashSet<>();
		Collections.addAll(set, enumClass.getEnumConstants());
		return set;
	}
	
	public static Set<Difficulty> allDifficultiesNotPeaceful() {
		Set<Difficulty> wow = allOf(Difficulty.class);
		wow.remove(Difficulty.PEACEFUL);
		return wow;
	}
	
	/// Cross platform stuff
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
}
