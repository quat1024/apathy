package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.newconfig.ConfigProperty;
import agency.highlysuspect.apathy.core.newconfig.ConfigSchema;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;

import java.util.Set;

public class CoreOptions {
	public static class General {
		public static final ConfigProperty<Integer> configVersion = ConfigProperty.intOpt("configVersion", 0).build();
		
		public static final ConfigProperty<Integer> recheckInterval = ConfigProperty.intOpt("recheckInterval", 20,
			"By default, mobs that are currently attacking a player do not check every tick if it's still okay to do so.",
			"This is how often the mob will check. (Set this to 1 to check every tick.)"
		).atLeast(1).build();
		
		public static final ConfigProperty<Boolean> runRuleOptimizer = ConfigProperty.boolOpt("runRuleOptimizer", true,
			"Should Apathy attempt to optimize the rule in the config file, to do less work per tick?",
			"If your rule is doing something unexpected, or isn't working like it should, try turning this off.",
			"And report it too, since it's definitely a bug, lol."
		).build();
		
		public static final ConfigProperty<Set<ApathyDifficulty>> zombieAttackVillagerDifficulties =
			ConfigProperty.difficultySetOpt("zombieAttackVillagerDifficulties", ApathyDifficulty.allDifficultiesNotPeaceful(), 
				"Comma-separated list of difficulties where zombies are allowed to attack villagers."
			).build();
		
		public static final ConfigProperty<Set<ApathyDifficulty>> angryPiggies =
			ConfigProperty.difficultySetOpt("angryPiggies", ApathyDifficulty.allDifficultiesNotPeaceful(), 
				"Comma-separated list of difficulties.", 
				"If the current world difficulty appears in the set, zombified piglins will alert their friends", 
				"when a player provokes one. This will also spread Apathy's revengeTimer to them."
			).build();
		
		public static final ConfigProperty<Integer> sameTypeRevengeSpread = ConfigProperty.intOpt("sameTypeRevengeSpread", 0,
			"Let's say this option is set to 10, and you attack a zombie. Other zombies within 10 blocks will have their revengeTimer set, too.",
			"This option affects mobs of the same type only.",
			"Please recall that the revengeTimer is an Apathy concept, and Apathy only ever suppresses normal mob AI.",
			"i.e, this won't cause a mob to attack a player that already wasn't going to."
		).atLeast(0).build();
		
		public static final ConfigProperty<Integer> differentTypeRevengeSpread = ConfigProperty.intOpt("differentTypeRevengeSpread", 0,
			"When attacking any mob, other mobs within this range will have their revengeTimer set, too.",
			"This option affects all mobs, whether they have the same type or not.",
			"Please recall that the revengeTimer is an Apathy concept, and Apathy only ever suppresses normal mob AI.",
			"i.e, this won't cause a mob to attack a player that already wasn't going to."
		).atLeast(0).build();
		
		public static final ConfigProperty<Boolean> debugBuiltinRule = ConfigProperty.boolOpt("debugBuiltinRule", false,
			"If 'true', Apathy will dump the rule specified in the config file to the file 'comfig/apathy/dumps/builtin-rule.json'.",
			"It will also dump the automatically optimized rule out to 'config/apathy/dumps/builtin-rule-opt.json'.",
			"Maybe this is handy if you'd like to see what the .json format looks like for a particular config file."
		).build();
		
		public static final ConfigProperty<Boolean> debugJsonRule = ConfigProperty.boolOpt("debugJsonRule", false,
			"If 'true', Apathy will dump the rule specified in mobs.json to the file 'config/apathy/dumps/json-rule.json'.",
			"It will also dump the automatically optimized rule out to 'config/apathy/dumps/json-rule-opt.json'."
		).build();
		
		public static void visit(ConfigSchema schema) {
			schema.option(configVersion);
			
			schema.section("Optimization");
			schema.option(recheckInterval);
			schema.option(runRuleOptimizer);
			
			schema.section("Wow even more misc options"); //why'd i call it that
			schema.option(zombieAttackVillagerDifficulties);
			
			schema.section("Revenge Spread");
			schema.option(angryPiggies);
			schema.option(sameTypeRevengeSpread);
			schema.option(differentTypeRevengeSpread);
			
			schema.section("Debug");
			schema.option(debugBuiltinRule);
			schema.option(debugJsonRule);
		}
	}
}
