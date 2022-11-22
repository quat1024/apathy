package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy119;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.hell.ApathyHell;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class FabricInit extends Apathy119 implements ModInitializer {
	public FabricInit() {
		super(FabricLoader.getInstance().getConfigDir().resolve(ApathyHell.MODID));
	}
	
	@Override
	public void onInitialize() {
		init();
	}
	
	@Override
	public void installConfigFileReloader() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return new ResourceLocation(MODID, "reload-config");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				Apathy119.instance119.loadConfig();
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
}
