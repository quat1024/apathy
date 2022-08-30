package agency.highlysuspect.apathy.platform;

import java.nio.file.Path;

public interface PlatformSupport {
	void installConfigFileReloader();
	void installCommandRegistrationCallback();
	void installPlayerSetManagerTicker();
	
	Path getConfigPath();
}
