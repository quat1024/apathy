package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.newconfig.ConfigProperty;
import agency.highlysuspect.apathy.core.newconfig.ConfigSchema;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
			schema.section("Optimization", recheckInterval, runRuleOptimizer);
			schema.section("Wow even more misc options", zombieAttackVillagerDifficulties);
			schema.section("Revenge Spread", angryPiggies, sameTypeRevengeSpread, differentTypeRevengeSpread);
			schema.section("Debug", debugBuiltinRule, debugJsonRule);
		}
	}
	
	public static class Mobs {
		public static final ConfigProperty<Integer> configVersion = ConfigProperty.intOpt("configVersion", 0).build();
		
		public static final ConfigProperty<Boolean> nuclearOption = ConfigProperty.boolOpt("nuclearOption", false,
			"If set to 'true', no mob will ever attack anyone.",
			"Use this option if you don't want to deal with the rest of the config file."
		).build();
		
		public static final ConfigProperty<List<String>> ruleOrder = ConfigProperty.stringListOpt("ruleOrder",
			Arrays.asList("json", "difficulty", "boss", "mobSet", "tagSet", "playerSet", "revenge"),
			"Which order should the rules in this config file be evaluated in?",
			"Comma-separated list built out of any or all of the following keywords, in any order:",
			"json, difficulty, boss, mobSet, tagSet, playerSet, revenge")
			.note("If a rule is not listed in the rule order, it will not be checked.")
			.example("difficulty, revenge, playerSet")
			.build();
		
		public static final ConfigProperty<Set<ApathyDifficulty>> difficultySet = ConfigProperty.difficultySetOpt("difficultySet", Collections.emptySet(),
			"Comma-separated list of difficulties.")
			.example("easy, normal")
			.build();
		
		public static final ConfigProperty<TriState> difficultySetIncluded = ConfigProperty.allowDenyPassOpt("difficultySetIncluded", TriState.DEFAULT,
			"What happens when the current world difficulty appears in difficultySet?",
			"May be one of:",
			"allow - Every mob is always allowed to attack everyone.",
			"deny  - No mob is ever allowed to attack anyone.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<TriState> difficultySetExcluded = ConfigProperty.allowDenyPassOpt("difficultySetExcluded", TriState.DEFAULT,
			"What happens when the current world difficulty does *not* appear in difficultySet?",
			"May be one of:",
			"allow - Every mob is always allowed to attack everyone.",
			"deny  - No mob is ever allowed to attack anyone.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<TriState> boss = ConfigProperty.allowDenyPassOpt("boss", TriState.TRUE,
			"What happens when the attacker is a boss?",
			"'Bossness' is defined by inclusion in the 'apathy:bosses' tag.",
			"May be one of:",
			"allow - Every boss is allowed to attack everyone.",
			"deny  - No boss is allowed to attack anyone.",
			"pass  - Defer to the next rule.")
			.note("If the current attacker is *not* a boss, always passes to the next rule.")
			.build();
		
		//TODO: mobSet (needs EntityType<?>)
		
		public static final ConfigProperty<TriState> mobSetIncluded = ConfigProperty.allowDenyPassOpt("mobSetIncluded", TriState.DEFAULT,
			"What happens when the attacker's entity ID appears in mobSet?",
			"May be one of:",
			"allow - The mob will be allowed to attack the player.",
			"deny  - The mob will not be allowed to attack the player.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<TriState> mobSetExcluded = ConfigProperty.allowDenyPassOpt("mobSetExcluded", TriState.DEFAULT,
			"What happens when the attacker's entity ID does *not* appear in mobSet?",
			"May be one of:",
			"allow - The mob will be allowed to attack the player.",
			"deny  - The mob will not be allowed to attack the player.",
			"pass  - Defer to the next rule."
		).build();
		
		//TODO: tagSet (needs TagKey<EntityType<?>>)
		
		public static final ConfigProperty<TriState> tagSetIncluded = ConfigProperty.allowDenyPassOpt("tagSetIncluded", TriState.DEFAULT,
			"What happens when the attacker is tagged with one of the tags in mobTagSet?",
			"May be one of:",
			"allow - The mob will be allowed to attack the player.",
			"deny  - The mob will not be allowed to attack the player.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<TriState> tagSetExcluded = ConfigProperty.allowDenyPassOpt("tagSetExcluded", TriState.DEFAULT,
			"What happens when the attacker is *not* tagged with one of the tags in mobTagSet?",
			"May be one of:",
			"allow - The mob will be allowed to attack the player.",
			"deny  - The mob will not be allowed to attack the player.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<Optional<String>> playerSetName = ConfigProperty.optionalStringOpt("playerSetName", Optional.of("no-mobs"),
			"The name of a set of players.",
			"If this option is not provided, a player set is not created, and this whole rule always passes."
		).build();
		
		public static final ConfigProperty<Boolean> playerSetSelfSelect = ConfigProperty.boolOpt("playerSetSelfSelect", true,
			"If 'true', players can add themselves to the set, using '/apathy set join <playerListName>'.",
			"If 'false', only an operator can add them to the set, using '/apathy set-admin join <selector> <playerListName>'."
		).build();
		
		public static final ConfigProperty<TriState> playerSetIncluded = ConfigProperty.allowDenyPassOpt("playerSetIncluded", TriState.FALSE,
			"What happens when a mob tries to attack someone who appears in the playerSet?",
			"May be one of:",
			"allow - The mob is allowed to attack the player.",
			"deny  - The mob is not allowed to attack the player.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<TriState> playerSetExcluded = ConfigProperty.allowDenyPassOpt("playerSetExcluded", TriState.DEFAULT,
			"What happens when a mob tries to attack someone who does *not* appear in the playerSet?",
			"May be one of:",
			"allow - The mob is allowed to attack the player.",
			"deny  - The mob is not allowed to attack the player.",
			"pass  - Defer to the next rule."
		).build();
		
		public static final ConfigProperty<Long> revengeTimer = ConfigProperty.longOpt("revengeTimer", -1,
			"For how many ticks is a mob allowed to retaliate after being attacked?",
			"Set to -1 to disable this 'revenge' mechanic.",
			"When the timer expires, defers to the next rule.")
			.note(
				"The exact duration of the attack may be up to (<revengeTimer> + <recheckInterval>) ticks.",
				"Btw, the original mod had an option for 'eternal revenge', with an uncapped timer.",
				"I didn't port that, but the maximum value of the timer is " + Long.MAX_VALUE + " ticks.",
				"Make of that information what you will ;)"
			)
			.atLeast(-1)
			.build();
		
		public static final ConfigProperty<Boolean> fallthrough = ConfigProperty.boolAllowDenyOpt("fallthrough", true,
			"If absolutely none of the previous rules applied, what happens?",
			"May be one of:",
			"allow - By default, mobs are allowed to attack players.",
			"deny  - By default, mobs are not allowed to attack players.",
			"May *not* be set to 'pass'."
		).build();
		
		public static void visit(ConfigSchema schema) {
			schema.option(configVersion);
			
			schema.section("Nuclear Option", nuclearOption);
			schema.section("Built In Rule Order", ruleOrder);
			schema.section("Difficulty Rule", difficultySet, difficultySetIncluded, difficultySetExcluded);
			schema.section("Boss Rule", boss);
			schema.section("Mob Set Rule", mobSetIncluded, mobSetExcluded); //TODO
			schema.section("Tag Set Rule", tagSetIncluded, tagSetExcluded); //TODO
			schema.section("Player Set Rule", playerSetName, playerSetSelfSelect, playerSetIncluded, playerSetExcluded);
			schema.section("Revenge Rule", revengeTimer);
			schema.section("Last Resort Rule", fallthrough);
		}
	}
}
