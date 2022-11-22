package agency.highlysuspect.apathy.hell;

import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.hell.rule.PartialSpec;
import agency.highlysuspect.apathy.hell.rule.PartialSpecAll;
import agency.highlysuspect.apathy.hell.rule.PartialSpecAlways;
import agency.highlysuspect.apathy.hell.rule.PartialSpecAny;
import agency.highlysuspect.apathy.hell.rule.PartialSpecNot;
import agency.highlysuspect.apathy.hell.rule.Rule;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.hell.rule.RuleSpec;
import agency.highlysuspect.apathy.hell.rule.RuleSpecAlways;
import agency.highlysuspect.apathy.hell.rule.RuleSpecChain;
import agency.highlysuspect.apathy.hell.rule.RuleSpecDebug;
import agency.highlysuspect.apathy.hell.rule.RuleSpecDifficultyCase;
import agency.highlysuspect.apathy.hell.rule.RuleSpecJson;
import agency.highlysuspect.apathy.hell.rule.RuleSpecPredicated;
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
		
		installConfigFileReloader();
		installCommandRegistrationCallback();
		installPlayerSetManagerTicker();
	}
	
	//TODO maybe find a better home for these 4 methods?
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
	
	//And this
	public static <T extends Enum<?>> Set<T> allOf(Class<T> enumClass) {
		Set<T> set = new HashSet<>();
		Collections.addAll(set, enumClass.getEnumConstants());
		return set;
	}
	
	public void addPlatformSpecificRules() {
		
	}
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
}
