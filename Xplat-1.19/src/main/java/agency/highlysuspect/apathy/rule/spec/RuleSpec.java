package agency.highlysuspect.apathy.rule.spec;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.JsonRule;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.rule.Rule;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.nio.file.Files;
import java.nio.file.Path;

public interface RuleSpec {
	default RuleSpec optimize() {
		return this;
	}
	
	Rule build();
	Codec<? extends RuleSpec> codec();
	
	default void dump(Path configFolder, String filename) {
		try {
			Path dumpDir = configFolder.resolve("dumps");
			
			Files.createDirectories(dumpDir);
			Path outPath = dumpDir.resolve(filename + ".json");
			ApathyHell.instance.log.info("Dumping rule to " + outPath);
			
			DataResult<JsonElement> jsonResult = Specs.RULE_SPEC_CODEC.encodeStart(JsonOps.INSTANCE, this);
			JsonElement json = jsonResult.getOrThrow(false, ApathyHell.instance.log::error);
			
			Files.writeString(outPath, JsonRule.GSON.toJson(json));
		} catch (Exception e) {
			ApathyHell.instance.log.error("Problem dumping rule to " + filename, e);
		}
	}
}
