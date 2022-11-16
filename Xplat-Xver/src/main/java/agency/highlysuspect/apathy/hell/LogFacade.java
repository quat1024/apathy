package agency.highlysuspect.apathy.hell;

/**
 * "We have SLF4J at home."
 * Xplatxver interface to the game's logger.
 */
public interface LogFacade {
	void info(String message, Object... args);
	void warn(String message, Object... args);
	void error(String message, Object... args);
}
