package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.rule.Rule;
import agency.highlysuspect.apathy.core.rule.Spec;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class JsonRule {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static @Nullable Rule loadJson(Path mobsJson) throws Exception {
		if(!Files.exists(mobsJson)) return null;
		
		//read it into string
		String stuff;
		try {
			stuff = Files.lines(mobsJson).collect(Collectors.joining("\n"));
		} catch (Exception e) {
			throw context(e, "Problem loading json rule at " + mobsJson + ":");
		}
		
		//parse the string as unstructured json
		JsonElement json;
		try {
			json = GSON.fromJson(stuff, JsonElement.class);
		} catch (Exception e) {
			throw context(e, "Problem parsing json rule at " + mobsJson + ":");
		}
		
		//parse the json into a java object
		Spec<Rule, ?> spec;
		try {
			spec = Apathy.instance.readRule(json);
		} catch (Exception e) {
			throw context(e, "Problem decoding json rule:");
		}
		
		//realize the rulespec into a rule
		try {
			boolean debug = Apathy.instance.generalCfg.get(CoreGenOptions.debugJsonRule);
			boolean opt = Apathy.instance.generalCfg.get(CoreGenOptions.runRuleOptimizer);
			
			if(debug) dump(spec, "json-rule");
			
			if(opt) {
				spec = spec.optimize();
				if(debug) dump(spec, "json-rule-opt");
			}
			
			return spec.build();
		} catch (Exception e) {
			throw context(e, "Problem finalizing rule:");
		}
	}
	
	public static <T extends Throwable> RuntimeException context(T in, String message) {
		RuntimeException ctx = new RuntimeException(message, in);
		ctx.setStackTrace(new StackTraceElement[0]);
		return ctx;
	}
	
	/**
	 * Write a rule as json and poop it out.
	 */
	public static <RULE extends Spec<Rule, RULE>> void dump(Spec<Rule, RULE> ruleSpec, String filename) {
		try {
			Path dumpDir = Apathy.instance.dumpsDirPath();
			
			Files.createDirectories(dumpDir);
			
			Path outPath = dumpDir.resolve(filename + ".json");
			Apathy.instance.log.info("Dumping rule to " + outPath);
			JsonObject json = Apathy.instance.writeRule(ruleSpec);
			Files.write(outPath, GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			Apathy.instance.log.error("Problem dumping rule to " + filename, e);
		}
	}
}
