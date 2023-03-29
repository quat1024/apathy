package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.PlayerSetManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Path;

public class FabricInit extends Apathy implements ModInitializer {
	@Override
	public void onInitialize() {
		init();
	}
	
	@Override
	public void installConfigFileReloader() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return id("reload-config");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				loadConfig();
			}
		});
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		CommandRegistrationCallback.EVENT.register(ApathyCommands::registerCommands);
	}
	
	@Override
	public void installPlayerSetManagerTicker() {
		ServerTickEvents.START_SERVER_TICK.register(server -> PlayerSetManager.getFor(server).syncWithConfig());
	}
	
	@Override
	public Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(MODID);
	}
}
