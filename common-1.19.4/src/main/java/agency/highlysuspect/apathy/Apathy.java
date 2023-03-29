package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.spec.Specs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Apathy {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	public static Apathy INSTANCE;
	
	public final Path configFolder;
	
	public GeneralConfig generalConfig = new GeneralConfig();
	public MobConfig mobConfig = new MobConfig();
	public BossConfig bossConfig = new BossConfig();
	public @Nullable Rule jsonRule;
	
	public Apathy() {
		Apathy.INSTANCE = this;
		configFolder = getConfigPath();
	}
	
	public void init() {
		//Ensure the config subdirectory exists and things can be placed inside it
		try {
			Files.createDirectories(configFolder);
		} catch (IOException e) {
			throw new RuntimeException("Problem creating config/apathy/ subdirectory", e);
		}
		
		//Register all the weird json rule stuff
		Specs.onInitialize();
		
		//Don't load the config files yet, this should happen on server resource load instead.
		//installConfigFileReloader should set this up.
		//See https://github.com/quat1024/apathy/issues/9 . This is kind of embarassing...
		//loadConfig();
		
		//Platform dependent init
		installConfigFileReloader();
		installCommandRegistrationCallback();
		installPlayerSetManagerTicker();
	}
	
	public boolean loadConfig() {
		boolean ok = true;
		
		GeneralConfig newGeneralConfig = generalConfig;
		try {
			newGeneralConfig = Config.read(new GeneralConfig(), configFolder.resolve("general.cfg"));
		} catch (Exception e) {
			LOG.error("Problem reading general.cfg:", e);
			ok = false;
		} finally {
			generalConfig = newGeneralConfig;
		}
		
		MobConfig newMobConfig = mobConfig;
		try {
			newMobConfig = Config.read(new MobConfig(), configFolder.resolve("mobs.cfg"));
		} catch (Exception e) {
			LOG.error("Problem reading mobs.cfg: ", e);
			ok = false;
		} finally {
			mobConfig = newMobConfig;
		}
		
		BossConfig newBossConfig = bossConfig;
		try {
			newBossConfig = Config.read(new BossConfig(), configFolder.resolve("boss.cfg"));
		} catch (Exception e) {
			LOG.error("Problem reading boss.cfg: ", e);
			ok = false;
		} finally {
			bossConfig = newBossConfig;
		}
		
		Rule newJsonRule = jsonRule;
		try {
			newJsonRule = JsonRule.loadJson(configFolder.resolve("mobs.json"));
		} catch (Exception e) {
			LOG.error("Problem reading mobs.json: ", e);
			ok = false;
		} finally {
			jsonRule = newJsonRule;
		}
		
		return ok;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted") //But it makes more sense that way!
	public boolean allowedToTargetPlayer(Mob attacker, ServerPlayer player) {
		if(attacker.level.isClientSide) throw new IllegalStateException("Do not call on the client, please");
		
		TriState result = mobConfig.rule.apply(attacker, player);
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
	
	public void filterMobEffectUtilCall(ServerLevel level, @Nullable Entity provoker, List<ServerPlayer> original) {
		if(provoker instanceof Warden warden) {
			if(!bossConfig.wardenDarknessDifficulties.contains(level.getDifficulty())) original.clear();
			if(bossConfig.wardenDarknessOnlyToPlayersItCanTarget) original.removeIf(player -> !allowedToTargetPlayer(warden, player));
		}
	}
	
	/// Random util crap
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
	public abstract Path getConfigPath();
}
