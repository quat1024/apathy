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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class JsonRule {
	public static Rule jsonRule;
	
	public static final Path MOBS_JSON = Init.CONFIG_FOLDER.resolve("mobs.json");
	public static final Path DUMP_DIR = Init.CONFIG_FOLDER.resolve("dumps");
	
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
			Init.LOG.error("Problem loading json rule at " + MOBS_JSON, e);
			return;
		}
		
		JsonElement json;
		try {
			json = GSON.fromJson(stuff, JsonElement.class);
		} catch (JsonParseException e) {
			Init.LOG.error("Problem parsing json rule at " + MOBS_JSON, e);
			return;
		}
		
		DataResult<RuleSpec> ruleSpecResult = Specs.RULE_SPEC_CODEC.parse(JsonOps.INSTANCE, json);
		if(ruleSpecResult.error().isPresent()) {
			Init.LOG.error("Problem decoding json rule: " + ruleSpecResult.error().get().message());
			return;
		}
		
		RuleSpec spec = ruleSpecResult.getOrThrow(false, Init.LOG::error);
		
		try {
			if(Init.generalConfig.debugJsonRule) dumpSpec("json-rule", spec);
			
			if(Init.generalConfig.runRuleOptimizer) {
				spec = spec.optimize();
				if(Init.generalConfig.debugJsonRule) dumpSpec("json-rule-opt", spec);
			}
			
			jsonRule = spec.build();
		} catch (Exception e) {
			Init.LOG.error("Problem finalizing rule", e);
		}
	}
	
	public static void dumpSpec(String filename, RuleSpec spec) {
		try {
			Files.createDirectories(DUMP_DIR);
			Path outPath = DUMP_DIR.resolve(filename + ".json");
			Init.LOG.info("Dumping rule to " + outPath);
			
			DataResult<JsonElement> jsonResult = Specs.RULE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, spec);
			JsonElement json = jsonResult.getOrThrow(false, Init.LOG::error);
			
			Files.writeString(outPath, GSON.toJson(json));
		} catch (Exception e) {
			Init.LOG.error("Problem dumping rule to " + filename, e);
		}
	}
}
