package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;

import java.nio.file.Path;

public class FabricPlatformSupport extends PlatformSupport {
	@Override
	public void installConfigFileReloader() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return Apathy.id("reload-config");
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				Apathy.loadConfig();
			}
		});
	}
	
	@Override
	public void installAttackCallback() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!world.isClientSide && entity instanceof MobExt ext) {
				ext.apathy$provokeNow();
			}
			
			return InteractionResult.PASS;
		});
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		//TODO: Will need verifying that it's the same shape on Forge
		CommandRegistrationCallback.EVENT.register(ApathyCommands::registerCommands);
	}
	
	@Override
	public void installPlayerSetManagerUpkeepTicker() {
		//TODO: This kind of sucks, and could do with some cleaning up
		
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			PlayerSetManager mgr = PlayerSetManager.getFor(server);
			Apathy.mobConfig.playerSetName.ifPresent(s -> {
				if(mgr.hasSet(s)) mgr.get(s).setSelfSelect(Apathy.mobConfig.playerSetSelfSelect);
				else mgr.createSet(s, Apathy.mobConfig.playerSetSelfSelect);
			});
		});
	}
	
	@Override
	public Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(Apathy.MODID);
	}
	
	@Override
	public boolean externalApathyReloadSupported() {
		return true;
	}
	
	@Override
	public Tag.Named<EntityType<?>> entityTypeTag(ResourceLocation id) {
		//TODO: Not really sure what this is about
		return TagFactory.ENTITY_TYPE.create(id);
	}
}
