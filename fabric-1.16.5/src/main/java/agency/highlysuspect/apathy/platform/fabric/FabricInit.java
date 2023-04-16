package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy116;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.core.config.CrummyConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class FabricInit extends Apathy116 implements ModInitializer {
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
				refreshConfig();
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
	
	private Path cfgPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(MODID); //inside a subfolder of main config directory
	}
	
	@Override
	public ConfigSchema.Bakery generalConfigBakery() {
		return new CrummyConfig.Bakery(cfgPath().resolve("general.cfg"));
	}
	
	@Override
	public ConfigSchema.Bakery mobsConfigBakery() {
		return new CrummyConfig.Bakery(cfgPath().resolve("mobs.cfg"));
	}
	
	@Override
	public ConfigSchema.Bakery bossConfigBakery() {
		return new CrummyConfig.Bakery(cfgPath().resolve("boss.cfg"));
	}
	
	@Override
	public Path mobsJsonPath() {
		return cfgPath().resolve("mobs.json");
	}
	
	@Override
	public Path dumpsDirPath() {
		return FabricLoader.getInstance().getGameDir().resolve("apathy-dumps");
	}
	
	@Override
	public @Nullable Tag.Named<EntityType<?>> constructTagUsingWeirdAncientMethods(ResourceLocation rl) {
		//TagRegistry.entityType delegates through to TagRegistry.create(), which always returns a tag.named.
		//Not sure why the return type on the more specific methods is erased a bit. Unchecked downcast should be fine?
		return (Tag.Named<EntityType<?>>) TagRegistry.entityType(rl);
	}
}
