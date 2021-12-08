package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import agency.highlysuspect.apathy.rule.spec.Specs;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Apathy {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	
	public static Path CONFIG_FOLDER;
	
	public static MobConfig mobConfig;
	public static GeneralConfig generalConfig;
	public static BossConfig bossConfig;
	
	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
	
	public static void setup() {
		//might be able to move this into static init, worried a bit about a classloading cycle though
		CONFIG_FOLDER = PlatformSupport.INSTANCE.getConfigDir();
		
		//Ensure the config subdirectory exists and things can be placed inside it
		try {
			Files.createDirectories(CONFIG_FOLDER);
		} catch (IOException e) {
			throw new RuntimeException("Problem creating config directory", e);
		}
		
		//Various subsystems
		Specs.onInitialize();
		
		Commands.onInitialize();
		PlayerSetManager.onInitialize();
		
		//Config file stuff
		PlatformSupport.INSTANCE.installResourceReloader0("reload-config", Apathy::reloadConfig);
		
		PlatformSupport.INSTANCE.registerProvocationDetector();
	}
	
	private static void reloadConfig() {
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
}
