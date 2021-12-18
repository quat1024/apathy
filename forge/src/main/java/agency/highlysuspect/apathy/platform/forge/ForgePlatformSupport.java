package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ForgePlatformSupport extends PlatformSupport {
	@SuppressWarnings("Convert2Lambda")
	@Override
	public void installConfigFileReloader() {
		MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
			event.addListener(new PreparableReloadListener() {
				@Override
				public CompletableFuture<Void> reload(final PreparationBarrier stage, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
					return CompletableFuture.runAsync(Apathy::loadConfig, gameExecutor);
				}
			});
		});
	}
	
	@Override
	public void installAttackCallback() {
		MinecraftForge.EVENT_BUS.addListener((AttackEntityEvent e) -> {
			Level level = e.getTarget().level;
			if(!level.isClientSide() && e.getTarget() instanceof MobExt ext) {
				ext.apathy$provokeNow();
			}
		});
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
			ApathyCommands.registerCommands(e.getDispatcher(), e.getEnvironment() == Commands.CommandSelection.DEDICATED);
		});
	}
	
	@Override
	public void installPlayerSetManagerUpkeepTicker() {
		//TODO
	}
	
	@Override
	public Path getConfigPath() {
		//TODO should really use an actual forge config
		return FMLPaths.CONFIGDIR.get().resolve(Apathy.MODID);
	}
	
	@Override
	public boolean externalApathyReloadSupported() {
		//TODO: True for now until this project uses the real Forge config system
		return true;
	}
	
	@Override
	public Tag.Named<EntityType<?>> entityTypeTag(ResourceLocation id) {
		//Forge extension, I think
		return EntityTypeTags.createOptional(id);
	}
}
