package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.config.annotation.Comment;
import agency.highlysuspect.apathy.config.annotation.NoDefault;
import agency.highlysuspect.apathy.config.annotation.Section;

public class BossConfig extends Config {
	protected static int CURRENT_CONFIG_VERSION = 0;
	@NoDefault protected int configVersion = CURRENT_CONFIG_VERSION;
	
	////////////////////////
	@Section("Ender Dragon")
	////////////////////////
	
	@Comment({
		"Set to 'true' to remove the Ender Dragon fight sequence entirely.",
		"When you first visit the End, the exit portal will already be open, complete with egg.",
		"You will earn the 'Free the End' advancement automatically.",
		"Placing four End Crystals on the exit portal will generate an End Gateway and grant the advancement for respawning the dragon."
	})
	public boolean noDragon;
	
	@Comment({
		"Can the Dragon perform the 'strafe_player' or 'charging_player' actions?",
		"strafe_player is the one where she shoots a fireball.",
		"charge_player is the one where she tries to fly into you.",
		"If 'false', she will perform the 'landing_approach' action instead, which will cause her to perch on the portal.",
		"(NOT TESTED - I CANNOT TEST THE DRAGON FIGHT)"
	})
	public boolean dragonFlies = true;
	
	@Comment({
		"Can the Dragon perform the 'sitting_flaming' or 'sitting_attacking' actions?",
		"sitting_flaming is the one where she perches on the End portal and pours out a puddle of dragon's breath.",
		"sitting_attacking is when she roars at you.",
		"If 'false', she will perform the 'sitting_scanning' action instead, which will soon lead to her leaving her perch.",
		"(NOT TESTED - I CANNOT TEST THE DRAGON FIGHT)"
	})
	public boolean dragonSits = true;
	
	//////////////////
	@Section("Wither")
	//////////////////
	
	@Comment({
		"Set to 'true' to remove the Wither fight sequence.",
		"Building the Wither formation will spawn a Nether Star item and give you the advancement for killing the Wither."
	})
	public boolean noWither;
	
	@Comment("Is the Wither allowed to intentionally target players?")
	public boolean witherTargetsPlayers = true;
	
	@Comment("Is the Wither allowed to intentionally target non-players?")
	public boolean witherTargetsMobs = true;
	
	@Comment("Can the Wither fire black wither skulls?")
	public boolean blackWitherSkulls = true;
	
	@Comment("Can the Wither fire blue ('charged') wither skulls on Normal and Hard difficulty?")
	public boolean blueWitherSkulls = true;
	
	@Comment("Does the Wither break nearby blocks after it gets damaged?")
	public boolean witherBreaksBlocks = true;
}
