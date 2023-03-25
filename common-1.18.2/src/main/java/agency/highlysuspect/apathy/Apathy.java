package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import agency.highlysuspect.apathy.rule.spec.Specs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Apathy {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	
	public static final Path CONFIG_FOLDER = PlatformSupport.instance.getConfigPath();
	
	public static MobConfig mobConfig;
	public static GeneralConfig generalConfig;
	public static BossConfig bossConfig;
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	public static void init() {
		//Ensure the config subdirectory exists and things can be placed inside it
		try {
			Files.createDirectories(CONFIG_FOLDER);
		} catch (IOException e) {
			throw new RuntimeException("Problem creating config/apathy/ subdirectory", e);
		}
		
		Specs.onInitialize();
		
		//The config file reloader should take care of this.
		//See https://github.com/quat1024/apathy/issues/9 .
		//loadConfig();
		
		PlatformSupport.instance.initialize();
	}
	
	public static void loadConfig() {
		GeneralConfig oldGeneralConfig = generalConfig;
		MobConfig oldMobConfig = mobConfig;
		BossConfig oldBossConfig = bossConfig;
		
		try {
			generalConfig = Config.read(new GeneralConfig(), CONFIG_FOLDER.resolve("general.cfg"));
			mobConfig = Config.read(new MobConfig(), CONFIG_FOLDER.resolve("mobs.cfg"));
			bossConfig = Config.read(new BossConfig(), CONFIG_FOLDER.resolve("boss.cfg"));
			
			//todo this is kinda tacked on
			JsonRule.loadJson();
		} catch (Exception e) {
			if(oldGeneralConfig == null && oldMobConfig == null && oldBossConfig == null) {
				throw new RuntimeException("Problem initializing config file.", e);
			} else {
				generalConfig = oldGeneralConfig;
				mobConfig = oldMobConfig;
				bossConfig = oldBossConfig;
				LOG.error("Problem reloading config file: ", e);
				LOG.error("The current config has not been changed. Resolve the error, and try loading the config file again.");
			}
		}
	}
	
	public static void noticePlayerAttack(Player player, Entity provoked) {
		Level level = player.level;
		if(level.isClientSide) return;
		
		if(provoked instanceof MobExt ext) {
			//Set the revengetimer on the hit entity
			ext.apathy$provokeNow();
			
			if(Apathy.generalConfig.sameTypeRevengeSpread > 0) {
				for(Entity nearby : level.getEntitiesOfClass(provoked.getClass(), provoked.getBoundingBox().inflate(Apathy.generalConfig.sameTypeRevengeSpread))) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
			
			if(Apathy.generalConfig.differentTypeRevengeSpread > 0) {
				//kinda grody sorry
				for(Entity nearby : level.getEntities((Entity) null, provoked.getBoundingBox().inflate(Apathy.generalConfig.differentTypeRevengeSpread), ent -> ent instanceof MobExt)) {
					if(nearby instanceof MobExt extt) extt.apathy$provokeNow();
				}
			}
		}
		
		//handle the "peaceful-at-the-start dragon" option
		if(provoked instanceof DragonDuck dragn) dragn.apathy$allowAttackingPlayers();
	}
	
	//Random util crap
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
}
