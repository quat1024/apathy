package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.config.CookedConfig;
import agency.highlysuspect.apathy.core.rule.JsonSerializer;
import agency.highlysuspect.apathy.core.rule.Partial;
import agency.highlysuspect.apathy.core.rule.PartialSpecAll;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.PartialSpecAny;
import agency.highlysuspect.apathy.core.rule.PartialSpecAttackerIs;
import agency.highlysuspect.apathy.core.rule.PartialSpecAttackerIsBoss;
import agency.highlysuspect.apathy.core.rule.PartialSpecAttackerTaggedWith;
import agency.highlysuspect.apathy.core.rule.PartialSpecDifficultyIs;
import agency.highlysuspect.apathy.core.rule.PartialSpecNot;
import agency.highlysuspect.apathy.core.rule.PartialSpecRandom;
import agency.highlysuspect.apathy.core.rule.PartialSpecRevengeTimer;
import agency.highlysuspect.apathy.core.rule.PartialSpecSpawnType;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.core.rule.RuleSpecChain;
import agency.highlysuspect.apathy.core.rule.RuleSpecDebug;
import agency.highlysuspect.apathy.core.rule.RuleSpecDifficultyCase;
import agency.highlysuspect.apathy.core.rule.RuleSpecIf;
import agency.highlysuspect.apathy.core.rule.RuleSpecJson;
import agency.highlysuspect.apathy.core.rule.RuleSpecPredicated;
import agency.highlysuspect.apathy.core.rule.Spec;
import agency.highlysuspect.apathy.core.wrapper.Attacker;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;
import agency.highlysuspect.apathy.core.wrapper.Defender;
import agency.highlysuspect.apathy.core.wrapper.LogFacade;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * how much of Apathy is it possible to port while not touching minecraft at all? Surprisingly, a lot, i guess
 */
public abstract class Apathy {
	public static final String MODID = "apathy";
	
	/** constructor sets this as a side effect */
	public static Apathy instance;
	public final LogFacade log;
	
	public final NotRegistry<JsonSerializer<? extends Spec<Rule, ?>>> ruleSerializers = new NotRegistry<>();
	public final NotRegistry<JsonSerializer<? extends Spec<Partial, ?>>> partialSerializers = new NotRegistry<>();
	
	public CookedConfig generalCfg = CookedConfig.Unset.INSTANCE;
	public CookedConfig mobCfg = CookedConfig.Unset.INSTANCE;
	public CookedConfig bossCfg = CookedConfig.Unset.INSTANCE;
	
	public Rule configuredRule = RuleSpecAlways.ALWAYS_ALLOW.build();
	public @Nullable Rule jsonRule;
	
	public Apathy(LogFacade log) {
		if(instance == null) {
			instance = this;
		} else {
			IllegalStateException e = new IllegalStateException("Apathy instantiated twice!");
			log.error("Apathy instantiated twice!", e);
			throw e;
		}
		
		this.log = log;
	}
	
	public void init() {
		//rule setup
		addRules();
		
		//config
		ConfigSchema generalConfigSchema = new ConfigSchema();
		addGeneralConfig(generalConfigSchema);
		generalCfg = generalConfigBakery().cook(generalConfigSchema);
		
		ConfigSchema mobsConfigSchema = new ConfigSchema();
		addMobConfig(mobsConfigSchema);
		mobCfg = mobsConfigBakery().cook(mobsConfigSchema);
		
		ConfigSchema bossConfigSchema = new ConfigSchema();
		addBossConfig(bossConfigSchema);
		bossCfg = bossConfigBakery().cook(bossConfigSchema);
		
		//misc
		installConfigFileReloader();
		installCommandRegistrationCallback();
		installPlayerSetManagerTicker();
	}
	
	public boolean allowedToTargetPlayer(Attacker attacker, Defender defender) {
		TriState result = configuredRule.apply(attacker, defender);
		if(result != TriState.DEFAULT) return result.get();
		else return mobCfg.get(CoreMobOptions.fallthrough);
	}
	
