package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.rule.Rule;
import agency.highlysuspect.apathy.rule.RuleSpec;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

//this is just a static class but idk where else i should really put it tbh
public class JsonRule {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static @Nullable Rule loadJson(Path mobsJson) throws IOException, JsonParseException {
		if(!Files.exists(mobsJson)) {
			return null;
		}
		
		//read it into string
		String stuff;
		try {
			stuff = Files.lines(mobsJson).collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.addSuppressed(new RuntimeException("Problem loading json rule at " + mobsJson));
			throw e;
		}
		
		//parse the string as unstructured json
		JsonElement json;
		try {
			json = GSON.fromJson(stuff, JsonElement.class);
		} catch (JsonParseException e) {
			e.addSuppressed(new RuntimeException("Problem parsing json rule at " + mobsJson));
			throw e;
		}
		
		//parse the json into a java object
		RuleSpec<?> spec;
		try {
			spec = Apathy119.instance119.readRule(json);
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException("Problem decoding json rule"));
			throw e;
		}
		
		//realize the rulespec into a rule
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
