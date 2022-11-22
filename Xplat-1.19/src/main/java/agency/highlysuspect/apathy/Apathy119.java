package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.hell.wrapper.DragonDuck;
import agency.highlysuspect.apathy.hell.LogFacade;
import agency.highlysuspect.apathy.hell.rule.Rule;
import agency.highlysuspect.apathy.rule.PartialSpecDefenderInPlayerSet;
import agency.highlysuspect.apathy.rule.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.rule.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.rule.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.rule.PartialSpecDefenderHasAdvancement;
import agency.highlysuspect.apathy.rule.PartialSpecDifficultyIs;
import agency.highlysuspect.apathy.rule.PartialSpecLocation;
import agency.highlysuspect.apathy.rule.PartialSpecRevengeTimer;
import agency.highlysuspect.apathy.rule.PartialSpecScore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Apathy119 extends ApathyHell {
	public static Apathy119 instance119;
	
	public GeneralConfig generalConfig = new GeneralConfig();
	public MobConfig mobConfig = new MobConfig();
	public BossConfig bossConfig = new BossConfig();
	
	public Apathy119(Path configPath) {
		super(configPath, new Log4jLoggingFacade(LogManager.getLogger(ApathyHell.MODID)));
		
		Apathy119.instance119 = this;
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
	
	public void filterMobEffectUtilCall(ServerLevel level, @Nullable Entity provoker, List<ServerPlayer> original) {
		if(provoker instanceof Warden warden) {
			if(!bossConfig.wardenDarknessDifficulties.contains(level.getDifficulty())) original.clear();
			if(bossConfig.wardenDarknessOnlyToPlayersItCanTarget) original.removeIf(player -> !mobConfig.allowedToTargetPlayer(warden, player));
		}
	}
	
	//TODO delete this lol
	public static Set<Difficulty> allDifficultiesNotPeaceful() {
		Set<Difficulty> wow = ApathyHell.allOf(Difficulty.class);
		wow.remove(Difficulty.PEACEFUL);
		return wow;
	}
	
	//TODO really delete this one (need to look at config stuff again)
	@Deprecated(forRemoval = true)
	public static Set<ApathyDifficulty> skillIssue(Set<Difficulty> old) {
		return old.stream().map(d -> switch(d) {
			case PEACEFUL -> ApathyDifficulty.PEACEFUL;
			case EASY -> ApathyDifficulty.EASY;
			case NORMAL -> ApathyDifficulty.NORMAL;
			case HARD -> ApathyDifficulty.HARD;
		}).collect(Collectors.toSet());
	}
	
	private record Log4jLoggingFacade(Logger log) implements LogFacade {
		@Override
		public void info(String message, Object... args) {
			log.info(message, args);
		}
		
		@Override
		public void warn(String message, Object... args) {
			log.warn(message, args);
		}
		
		@Override
		public void error(String message, Object... args) {
			log.error(message, args);
		}
	}
	
	@Override
	public void addPlatformSpecificRules() {
		partialSerializers.register("apathy:advancements", PartialSpecDefenderHasAdvancement.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_is", PartialSpecAttackerIs.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_is_boss", PartialSpecAttackerIsBoss.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_tagged_with", PartialSpecAttackerTaggedWith.Serializer.INSTANCE);
		partialSerializers.register("apathy:difficulty_is", PartialSpecDifficultyIs.Serializer.INSTANCE);
		partialSerializers.register("apathy:in_player_set", PartialSpecDefenderInPlayerSet.Serializer.INSTANCE);
		partialSerializers.register("apathy:location", PartialSpecLocation.Serializer.INSTANCE);
		partialSerializers.register("apathy:revenge_timer", PartialSpecRevengeTimer.Serializer.INSTANCE);
		partialSerializers.register("apathy:score", PartialSpecScore.Serializer.INSTANCE);
	}
}
