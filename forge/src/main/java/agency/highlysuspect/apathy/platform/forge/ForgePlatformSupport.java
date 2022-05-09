package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.DragonDuck;
import agency.highlysuspect.apathy.MobExt;
import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import net.minecraft.commands.Commands;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

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
					return CompletableFuture.runAsync(() -> {}, backgroundExecutor)
						.thenCompose(stage::wait)
						.thenRunAsync(Apathy::loadConfig, gameExecutor);
				}
			});
		});
	}
	
	@Override
	public void installAttackCallback() {
		MinecraftForge.EVENT_BUS.addListener((AttackEntityEvent e) -> Apathy.onPoke(e.getPlayer().level, e.getPlayer(), e.getTarget()));
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
			ApathyCommands.registerCommands(e.getDispatcher(), e.getEnvironment() == Commands.CommandSelection.DEDICATED);
		});
	}
	
	@Override
	public void installPlayerSetManagerTicker() {
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent e) -> {
			if(e.phase == TickEvent.Phase.START) {
				//Imagine having access to the server, that is ticking, in something called "Server Tick Event".
				//What a FUCKIng concept!
				PlayerSetManager.getFor(ServerLifecycleHooks.getCurrentServer()).syncWithConfig();
			}
		});
	}
	
	@Override
	public Path getConfigPath() {
		//TODO should really use an actual forge config
		return FMLPaths.CONFIGDIR.get().resolve(Apathy.MODID);
	}
}
