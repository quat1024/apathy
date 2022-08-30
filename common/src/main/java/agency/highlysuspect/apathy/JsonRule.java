package agency.highlysuspect.apathy;

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
	public static Rule jsonRule;
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static void loadJson(Path configFolder) {
		Path mobsJson = configFolder.resolve("mobs.json");
		
		if(!Files.exists(mobsJson)) {
			jsonRule = null;
			return;
		}
		
		String stuff;
		try {
			stuff = Files.lines(mobsJson).collect(Collectors.joining("\n"));
		} catch (IOException e) {
			Apathy.LOG.error("Problem loading json rule at " + mobsJson, e);
			return;
		}
		
		JsonElement json;
		try {
			json = GSON.fromJson(stuff, JsonElement.class);
		} catch (JsonParseException e) {
			Apathy.LOG.error("Problem parsing json rule at " + mobsJson, e);
			return;
		}
		
		DataResult<RuleSpec> ruleSpecResult = Specs.RULE_SPEC_CODEC.parse(JsonOps.INSTANCE, json);
		if(ruleSpecResult.error().isPresent()) {
			Apathy.LOG.error("Problem decoding json rule: " + ruleSpecResult.error().get().message());
			return;
		}
		
		RuleSpec spec = ruleSpecResult.getOrThrow(false, Apathy.LOG::error);
		
		try {
			if(Apathy.INSTANCE.generalConfig.debugJsonRule) spec.dump(configFolder, "json-rule");
			
			if(Apathy.INSTANCE.generalConfig.runRuleOptimizer) {
				spec = spec.optimize();
				if(Apathy.INSTANCE.generalConfig.debugJsonRule) spec.dump(configFolder, "json-rule-opt");
			}
			
			jsonRule = spec.build();
		} catch (Exception e) {
			Apathy.LOG.error("Problem finalizing rule", e);
		}
	}
}
