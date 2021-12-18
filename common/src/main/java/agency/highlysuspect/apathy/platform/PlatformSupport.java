package agency.highlysuspect.apathy.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;

import java.nio.file.Path;

public abstract class PlatformSupport {
	public static PlatformSupport instance;
	
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
