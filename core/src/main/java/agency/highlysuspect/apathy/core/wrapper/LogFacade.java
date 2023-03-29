package agency.highlysuspect.apathy.core.wrapper;

public interface LogFacade {
	void info(String message, Object... args);
	void warn(String message, Object... args);
	void error(String message, Object... args);
}
