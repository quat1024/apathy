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
		"Set to 'true' to completely remove the Ender Dragon fight sequence from the game.",
		"When you first visit the End, the exit portal will already be open."
	})
	public boolean noDragon;
}