	//TODO: rethink error handling here (keep the old config around)
	public boolean refreshGeneralConfig() {
		return generalCfg.refresh();
	}
	
	public boolean refreshMobConfig() {
		boolean ok = mobCfg.refresh();
		
		Rule newConfiguredRule = configuredRule;
		try {
			newConfiguredRule = bakeMobsConfigRule();
		} catch (Exception e) {
			log.error("Problem baking rule: ", e);
			ok = false;
		} finally {
			configuredRule = newConfiguredRule;
		}
		
		Rule newJsonRule = jsonRule;
		try {
			newJsonRule = JsonRule.loadJson(mobsJsonPath());
		} catch (Exception e) {
			log.error("Problem reading mobs.json: ", e);
			ok = false;
		} finally {
			jsonRule = newJsonRule;
		}
		
		return ok;
	}
	
	public boolean refreshBossConfig() {
		return bossCfg.refresh();
	}
	
	public boolean refreshConfig() {
		boolean ok = refreshGeneralConfig();
		ok &= refreshMobConfig();
		ok &= refreshBossConfig();
		
		return ok;
	}
	
	public void addRules() {
		ruleSerializers.register("always", RuleSpecAlways.Serializer.INSTANCE);
		ruleSerializers.register("chain", RuleSpecChain.Serializer.INSTANCE);
		ruleSerializers.register("debug", RuleSpecDebug.Serializer.INSTANCE);
		ruleSerializers.register("difficulty_case", RuleSpecDifficultyCase.Serializer.INSTANCE);
		ruleSerializers.register("evaluate_json_file", RuleSpecJson.Serializer.INSTANCE);
		ruleSerializers.register("if", RuleSpecIf.Serializer.INSTANCE);
		ruleSerializers.register("predicated", RuleSpecPredicated.Serializer.INSTANCE);
		
		partialSerializers.register("all", PartialSpecAll.Serializer.INSTANCE);
		partialSerializers.register("always", PartialSpecAlways.Serializer.INSTANCE);
		partialSerializers.register("any", PartialSpecAny.Serializer.INSTANCE);
		partialSerializers.register("attacker_is", PartialSpecAttackerIs.Serializer.INSTANCE);
		partialSerializers.register("attacker_is_boss", PartialSpecAttackerIsBoss.Serializer.INSTANCE);
		partialSerializers.register("attacker_tagged_with", PartialSpecAttackerTaggedWith.Serializer.INSTANCE);
		partialSerializers.register("difficulty_is", PartialSpecDifficultyIs.Serializer.INSTANCE);
		partialSerializers.register("not", PartialSpecNot.Serializer.INSTANCE);
		partialSerializers.register("random", PartialSpecRandom.Serializer.INSTANCE);
		partialSerializers.register("revenge_timer", PartialSpecRevengeTimer.Serializer.INSTANCE);
		partialSerializers.register("spawn_type", PartialSpecSpawnType.Serializer.INSTANCE);
		
		//backwards compatibilty/deprecated stuff (allow_if is a subset of predicated's functionality)
		ruleSerializers.register("allow_if", RuleSpecPredicated.LegacyAllowIfSerializer.INSTANCE);
		ruleSerializers.register("deny_if", RuleSpecPredicated.LegacyDenyIfSerializer.INSTANCE);
	}
	
	public void addGeneralConfig(ConfigSchema schema) {
		CoreGenOptions.visit(schema);
	}
	
	public void addMobConfig(ConfigSchema schema) {
		CoreMobOptions.visit(schema);
	}
	
	public void addBossConfig(ConfigSchema schema) {
		CoreBossOptions.visit(schema);
	}
	
	public abstract ConfigSchema.Bakery generalConfigBakery();
	public abstract ConfigSchema.Bakery mobsConfigBakery();
	public abstract ConfigSchema.Bakery bossConfigBakery();
	public abstract Path mobsJsonPath();
	public abstract Path dumpsDirPath();
	
	public abstract Rule bakeMobsConfigRule();
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
	
