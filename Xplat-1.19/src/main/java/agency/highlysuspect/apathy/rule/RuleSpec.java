package agency.highlysuspect.apathy.rule;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.JsonRule;
import agency.highlysuspect.apathy.hell.ApathyHell;
import agency.highlysuspect.apathy.hell.rule.SerializableRuleSpec;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A "rule spec" is one step removed from a rule. Rules themselves are lightweight single-method interfaces.
 * A rule spec knows a little more about how rules work.
 */
public interface RuleSpec<RULE extends SerializableRuleSpec<RULE>> extends SerializableRuleSpec<RULE> {
	/**
	 * Potentially lower this rulespec into a form that'd be less work to compute when build() is called.
	 * For example, a ChainRuleSpec with one entry can lower into only that entry, removing the wrapping.
	 */
	default RuleSpec<?> optimize() {
		return this;
	}
	
	/**
	 * Realize the actual computable rule.
	 */
	Rule build();
	
	/**
	 * Write the rule as json and poop it out.
	 */
	default void dump(Path configFolder, String filename) {
		try {
			Path dumpDir = configFolder.resolve("dumps");
			
			Files.createDirectories(dumpDir);
			
			Path outPath = dumpDir.resolve(filename + ".json");
			ApathyHell.instance.log.info("Dumping rule to " + outPath);
			JsonObject json = Apathy119.instance119.writeRule(this);
			Files.writeString(outPath, JsonRule.GSON.toJson(json));
		} catch (Exception e) {
			ApathyHell.instance.log.error("Problem dumping rule to " + filename, e);
		}
	}
}
