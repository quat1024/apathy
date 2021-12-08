package agency.highlysuspect.apathy.platform;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.MobEntityExt;
import agency.highlysuspect.apathy.mixin.MinecraftServerAccessor;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FabricPlatform extends PlatformSupport {
	private final List<Consumer<ResourceManager>> reloaders = new ArrayList<>();
	
	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir().resolve(Apathy.MODID);
	}
	
	@Override
	public void installResourceReloader(String name, Consumer<ResourceManager> reloader) {
		reloaders.add(reloader);
		
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier(Apathy.MODID, name);
			}
			
			@Override
			public void reload(ResourceManager manager) {
				reloader.accept(manager);
			}
		});
	}
	
	@Override
	public void runResourceReloaders(MinecraftServer server) {
		ResourceManager rm = ((MinecraftServerAccessor) server).apathy$getServerResourceManager().getResourceManager();
		reloaders.forEach(reloader -> reloader.accept(rm));
	}
	
	@Override
	public void registerProvocationDetector() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!world.isClient && entity instanceof MobEntityExt) {
				((MobEntityExt) entity).apathy$provokeNow();
			}
			
			return ActionResult.PASS;
		});
	}
}
