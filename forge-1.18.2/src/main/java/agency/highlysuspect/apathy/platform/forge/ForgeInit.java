package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy118;
import agency.highlysuspect.apathy.ApathyCommands;
import agency.highlysuspect.apathy.PlayerSetManager;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod("apathy")
public class ForgeInit extends Apathy118 {
	public ForgeInit() {
		//TODO should really use a real forge config
		super(FMLPaths.CONFIGDIR.get().resolve(MODID));
		
		LegacyToTomlUpgrader.doIt();
		
		//borrowed from IExtensionPoint javadoc in fmlcore
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				// Ignore this mod if not present on the client
				() -> NetworkConstants.IGNORESERVERONLY,
				// If present on the client, accept any version if from a server
				(remoteVersion, isFromServer) -> true
			)
		);
		
		generalForgeSpec = new ForgeConfigSpec.Builder();
		mobsForgeSpec = new ForgeConfigSpec.Builder();
		bossForgeSpec = new ForgeConfigSpec.Builder();
		
		init(); //this calls xxxConfigBakery().cook(), which populates the forge spec
		
		ForgeConfigSpec generalForge = generalForgeSpec.build();
		ForgeConfigSpec mobsForge = mobsForgeSpec.build();
		ForgeConfigSpec bossForge = bossForgeSpec.build();
		
		//XXX: i like the idea of forge's server-only, per-world configs, but it's not commonly understood how to make them not per-world.
		//if i used per-world configs, i think i'd just get 50 comments of the form "where's the config file?" or "oh, i can't configure
		//the mod in the same place as my other mods? that's annoying". sorry forge
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, generalForge, "apathy-general.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, mobsForge, "apathy-mobs.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, bossForge, "apathy-boss.toml");
	}
	
	private final ForgeConfigSpec.Builder generalForgeSpec, mobsForgeSpec, bossForgeSpec;
	
	@SuppressWarnings("Convert2Lambda")
	@Override
	public void installConfigFileReloader() {
		//when running /reload:
		MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new PreparableReloadListener() {
			@Override
			public CompletableFuture<Void> reload(final PreparationBarrier stage, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
				return CompletableFuture.runAsync(() -> {}, backgroundExecutor)
					.thenCompose(stage::wait)
					.thenRunAsync(ForgeInit.this::refreshConfig, gameExecutor);
			}
		}));
		
		//when loading forge configs:
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Loading e) -> {
			//sortof a kludgy way to double-check that it's one of mine
			if(e.getConfig().getFileName().startsWith("apathy")) refreshConfig();
		});
		
		//when reloading forge configs:
		FMLJavaModLoadingContext.get().getModEventBus().addListener((ModConfigEvent.Reloading e) -> {
			if(e.getConfig().getFileName().startsWith("apathy")) refreshConfig();
		});
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> ApathyCommands.registerCommands(e.getDispatcher()));
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
	public ConfigSchema.Bakery generalConfigBakery() {
		return new ForgeBackedConfig.Bakery(generalForgeSpec);
	}
	
	@Override
	public ConfigSchema.Bakery mobsConfigBakery() {
		return new ForgeBackedConfig.Bakery(mobsForgeSpec);
	}
	
	@Override
	public ConfigSchema.Bakery bossConfigBakery() {
		return new ForgeBackedConfig.Bakery(bossForgeSpec);
	}
}
