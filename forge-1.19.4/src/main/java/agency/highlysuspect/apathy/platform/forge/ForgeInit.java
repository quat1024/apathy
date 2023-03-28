package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.PlayerSetManager;
import net.minecraft.commands.Commands;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.file.Path;

@Mod("apathy")
public class ForgeInit extends Apathy {
	public ForgeInit() {
		//borrowed from IExtensionPoint javadoc in fmlcore
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				(remoteVersion, isFromServer) -> true
			)
		);
		
		init();
	}
	
	@SuppressWarnings("CodeBlock2Expr")
	@Override
	public void installConfigFileReloader() {
		MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
			event.addListener(new SimplePreparableReloadListener<>() {
				@Override
				protected Object prepare(ResourceManager mgr, ProfilerFiller prof) {
					return null;
				}
				
				@Override
				protected void apply(Object preparedObject, ResourceManager mgr, ProfilerFiller prof) {
					Apathy.INSTANCE.loadConfig();
				}
			});
		});
	}
	
	@SuppressWarnings("CodeBlock2Expr")
	@Override
	public void installCommandRegistrationCallback() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
			ApathyCommands.registerCommands(e.getDispatcher(), e.getBuildContext(), e.getCommandSelection());
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
