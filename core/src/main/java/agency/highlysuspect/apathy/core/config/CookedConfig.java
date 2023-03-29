package agency.highlysuspect.apathy.core.config;

public interface CookedConfig {
	<T> T get(ConfigProperty<T> key);
	boolean refresh();
}
