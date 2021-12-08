package agency.highlysuspect.apathy.platform;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.function.Consumer;

public abstract class PlatformSupport {
	public static PlatformSupport INSTANCE;
	
	public abstract Path getConfigDir();
	
	public abstract void installResourceReloader(String name, Consumer<ResourceManager> reloader);
	public void installResourceReloader0(String name, Runnable reloader) {
		installResourceReloader(name, (ignored) -> reloader.run());
	}
	public abstract void runResourceReloaders(MinecraftServer server);
	
	public abstract void registerProvocationDetector();
}
