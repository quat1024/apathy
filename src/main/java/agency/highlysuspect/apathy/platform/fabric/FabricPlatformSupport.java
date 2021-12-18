package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;

import java.nio.file.Path;

public class FabricPlatformSupport extends PlatformSupport {
	@Override
	public Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(Apathy.MODID);
	}
	
	@Override
	public void reloadConfigFileOnResourceReload() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return Apathy.id("reload-config");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				Apathy.reloadConfigFile();
			}
		});
	}
	
	@Override
	public void installAttackCallback() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!world.isClientSide && entity instanceof MobExt) {
				((MobExt) entity).apathy$provokeNow();
			}
			
			return InteractionResult.PASS;
		});
	}
}
