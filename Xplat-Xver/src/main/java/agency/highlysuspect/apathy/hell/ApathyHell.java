package agency.highlysuspect.apathy.hell;

import agency.highlysuspect.apathy.hell.rule.PartialSerializer;
import agency.highlysuspect.apathy.hell.rule.Rule;
import agency.highlysuspect.apathy.hell.rule.RuleSerializer;
import agency.highlysuspect.apathy.hell.rule.RuleSpec;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
		
		installConfigFileReloader();
		installCommandRegistrationCallback();
		installPlayerSetManagerTicker();
	}
	
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
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
}
