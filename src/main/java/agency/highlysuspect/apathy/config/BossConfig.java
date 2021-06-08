package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.config.annotation.Comment;
import agency.highlysuspect.apathy.config.annotation.NoDefault;
import agency.highlysuspect.apathy.config.annotation.Section;

import java.io.IOException;
import java.nio.file.Path;

public class BossConfig extends Config {
	protected static int CURRENT_CONFIG_VERSION = 0;
	
	protected BossConfig() {
		super();
	}
	
	public BossConfig(Path configFilePath) throws IOException {
		super(configFilePath);
	}
	
	@Override
	protected Config defaultConfig() {
		return new BossConfig();
	}
	
	@NoDefault protected int configVersion = CURRENT_CONFIG_VERSION;
	
	////////////////////////
	@Section("Ender Dragon")
	////////////////////////
	
	@Comment({
		"Set to 'true' to remove the Ender Dragon fight sequence.",
		"When you first visit the End, the exit portal will already be open, with an egg.",
		"Placing four End Crystals on the exit portal will generate an End Gateway."
	})
	public boolean noDragon;
	
	//////////////////
	@Section("Wither")
	//////////////////
	
	@Comment({
		"Set to 'true' to remove the Wither fight sequence.",
		"Building the Wither formation will simply spawn a Nether Star item."
	})
	public boolean noWither;
}
