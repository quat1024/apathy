package agency.highlysuspect.apathy.core;

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
import agency.highlysuspect.apathy.core.rule.SerializablePartialSpec;
import agency.highlysuspect.apathy.core.rule.SerializableRuleSpec;
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
		
		addRules();
		
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
		RuleSerializer<?> ruleSerializer = ruleSerializers.get(type);
		
		//TODO way better error message (list the valid options?)
		if(ruleSerializer == null) throw new IllegalArgumentException("No rule serializer with name " + type);
		else return (RuleSpec<?>) ruleSerializer.read(json);
	}
	
	public <T extends SerializableRuleSpec<T>> JsonObject writeRule(RuleSpec<T> rule) {
		JsonObject ok = new JsonObject();
		RuleSerializer<T> serializer = rule.getSerializer();
		
		String name = ruleSerializers.getName(serializer);
		//TODO way better error message (list the valid options?)
		if(name == null) throw new IllegalArgumentException("Unregistered rule serializer: " + serializer.getClass().getName());
		
		ok.addProperty("type", name);
		serializer.writeErased(rule, ok);
		return ok;
	}
	
	public PartialSpec<?> readPartial(JsonElement jsonElem) {
		if(!(jsonElem instanceof JsonObject)) throw new IllegalArgumentException("Not json object");
		JsonObject json = (JsonObject) jsonElem;
		
		String type = json.getAsJsonPrimitive("type").getAsString();
		PartialSerializer<?> partialSerializer = partialSerializers.get(type);
		
		//TODO way better error message (list the valid options?)
		if(partialSerializer == null) throw new IllegalArgumentException("No partial serializer with name " + type);
		else return (PartialSpec<?>) partialSerializer.read(json);
	}
	
	public <T extends SerializablePartialSpec<T>> JsonObject writePartial(PartialSpec<T> part) {
		JsonObject ok = new JsonObject();
		PartialSerializer<T> serializer = part.getSerializer();
		
		String name = partialSerializers.getName(serializer);
		//TODO way better error message (list the valid options?)
		if(name == null) throw new IllegalArgumentException("Unregistered partial serializer: " + serializer.getClass().getName());
		
		ok.addProperty("type", name);
		serializer.writeErased(part, ok);
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
	
	public void addRules() {
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
	}
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
}