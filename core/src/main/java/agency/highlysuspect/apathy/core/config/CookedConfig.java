package agency.highlysuspect.apathy.core.config;

import agency.highlysuspect.apathy.core.Apathy;

public interface CookedConfig {
	<T> T get(ConfigProperty<T> key);
	boolean refresh();
	
	class Unset implements CookedConfig {
		public static final Unset INSTANCE = new Unset();
		
		@Override
		public <T> T get(ConfigProperty<T> key) {
			Apathy.instance.log.warn("Config key " + key.name() + " was accessed before loading config. Returning default value.\n" +
				"Probably not what the user wants, but at least it's better than crashing?\n" +
				"Please report what situation this happened in! This shouldn't occur.");
			return key.defaultValue();
		}
		
		@Override
		public boolean refresh() {
			return true;
		}
	}
}
