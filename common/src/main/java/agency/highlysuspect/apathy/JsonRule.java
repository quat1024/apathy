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
	
	public static final Path MOBS_JSON = Apathy.INSTANCE.configFolder.resolve("mobs.json");
	public static final Path DUMP_DIR = Apathy.INSTANCE.configFolder.resolve("dumps");
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static void loadJson() {
		if(!Files.exists(MOBS_JSON)) {
			jsonRule = null;
			return;
		}
		
		String stuff;
		try {
			stuff = Files.lines(MOBS_JSON).collect(Collectors.joining("\n"));
		} catch (IOException e) {
			Apathy.LOG.error("Problem loading json rule at " + MOBS_JSON, e);
			return;
		}
		
		JsonElement json;
		try {
			json = GSON.fromJson(stuff, JsonElement.class);
		} catch (JsonParseException e) {
			Apathy.LOG.error("Problem parsing json rule at " + MOBS_JSON, e);
			return;
		}
		
		DataResult<RuleSpec> ruleSpecResult = Specs.RULE_SPEC_CODEC.parse(JsonOps.INSTANCE, json);
		if(ruleSpecResult.error().isPresent()) {
			Apathy.LOG.error("Problem decoding json rule: " + ruleSpecResult.error().get().message());
			return;
		}
		
		RuleSpec spec = ruleSpecResult.getOrThrow(false, Apathy.LOG::error);
		
		try {
			if(Apathy.INSTANCE.generalConfig.debugJsonRule) dumpSpec("json-rule", spec);
			
			if(Apathy.INSTANCE.generalConfig.runRuleOptimizer) {
				spec = spec.optimize();
				if(Apathy.INSTANCE.generalConfig.debugJsonRule) dumpSpec("json-rule-opt", spec);
			}
			
			jsonRule = spec.build();
		} catch (Exception e) {
			Apathy.LOG.error("Problem finalizing rule", e);
		}
	}
	
	public static void dumpSpec(String filename, RuleSpec spec) {
		try {
			Files.createDirectories(DUMP_DIR);
			Path outPath = DUMP_DIR.resolve(filename + ".json");
			Apathy.LOG.info("Dumping rule to " + outPath);
			
			DataResult<JsonElement> jsonResult = Specs.RULE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, spec);
			JsonElement json = jsonResult.getOrThrow(false, Apathy.LOG::error);
			
			Files.writeString(outPath, GSON.toJson(json));
		} catch (Exception e) {
			Apathy.LOG.error("Problem dumping rule to " + filename, e);
		}
	}
}
