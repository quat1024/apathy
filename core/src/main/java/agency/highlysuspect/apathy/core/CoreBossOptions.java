package agency.highlysuspect.apathy.core;

import agency.highlysuspect.apathy.core.config.ConfigProperty;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.etc.DragonInitialState;
import agency.highlysuspect.apathy.core.etc.ElderGuardianEffect;
import agency.highlysuspect.apathy.core.etc.PortalInitialState;
import agency.highlysuspect.apathy.core.etc.ResummonSequence;
import agency.highlysuspect.apathy.core.wrapper.ApathyDifficulty;

import java.util.Set;

public class CoreBossOptions {
	public static final ConfigProperty<Integer> configVersion = ConfigProperty.intOpt("configVersion", 0).build();
	
	public static final ConfigProperty<DragonInitialState> dragonInitialState = ConfigProperty.enumOpt("dragonInitialState", DragonInitialState.DEFAULT,
			"What is the initial state of the Ender Dragon in the End?",
			"If 'default', she will be present and attack players, just like the vanilla game.",
			"If 'passive_dragon', she will be present, but idly noodle about until a player provokes her first.",
			"If 'calm', the End will not contain an Ender Dragon by default.")
		.note("If you choose 'calm', you should also change the 'portalInitialState' setting, so it is possible to leave the End.")
		.build();
	
	public static final ConfigProperty<PortalInitialState> portalInitialState = ConfigProperty.enumOpt("portalInitialState", PortalInitialState.CLOSED,
			"What is the initial state of the End Portal in the center of the main End Island?",
			"If 'closed', it will not be usable until the first Ender Dragon dies, just like in vanilla.",
			"If 'open', it will already be open.",
			"If 'open_with_egg', it will already be open and a Dragon Egg will be present.")
		.build();
	
	public static final ConfigProperty<Integer> initialEndGatewayCount = ConfigProperty.intOpt("initialEndGatewayCount", 0,
		"How many End Gateways will be available when first entering the End, without any Dragons having to die?"
	).atLeast(0).atMost(20).build();
	
	public static final ConfigProperty<ResummonSequence> resummonSequence = ConfigProperty.enumOpt("resummonSequence", ResummonSequence.DEFAULT,
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
	
	public static final ConfigProperty<ElderGuardianEffect> elderGuardianEffect = ConfigProperty.enumOpt("elderGuardianEffect", ElderGuardianEffect.DEFAULT,
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
