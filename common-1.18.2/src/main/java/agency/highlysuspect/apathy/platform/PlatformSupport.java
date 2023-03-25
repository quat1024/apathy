package agency.highlysuspect.apathy.platform;

import agency.highlysuspect.apathy.Apathy;

import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public abstract class PlatformSupport {
	public static final PlatformSupport instance;
	
	static {
		//Cribbed a bit from from Botania here.
		List<ServiceLoader.Provider<PlatformSupport>> providers = ServiceLoader.load(PlatformSupport.class).stream().toList();
		if(providers.size() != 1) {
			throw new IllegalStateException("There should be one PlatformSupport on the classpath, but I found these: " + providers.stream().map(p -> p.type().getName()).collect(Collectors.joining(",")));
		} else {
			ServiceLoader.Provider<PlatformSupport> provider = providers.get(0);
			Apathy.LOG.info("Found PlatformSupport: " + provider.type().getName());
			instance = provider.get();
		}
	}
	
	public void initialize() {
		installConfigFileReloader();
		installCommandRegistrationCallback();
		installPlayerSetManagerTicker();
	}
	
	public abstract void installConfigFileReloader();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerTicker();
	
	public abstract Path getConfigPath();
}
