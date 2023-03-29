package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy118;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.config.CrummyConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class FabricInit extends Apathy118 implements ModInitializer {
	public FabricInit() {
		super(FabricLoader.getInstance().getConfigDir().resolve(MODID));
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
				loadConfig();
			}
		});
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ApathyCommands.registerCommands(dispatcher));
	}
	
	@Override
	public void installPlayerSetManagerTicker() {
		ServerTickEvents.START_SERVER_TICK.register(server -> PlayerSetManager.getFor(server).syncWithConfig());
	}
	
	@Override
	public ConfigSchema.Bakery generalConfigBakery() {
		return new CrummyConfig.Bakery(configPath.resolve("general.cfg"));
	}
	
	@Override
	public ConfigSchema.Bakery mobsConfigBakery() {
		return new CrummyConfig.Bakery(configPath.resolve("mobs.cfg"));
	}
	
	@Override
	public ConfigSchema.Bakery bossConfigBakery() {
		return new CrummyConfig.Bakery(configPath.resolve("boss.cfg"));
	}
}
