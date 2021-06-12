package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.config.annotation.AtLeast;
import agency.highlysuspect.apathy.config.annotation.Comment;
import agency.highlysuspect.apathy.config.annotation.NoDefault;
import agency.highlysuspect.apathy.config.annotation.Section;

import java.io.IOException;
import java.nio.file.Path;

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
