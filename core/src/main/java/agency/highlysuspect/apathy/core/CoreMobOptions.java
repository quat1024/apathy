package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.config.ConfigProperty;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;
import agency.highlysuspect.apathy.core.wrapper.AttackerTag;
import agency.highlysuspect.apathy.core.wrapper.AttackerType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CoreMobOptions {
	public static final ConfigProperty<Integer> configVersion = ConfigProperty.intOpt("configVersion", 4).build();
	
	public static final ConfigProperty<Boolean> nuclearOption = ConfigProperty.boolOpt("nuclearOption", false,
		"If set to 'true', no mob will ever attack anyone.",
		"Use this option if you don't want to deal with the rest of the config file."
	).build();
	
	public static final ConfigProperty<List<String>> ruleOrder = ConfigProperty.stringListOpt("ruleOrder",
		Arrays.asList("json", "difficulty", "boss", "mobSet", "tagSet", "potionSet", "playerSet", "spawnType", "revenge"),
		"Which order should the rules in this config file be evaluated in?",
		"Comma-separated list built out of any or all of the following keywords, in any order:",
		"json, difficulty, boss, mobSet, tagSet, playerSet, potionSet, spawnType, revenge"
	).note("If a rule is not listed in the rule order, it will not be checked.")
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
		"pass  - Defer to the next rule."
	).note("If the current attacker is *not* a boss, always passes to the next rule.")
	.build();
	
	public static final ConfigProperty<Set<AttackerType>> mobSet = ConfigProperty.attackerTypeSetOpt("mobSet", Collections.emptySet(), "A comma-separated set of mob IDs.")
		.example("minecraft:creeper, minecraft:spider")
		.build();
	
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
	
	public static final ConfigProperty<Set<AttackerTag>> tagSet = ConfigProperty.attackerTagSetOpt("tagSet", Collections.emptySet(), "A comma-separated set of entity type tags.")
		.example("minecraft:raiders, some_datapack:some_tag")
		.build();
	
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
		"When the timer expires, defers to the next rule."
	).note(
		"The exact duration of the attack may be up to (<revengeTimer> + <recheckInterval>) ticks.",
		"Btw, the original mod had an option for 'eternal revenge', with an uncapped timer.",
		"I didn't port that, but the maximum value of the timer is " + Long.MAX_VALUE + " ticks.",
		"Make of that information what you will ;)"
	)
	.atLeast(-1)
	.build();
	
	public static final ConfigProperty<Set<String>> spawnTypeSet = ConfigProperty.stringSetOpt("spawnTypeSet", new HashSet<>(),
		"Comma-separated list of spawn types.",
		"The options are \"unknown\", \"natural\", \"chunk_generation\", \"spawner\", \"structure\", \"breeding\", \"mob_summoned\", \"jockey\",",
		"\"event\", \"conversion\", \"reinforcement\", \"triggered\", \"bucket\", \"spawn_egg\", \"command\", \"dispenser\", and \"patrol\".",
		"More documentation for exactly what these do is on the wiki."
	).build();
	
	public static final ConfigProperty<TriState> spawnTypeIncluded = ConfigProperty.allowDenyPassOpt("spawnTypeIncluded", TriState.DEFAULT,
		"What happens to mobs spawned with a method included in spawnTypeSet?",
		"May be one of:",
		"allow - The mob is allowed to attack the player.",
		"deny  - The mob is not allowed to attack the player.",
		"pass  - Defer to the next rule."
	).build();
	
	public static final ConfigProperty<TriState> spawnTypeExcluded = ConfigProperty.allowDenyPassOpt("spawnTypeExcluded", TriState.DEFAULT,
		"What happens to mobs that were *not* spawned with a method included in spawnTypeSet?",
		"May be one of:",
		"allow - The mob is allowed to attack the player.",
		"deny  - The mob is not allowed to attack the player.",
		"pass  - Defer to the next rule."
	).build();
	
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
		schema.section("Mob Set Rule", mobSet, mobSetIncluded, mobSetExcluded);
		schema.section("Tag Set Rule", tagSet, tagSetIncluded, tagSetExcluded);
		schema.section("Player Set Rule", playerSetName, playerSetSelfSelect, playerSetIncluded, playerSetExcluded);
		schema.section("Spawn Type Rule", spawnTypeSet, spawnTypeIncluded, spawnTypeExcluded);
		schema.section("Revenge Rule", revengeTimer);
		schema.section("Last Resort Rule", fallthrough);
	}
}
