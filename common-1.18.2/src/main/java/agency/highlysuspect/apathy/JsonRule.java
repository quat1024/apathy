package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.ApathyHell;
import agency.highlysuspect.apathy.core.CoreOptions;
import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.RuleSpec;
import agency.highlysuspect.apathy.core.rule.SerializableRuleSpec;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

//TODO: Move to core, currently blocked on dumping logic which requires the config to be moved first
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
			spec = ApathyHell.instance.readRule(json);
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException("Problem decoding json rule"));
			throw e;
		}
		
		//realize the rulespec into a rule
		try {
			boolean debug = ApathyHell.instance.generalConfigCooked.get(CoreOptions.General.debugJsonRule);
			boolean opt = ApathyHell.instance.generalConfigCooked.get(CoreOptions.General.runRuleOptimizer);
			
			if(debug) dump(spec, ApathyHell.instance.configPath, "json-rule");
			
			if(opt) {
				spec = spec.optimize();
				if(debug) dump(spec, ApathyHell.instance.configPath, "json-rule-opt");
			}
			
			return spec.build();
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException("Problem finalizing rule"));
			throw e;
		}
	}
	
	/**
	 * Write a rule as json and poop it out.
	 */
	public static <RULE extends SerializableRuleSpec<RULE>> void dump(RuleSpec<RULE> ruleSpec, Path configFolder, String filename) {
		try {
			Path dumpDir = configFolder.resolve("dumps");
			
			Files.createDirectories(dumpDir);
			
			Path outPath = dumpDir.resolve(filename + ".json");
			ApathyHell.instance.log.info("Dumping rule to " + outPath);
			JsonObject json = ApathyHell.instance.writeRule(ruleSpec);
			Files.writeString(outPath, GSON.toJson(json));
		} catch (Exception e) {
			ApathyHell.instance.log.error("Problem dumping rule to " + filename, e);
		}
	}
}