	public abstract @Nullable AttackerType parseAttackerType(String s);
	public abstract @Nullable AttackerTag parseAttackerTag(String s);
	
	//TODO maybe find a better home for these 4 methods?
	public Spec<Rule, ?> readRule(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject)) {
			throw new IllegalArgumentException("Expected json object, found " + (jsonElem == null ? "nothing" : jsonElem.getClass().getSimpleName()));
		}
		JsonObject json = (JsonObject) jsonElem;
		
		JsonPrimitive typeField = json.getAsJsonPrimitive("type");
		if(typeField == null) {
			throw new IllegalArgumentException("Expected json object to have field 'type', but didn't find any");
		}
		String type = typeField.getAsString();
		
		JsonSerializer<? extends Spec<Rule, ?>> jsonSerializer = ruleSerializers.get(type);
		if(jsonSerializer == null) {
			StringBuilder message = new StringBuilder("No rule serializer with name '").append(type).append("'.");
			
			//better error for putting a partial where a rule was expected
			if(partialSerializers.names().contains(type)) {
				message.append("\n   (Apathy does have something named '").append(type).append("', but it's a predicate, not a rule.)");
			}
			
			message.append("\n   Valid rules are '");
			message.append(ruleSerializers.names().stream().sorted().collect(Collectors.joining("', '")));
			message.append("'.");
			
			throw new IllegalArgumentException(message.toString());
		} else {
			try {
				return jsonSerializer.read(json);
			} catch (Exception e) {
				throw JsonRule.context(e, "Problem decoding '" + type + "' rule:");
			}
		}
	}
	
	public <T extends Spec<Rule, T>> JsonObject writeRule(Spec<Rule, T> rule) {
		JsonObject ok = new JsonObject();
		JsonSerializer<T> serializer = rule.getSerializer();
		
		String name = ruleSerializers.getName(serializer);
		if(name == null) throw new IllegalArgumentException("internal error, unregistered rule serializer for " + serializer.getClass().getName());
		
		ok.addProperty("type", name);
		serializer.writeErased(rule, ok);
		return ok;
	}
	
	public Spec<Partial, ?> readPartial(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject)) {
			throw new IllegalArgumentException("Expected json object, found " + (jsonElem == null ? "nothing" : jsonElem.getClass().getSimpleName()));
		}
		JsonObject json = (JsonObject) jsonElem;
		
		JsonPrimitive typeField = json.getAsJsonPrimitive("type");
		if(typeField == null) {
			throw new IllegalArgumentException("Expected json object to have field 'type', but didn't find any");
		}
		String type = typeField.getAsString();
		
		JsonSerializer<? extends Spec<Partial, ?>> jsonSerializer = partialSerializers.get(type);
		if(jsonSerializer == null) {
			StringBuilder message = new StringBuilder("No predicate serializer with name '").append(type).append("'.");
			
			//better error for putting a rule where a partial was expected
			if(ruleSerializers.names().contains(type)) {
				message.append("\n  (Apathy does have something named '").append(type).append("', but it's a rule, not a predicate.)");
			}
			
			message.append("\n   Valid predicates are '");
			message.append(partialSerializers.names().stream().sorted().collect(Collectors.joining("', '")));
			message.append("'.");
			
			throw new IllegalArgumentException(message.toString());
		} else {
			try {
				return jsonSerializer.read(json);
			} catch (Exception e) {
				throw JsonRule.context(e, "Problem decoding '" + type + "' predicate:");
			}
		}
	}
	
	public <T extends Spec<Partial, T>> JsonObject writePartial(Spec<Partial, T> part) {
		JsonObject ok = new JsonObject();
		JsonSerializer<T> serializer = part.getSerializer();
		
		String name = partialSerializers.getName(serializer);
		if(name == null) throw new IllegalArgumentException("internal error, unregistered partial serializer for " + serializer.getClass().getName());
		
		ok.addProperty("type", name);
		serializer.writeErased(part, ok);
		return ok;
	}
}
