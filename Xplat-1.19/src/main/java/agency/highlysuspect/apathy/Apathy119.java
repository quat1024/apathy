package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.LogFacade;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.rule.RuleSpecChain;
import agency.highlysuspect.apathy.rule.RuleSpecDebug;
import agency.highlysuspect.apathy.rule.RuleSpecDifficultyCase;
import agency.highlysuspect.apathy.rule.RuleSpecJson;
import agency.highlysuspect.apathy.rule.RuleSpecPredicated;
import agency.highlysuspect.apathy.rule.RuleSpec;
import agency.highlysuspect.apathy.rule.Specs;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Apathy119 extends ApathyHell {
	public static Apathy119 instance119;
	
	public GeneralConfig generalConfig = new GeneralConfig();
	public MobConfig mobConfig = new MobConfig();
	public BossConfig bossConfig = new BossConfig();
	public @Nullable Rule jsonRule;
	
	public Apathy119(Path configPath) {
		super(configPath, new Log4jLoggingFacade(LogManager.getLogger(ApathyHell.MODID)));
		
		Apathy119.instance119 = this;
	}
	
	public void init() {
		//Register all the weird json rule stuff
		Specs.onInitialize();
		
		//Actually register all the weird json rule stuff with the new system TODO find a better home for this
		ruleSerializers.register("apathy:always", RuleSpecAlways.AlwaysRuleSerializer.INSTANCE);
		ruleSerializers.register("apathy:chain", RuleSpecChain.ChainRuleSerializer.INSTANCE);
		ruleSerializers.register("apathy:predicated", RuleSpecPredicated.PredicatedRuleSpecSerializer.INSTANCE);
		ruleSerializers.register("apathy:allow_if", RuleSpecPredicated.AllowIfRuleSerializer.INSTANCE);
		ruleSerializers.register("apathy:deny_if", RuleSpecPredicated.DenyIfRuleSerializer.INSTANCE);
		ruleSerializers.register("apathy:debug", RuleSpecDebug.DebugRuleSerializer.INSTANCE);
		ruleSerializers.register("apathy:difficulty_case", RuleSpecDifficultyCase.DifficultyCaseRuleSerializer.INSTANCE);
		ruleSerializers.register("apathy:evaluate_json_file", RuleSpecJson.JsonRuleSerializer.INSTANCE);
		
		super.init();
	}
	
	//TODO HELL: find a better home for these
	public RuleSpec<?> readRule(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject json)) throw new IllegalArgumentException("Not json object");
		
		String type = json.getAsJsonPrimitive("type").getAsString();
		RuleSerializer<?> pee = ruleSerializers.get(type);
		return (RuleSpec<?>) pee.read(json); //TODO actually unchecked, it's SerializableRule stuff
	}
	
	public JsonObject writeRule(RuleSpec<?> rule) {
		JsonObject ok = new JsonObject();
		ok.addProperty("type", ruleSerializers.getName(rule.getSerializer()));
		rule.getSerializer().writeErased(rule, ok);
		return ok;
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
}
