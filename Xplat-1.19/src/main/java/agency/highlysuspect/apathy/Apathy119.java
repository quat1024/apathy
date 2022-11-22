package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.LogFacade;
import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.rule.RuleSpecChain;
import agency.highlysuspect.apathy.rule.RuleSpecDebug;
import agency.highlysuspect.apathy.rule.RuleSpecDifficultyCase;
import agency.highlysuspect.apathy.rule.RuleSpecJson;
import agency.highlysuspect.apathy.rule.RuleSpecPredicated;
import agency.highlysuspect.apathy.rule.RuleSpec;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecDefenderInPlayerSet;
import agency.highlysuspect.apathy.rule.predicate.PartialSpec;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAll;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAlways;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAny;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecDefenderHasAdvancement;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecDifficultyIs;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecLocation;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecNot;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecRevengeTimer;
import agency.highlysuspect.apathy.rule.predicate.PartialSpecScore;
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
		//TODO find a better home for this
		ruleSerializers.register("apathy:allow_if", RuleSpecPredicated.AllowIfSerializer.INSTANCE);
		ruleSerializers.register("apathy:always", RuleSpecAlways.Serializer.INSTANCE);
		ruleSerializers.register("apathy:chain", RuleSpecChain.Serializer.INSTANCE);
		ruleSerializers.register("apathy:debug", RuleSpecDebug.Serializer.INSTANCE);
		ruleSerializers.register("apathy:deny_if", RuleSpecPredicated.DenyIfSerializer.INSTANCE);
		ruleSerializers.register("apathy:difficulty_case", RuleSpecDifficultyCase.Serializer.INSTANCE);
		ruleSerializers.register("apathy:evaluate_json_file", RuleSpecJson.Serializer.INSTANCE);
		ruleSerializers.register("apathy:predicated", RuleSpecPredicated.PredicatedSerializer.INSTANCE);
		
		partialSerializers.register("apathy:advancements", PartialSpecDefenderHasAdvancement.Serializer.INSTANCE);
		partialSerializers.register("apathy:all", PartialSpecAll.Serializer.INSTANCE);
		partialSerializers.register("apathy:always", PartialSpecAlways.Serializer.INSTANCE);
		partialSerializers.register("apathy:any", PartialSpecAny.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_is", PartialSpecAttackerIs.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_is_boss", PartialSpecAttackerIsBoss.Serializer.INSTANCE);
		partialSerializers.register("apathy:attacker_tagged_with", PartialSpecAttackerTaggedWith.Serializer.INSTANCE);
		partialSerializers.register("apathy:difficulty_is", PartialSpecDifficultyIs.Serializer.INSTANCE);
		partialSerializers.register("apathy:in_player_set", PartialSpecDefenderInPlayerSet.Serializer.INSTANCE);
		partialSerializers.register("apathy:location", PartialSpecLocation.Serializer.INSTANCE);
		partialSerializers.register("apathy:not", PartialSpecNot.Serializer.INSTANCE);
		partialSerializers.register("apathy:revenge_timer", PartialSpecRevengeTimer.Serializer.INSTANCE);
		partialSerializers.register("apathy:score", PartialSpecScore.Serializer.INSTANCE);
		
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
	
	public PartialSpec<?> readPartial(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject json)) throw new IllegalArgumentException("Not json object");
		
		String type = json.getAsJsonPrimitive("type").getAsString();
		PartialSerializer<?> pee = partialSerializers.get(type);
		return (PartialSpec<?>) pee.read(json); //TODO actually unchecked
	}
	
	public JsonObject writePartial(PartialSpec<?> part) {
		JsonObject ok = new JsonObject();
		ok.addProperty("type", partialSerializers.getName(part.getSerializer()));
		part.getSerializer().writeErased(part, ok);
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
