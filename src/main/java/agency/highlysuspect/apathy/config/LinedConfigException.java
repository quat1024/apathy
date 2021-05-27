package agency.highlysuspect.apathy.config;

//This exception is always a rethrow of another exception, my own stacktrace is noise in this case.
class LinedConfigException extends RuntimeException {
	public LinedConfigException(String message, Throwable cause) {
		super(message, cause);
		setStackTrace(null);
	}
}
