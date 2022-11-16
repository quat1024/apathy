package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.spec.RuleSpec;
import agency.highlysuspect.apathy.rule.spec.Specs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class JsonRule {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static Rule loadJson(Path mobsJson) throws IOException, JsonParseException {
		if(!Files.exists(mobsJson)) {
			return null;
		}
		
		String stuff;
		try {
			stuff = Files.lines(mobsJson).collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.addSuppressed(new RuntimeException("Problem loading json rule at " + mobsJson));
			throw e;
		}
		
		JsonElement json;
		try {
			json = GSON.fromJson(stuff, JsonElement.class);
		} catch (JsonParseException e) {
			e.addSuppressed(new RuntimeException("Problem parsing json rule at " + mobsJson));
			throw e;
		}
		
		DataResult<RuleSpec<?>> ruleSpecResult = Specs.RULE_SPEC_CODEC.parse(JsonOps.INSTANCE, json);
		if(ruleSpecResult.error().isPresent()) {
			throw new RuntimeException("Problem decoding json rule: " + ruleSpecResult.error().get().message());
		}
		
		RuleSpec<?> spec = ruleSpecResult.getOrThrow(false, ApathyHell.instance.log::error);
		
		try {
			if(Apathy119.instance119.generalConfig.debugJsonRule) spec.dump(ApathyHell.instance.configPath, "json-rule");
			
			if(Apathy119.instance119.generalConfig.runRuleOptimizer) {
				spec = spec.optimize();
				if(Apathy119.instance119.generalConfig.debugJsonRule) spec.dump(ApathyHell.instance.configPath, "json-rule-opt");
			}
			
			return spec.build();
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException("Problem finalizing rule"));
			throw e;
		}
	}
}
