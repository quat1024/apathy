package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.core.LogFacade;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("ClassCanBeRecord")
public class Log4jLoggingFacade implements LogFacade {
	public Log4jLoggingFacade(Logger LOG) {
		this.LOG = LOG;
	}
	
	private final Logger LOG;
	
	@Override
	public void info(String message, Object... args) {
		LOG.info(message, args);
	}
	
	@Override
	public void warn(String message, Object... args) {
		LOG.warn(message, args);
	}
	
	@Override
	public void error(String message, Object... args) {
		LOG.error(message, args);
	}
}
