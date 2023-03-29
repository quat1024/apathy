package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.etc.DragonInitialStateN;
import agency.highlysuspect.apathy.core.etc.ElderGuardianEffectN;
import agency.highlysuspect.apathy.core.etc.PortalInitialStateN;
import agency.highlysuspect.apathy.core.etc.ResummonSequenceN;
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
	
	public static class Boss {
		public static final ConfigProperty<Integer> configVersion = ConfigProperty.intOpt("configVersion", 0).build();
		
		public static final ConfigProperty<DragonInitialStateN> dragonInitialState = ConfigProperty.enumOpt("dragonInitialState", DragonInitialStateN.DEFAULT,
				"What is the initial state of the Ender Dragon in the End?",
				"If 'default', she will be present and attack players, just like the vanilla game.",
				"If 'passive_dragon', she will be present, but idly noodle about until a player provokes her first.",
				"If 'calm', the End will not contain an Ender Dragon by default.")
			.note("If you choose 'calm', you should also change the 'portalInitialState' setting, so it is possible to leave the End.")
			.build();
		
		public static final ConfigProperty<PortalInitialStateN> portalInitialState = ConfigProperty.enumOpt("portalInitialState", PortalInitialStateN.CLOSED,
				"What is the initial state of the End Portal in the center of the main End Island?",
				"If 'closed', it will not be usable until the first Ender Dragon dies, just like in vanilla.",
				"If 'open', it will already be open.",
				"If 'open_with_egg', it will already be open and a Dragon Egg will be present.")
			.build();
		
		public static final ConfigProperty<Integer> initialEndGatewayCount = ConfigProperty.intOpt("initialEndGatewayCount", 0,
			"How many End Gateways will be available when first entering the End, without any Dragons having to die?"
		).atLeast(0).atMost(20).build();
		
		public static final ConfigProperty<ResummonSequenceN> resummonSequence = ConfigProperty.enumOpt("resummonSequence", ResummonSequenceN.DEFAULT,
				"What happens when a player places four End Crystals onto the exit End Portal?",
				"If 'default', a new Ender Dragon will be summoned and she will attack the player, just like in vanilla.",
				"If 'spawn_gateway', the mechanic will be replaced with one that directly creates an End Gateway, with no fighting required.",
				"If 'disabled', nothing will happen.")
			.build();
		
		public static final ConfigProperty<Boolean> simulacraDragonAdvancements = ConfigProperty.boolOpt("simulacraDragonAdvancements", true,
			"If 'true', and 'dragonInitialState' is 'calm', players automatically earn the Free the End advancement when visiting the End.",
			"If 'true', and 'resummonSequence' is 'spawn_gateway', players earn the advancement for resummoning the Dragon when using the spawn_gateway mechanic.",
			"Has no effects otherwise. Probably should be left as 'true'."
		).build();
		
		public static final ConfigProperty<Boolean> dragonFlies = ConfigProperty.boolOpt("dragonFlies", true,
			"Can the Dragon perform the 'strafe_player' or 'charging_player' actions?",
			"strafe_player is the one where she shoots a fireball.",
			"charge_player is the one where she tries to fly into you.",
			"If 'false', she will perform the 'landing_approach' action instead, which will cause her to perch on the portal."
		).build();
		
		public static final ConfigProperty<Boolean> dragonSits = ConfigProperty.boolOpt("dragonSits", true,
			"Can the Dragon perform the 'sitting_flaming' or 'sitting_attacking' actions?",
			"sitting_flaming is the one where she perches on the End portal and pours out a puddle of dragon's breath.",
			"sitting_attacking is when she roars at you.",
			"If 'false', she will perform the 'sitting_scanning' action instead, which will soon lead to her leaving her perch."
		).build();
		
		public static final ConfigProperty<Boolean> dragonDamage = ConfigProperty.boolOpt("dragonDamage", true,
			"Does the Dragon deal contact damage?"
		).build();
		
		public static final ConfigProperty<Boolean> dragonKnockback = ConfigProperty.boolOpt("dragonKnockback", true,
			"Does the Dragon knock back nearby entities, and damage them while she's sitting?"
		).build();
		
		public static final ConfigProperty<Set<ApathyDifficulty>> witherDifficulties = ConfigProperty.difficultySetOpt("witherDifficulties", ApathyDifficulty.allDifficultiesNotPeaceful(),
				"Comma-separated list of difficulties where the Wither is enabled.",
				"If the current world difficulty does not appear in the set, building the Wither formation will spawn a Nether Star",
				"item, and give you the advancement for killing the Wither.")
			.build();
		
		public static final ConfigProperty<Boolean> witherTargetsPlayers = ConfigProperty.boolOpt("witherTargetsPlayers", true,
			"Is the Wither allowed to intentionally target players?"
		).build();
		
		public static final ConfigProperty<Boolean> witherTargetsMobs = ConfigProperty.boolOpt("witherTargetsMobs", true,
			"Is the Wither allowed to intentionally target non-players?"
		).build();
		
		public static final ConfigProperty<Boolean> blackWitherSkulls = ConfigProperty.boolOpt("blackWitherSkulls", true,
			"Can the Wither fire black wither skulls?"
		).build();
		
		public static final ConfigProperty<Boolean> blueWitherSkulls = ConfigProperty.boolOpt("blueWitherSkulls", true,
			"Can the Wither fire blue ('charged') wither skulls on Normal and Hard difficulty?"
		).build();
		
		public static final ConfigProperty<Boolean> witherBreaksBlocks = ConfigProperty.boolOpt("witherBreaksBlocks", true,
			"Does the Wither break nearby blocks after it gets damaged?"
		).build();
		
		public static final ConfigProperty<ElderGuardianEffectN> elderGuardianEffect = ConfigProperty.enumOpt("elderGuardianEffect", ElderGuardianEffectN.DEFAULT,
			"This option affects your own client.",
			"What happens when an Elder Guardian gives you the Mining Fatigue effect?",
			"If 'default', the sound effect and particle appear.",
			"If 'only_sound', only the sound plays, and if 'only_particle', only the particle effect appears.",
			"If 'disabled', neither of those happen."
		).build();
		
		public static void visit(ConfigSchema schema) {
			schema.option(configVersion);
			
			schema.section("Ender Dragon", dragonInitialState, portalInitialState, initialEndGatewayCount, resummonSequence, simulacraDragonAdvancements, dragonFlies, dragonSits, dragonDamage, dragonKnockback);
			schema.section("Wither", witherDifficulties, witherTargetsPlayers, witherTargetsMobs, blackWitherSkulls, blueWitherSkulls, witherBreaksBlocks);
			schema.section("Elder Guardian", elderGuardianEffect);
		}
	}
}
