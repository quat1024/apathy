package agency.highlysuspect.apathy.core.newconfig;

public interface CookedConfig {
	<T> T get(ConfigProperty<T> key);
	boolean refresh();
}
