package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.newconfig.ConfigProperty;
import agency.highlysuspect.apathy.core.newconfig.ConfigSchema;
import agency.highlysuspect.apathy.core.newconfig.CookedConfig;
import agency.highlysuspect.apathy.core.rule.PartialSerializer;
import agency.highlysuspect.apathy.core.rule.PartialSpec;
import agency.highlysuspect.apathy.core.rule.PartialSpecAll;
import agency.highlysuspect.apathy.core.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.core.rule.PartialSpecAny;
import agency.highlysuspect.apathy.core.rule.PartialSpecNot;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.RuleSerializer;
import agency.highlysuspect.apathy.core.rule.RuleSpec;
import agency.highlysuspect.apathy.core.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.core.rule.RuleSpecChain;
import agency.highlysuspect.apathy.core.rule.RuleSpecDebug;
import agency.highlysuspect.apathy.core.rule.RuleSpecDifficultyCase;
import agency.highlysuspect.apathy.core.rule.RuleSpecJson;
import agency.highlysuspect.apathy.core.rule.RuleSpecPredicated;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class ApathyHell {
	public static final String MODID = "apathy";
	public static ApathyHell instance;
	
	public final Path configPath;
	public final LogFacade log;
	
	public final NotRegistry<RuleSerializer<?>> ruleSerializers = new NotRegistry<>();
	public final NotRegistry<PartialSerializer<?>> partialSerializers = new NotRegistry<>();
	
	private final ConfigSchema generalConfigSchema = new ConfigSchema();
	private final ConfigSchema mobConfigSchema = new ConfigSchema();
	private final ConfigSchema bossConfigSchema = new ConfigSchema();
	
	public CookedConfig generalConfigCooked; //TODO remove -cooked suffix after migrating over, it name-clashes rn
	public CookedConfig mobConfigCooked;
	public CookedConfig bossConfigCooked;
	
	//werid spot for this, idk
	public @Nullable Rule jsonRule;
	
	public ApathyHell(Path configPath, LogFacade log) {
		if(instance == null) {
			instance = this;
		} else {
			log.error("Apathy instantiated twice");
			throw new IllegalStateException("Apathy instantiated twice");
		}
		
		this.configPath = configPath;
		this.log = log;
	}
	
	public void init() {
		try {
			Files.createDirectories(configPath);
		} catch (IOException e) {
			throw new RuntimeException("Problem creating config/apathy/ subdirectory at " + configPath, e);
		}
		
		//Rules
		ruleSerializers.register("apathy:allow_if", RuleSpecPredicated.AllowIfSerializer.INSTANCE);
		ruleSerializers.register("apathy:always", RuleSpecAlways.Serializer.INSTANCE);
		ruleSerializers.register("apathy:chain", RuleSpecChain.Serializer.INSTANCE);
		ruleSerializers.register("apathy:debug", RuleSpecDebug.Serializer.INSTANCE);
		ruleSerializers.register("apathy:deny_if", RuleSpecPredicated.DenyIfSerializer.INSTANCE);
		ruleSerializers.register("apathy:difficulty_case", RuleSpecDifficultyCase.Serializer.INSTANCE);
		ruleSerializers.register("apathy:evaluate_json_file", RuleSpecJson.Serializer.INSTANCE);
		ruleSerializers.register("apathy:predicated", RuleSpecPredicated.PredicatedSerializer.INSTANCE);
		partialSerializers.register("apathy:all", PartialSpecAll.Serializer.INSTANCE);
		partialSerializers.register("apathy:always", PartialSpecAlways.Serializer.INSTANCE);
		partialSerializers.register("apathy:any", PartialSpecAny.Serializer.INSTANCE);
		partialSerializers.register("apathy:not", PartialSpecNot.Serializer.INSTANCE);
		addPlatformSpecificRules();
		
		//Config
		CoreOptions.General.visit(generalConfigSchema);
		addPlatformSpecificGeneralConfig(generalConfigSchema);
		generalConfigCooked = generalConfigBakery().cook(generalConfigSchema);
		
		//Misc
		installConfigFileReloader();
		installCommandRegistrationCallback();
		installPlayerSetManagerTicker();
	}
	
	public void loadConfig_toplevel() { //TODO rename after onboarding everything to new system
		generalConfigCooked.refresh();
	}
	
	//TODO maybe find a better home for these 4 methods?
	public RuleSpec<?> readRule(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject)) throw new IllegalArgumentException("Not json object");
		JsonObject json = (JsonObject) jsonElem; 
		
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
		if(!(jsonElem instanceof JsonObject)) throw new IllegalArgumentException("Not json object");
		JsonObject json = (JsonObject) jsonElem;
		
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
	
	public static <T extends Enum<?>> Set<T> allOf(Class<T> enumClass) {
		Set<T> set = new HashSet<>();
		Collections.addAll(set, enumClass.getEnumConstants());
		return set;
	}
	
	public void addPlatformSpecificGeneralConfig(ConfigSchema generalConfigSchema) { }
	public void addPlatformSpecificMobConfig(ConfigSchema mobConfig) { }
	public void addPlatformSpecificBossConfig(ConfigSchema bossConfig) { }
	
	public abstract ConfigSchema.Bakery generalConfigBakery();
	
	public void addPlatformSpecificRules() { }
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
}
