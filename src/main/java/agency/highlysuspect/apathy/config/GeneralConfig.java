package agency.highlysuspect.apathy.config;

import agency.highlysuspect.apathy.config.annotation.AtLeast;
import agency.highlysuspect.apathy.config.annotation.Comment;
import agency.highlysuspect.apathy.config.annotation.NoDefault;
import agency.highlysuspect.apathy.config.annotation.Section;

import java.io.IOException;
import java.nio.file.Path;

public class GeneralConfig extends Config {
	protected static int CURRENT_CONFIG_VERSION = 0;
	
	protected GeneralConfig() {}
	
	public GeneralConfig(Path configFilePath) throws IOException {
		super(configFilePath);
	}
	
	@Override
	protected Config defaultConfig() {
		return new GeneralConfig();
	}
	
	@NoDefault protected int configVersion = CURRENT_CONFIG_VERSION;
	
	///////////////////
	@Section("Clojure")
	///////////////////
	
	@Comment({
		"Enable the Clojure API for configuring the mod. See the README on github for more information."
	})
	public boolean useClojure = false; //False by default. Sorry Eutro.
	
	////////////////////////
	@Section("Optimization")
	////////////////////////
	
	@Comment({
		"By default, mobs that are currently attacking a player do not check every tick if it's still okay to do so.",
		"This is how often the mob will check. (Set this to 1 to check every tick.)"
	})
	@AtLeast(minInt = 1)
	public int recheckInterval = 20;
}
