package agency.highlysuspect.apathy.platform;

import java.nio.file.Path;

public abstract class PlatformSupport {
	public static PlatformSupport instance;
	
	public abstract Path getConfigPath();
	public abstract void reloadConfigFileOnResourceReload();
	public abstract void installAttackCallback();
}
