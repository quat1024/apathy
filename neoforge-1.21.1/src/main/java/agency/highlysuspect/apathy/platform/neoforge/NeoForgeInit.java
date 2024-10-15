package agency.highlysuspect.apathy.platform.neoforge;

import agency.highlysuspect.apathy.Apathy121;
import agency.highlysuspect.apathy.core.config.ConfigSchema;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyCommands;
import agency.highlysuspect.apathy.coreplusminecraft.ApathyPlusMinecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod("apathy")
public class NeoForgeInit extends Apathy121 {
	public NeoForgeInit(IEventBus bus) {
		generalForgeSpec = new ModConfigSpec.Builder();
		mobsForgeSpec = new ModConfigSpec.Builder();
		bossForgeSpec = new ModConfigSpec.Builder();
		
		init(); //this calls xxxConfigBakery().cook(), which populates the forge spec
		
		generalForge = generalForgeSpec.build();
		mobsForge = mobsForgeSpec.build();
		bossForge = bossForgeSpec.build();
		
		//XXX: i like the idea of forge's server-only, per-world configs, but it's not commonly understood how to make them not per-world.
		//if i used per-world configs, i think i'd just get 50 comments of the form "where's the config file?" or "oh, i can't configure
		//the mod in the same place as my other mods? that's annoying". sorry forge
		ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, generalForge, "apathy-general.toml");
		ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, mobsForge, "apathy-mobs.toml");
		ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, bossForge, "apathy-boss.toml");
	}
	
	private final ModConfigSpec.Builder generalForgeSpec, mobsForgeSpec, bossForgeSpec;
	private final ModConfigSpec generalForge, mobsForge, bossForge;
	
	@SuppressWarnings("Convert2Lambda")
	@Override
	public void installConfigFileReloader() {
		//when running /reload:
		NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new PreparableReloadListener() {
			@Override
			public CompletableFuture<Void> reload(final PreparationBarrier stage, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
				return CompletableFuture.runAsync(() -> {}, backgroundExecutor)
					.thenCompose(stage::wait)
					.thenRunAsync(NeoForgeInit.this::refreshConfig, gameExecutor);
			}
		}));
		
		//when loading forge configs:
		ModLoadingContext.get().getActiveContainer().getEventBus().addListener((ModConfigEvent.Loading e) -> {
			//todo; in practice refreshMobConfig has a dependency on refreshGeneralConfig (because that's where the dump options are stored)
			if(e.getConfig().getSpec() == generalForge) refreshGeneralConfig();
			else if(e.getConfig().getSpec() == mobsForge) refreshMobConfig();
			else if(e.getConfig().getSpec() == bossForge) refreshBossConfig();
		});
		
		//when reloading forge configs:
		ModLoadingContext.get().getActiveContainer().getEventBus().addListener((ModConfigEvent.Reloading e) -> {
			if(e.getConfig().getSpec() == generalForge) refreshGeneralConfig();
			else if(e.getConfig().getSpec() == mobsForge) refreshMobConfig();
			else if(e.getConfig().getSpec() == bossForge) refreshBossConfig();
		});
	}
	
	@Override
	public void installCommandRegistrationCallback() {
		NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> ApathyCommands.registerCommands(e.getDispatcher()));
	}
	
	@Override
	public void installPlayerSetManagerTicker() {
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Pre e) -> {
			//Imagine having access to the server, that is ticking, in something called "Server Tick Event".
			//What a FUCKIng concept!
			ApathyPlusMinecraft.instanceMinecraft.getFor(ServerLifecycleHooks.getCurrentServer()).syncWithConfig();
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
	
	@Override
	public Path mobsJsonPath() {
		return FMLPaths.CONFIGDIR.get().resolve("apathy-mobs.json");
	}
	
	@Override
	public Path dumpsDirPath() {
		return FMLPaths.GAMEDIR.get().resolve("apathy-dumps");
	}
}
