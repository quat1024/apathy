package agency.highlysuspect.apathy.platform;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

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
		installAttackCallback();
		installCommandRegistrationCallback();
		installPlayerSetManagerUpkeepTicker();
	}
	
	public abstract void installConfigFileReloader();
	public abstract void installAttackCallback();
	public abstract void installCommandRegistrationCallback();
	public abstract void installPlayerSetManagerUpkeepTicker();
	
	public abstract Path getConfigPath();
	public abstract boolean externalApathyReloadSupported();
	
	public abstract Tag.Named<EntityType<?>> entityTypeTag(ResourceLocation id);
}
