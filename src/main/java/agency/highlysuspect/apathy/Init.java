package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.config.BossConfig;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.config.GeneralConfig;
import agency.highlysuspect.apathy.config.MobConfig;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import agency.highlysuspect.apathy.mixin.MinecraftServerAccessor;
import agency.highlysuspect.apathy.rule.spec.Specs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Init implements ModInitializer {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	public static final Path CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(MODID);
	
	public static MobConfig mobConfig;
	public static GeneralConfig generalConfig;
	public static BossConfig bossConfig;
	
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}
	
	@Override
	public void onInitialize() {
		//Ensure the config subdirectory exists and things can be placed inside it
		try {
			Files.createDirectories(CONFIG_FOLDER);
		} catch (IOException e) {
			throw new RuntimeException("Problem creating config/apathy/ subdirectory", e);
		}
		
		//Various subsystems
		Specs.onInitialize();
		
		Commands.onInitialize();
		PlayerSetManager.onInitialize();
		
		//Config file stuff
		installReloadAndRunNow("reload-config", () -> {
			GeneralConfig oldGeneralConfig = generalConfig;
			MobConfig oldMobConfig = mobConfig;
			BossConfig oldBossConfig = bossConfig;
			
			try {
				generalConfig = Config.read(new GeneralConfig(), CONFIG_FOLDER.resolve("general.cfg"));
				mobConfig = Config.read(new MobConfig(), CONFIG_FOLDER.resolve("mobs.cfg"));
				bossConfig = Config.read(new BossConfig(), CONFIG_FOLDER.resolve("boss.cfg"));
				
				//todo this is kinda tacked on
				JsonRule.loadJson();
			} catch (Exception e) {
				if(oldGeneralConfig == null && oldMobConfig == null && oldBossConfig == null) {
					throw new RuntimeException("Problem initializing config file.", e);
				} else {
					generalConfig = oldGeneralConfig;
					mobConfig = oldMobConfig;
					bossConfig = oldBossConfig;
					LOG.error("Problem reloading config file: ", e);
					LOG.error("The current config has not been changed. Resolve the error, and try loading the config file again.");
				}
			}
		});
		
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!world.isClientSide && entity instanceof MobExt) {
				((MobExt) entity).apathy$provokeNow();
			}
			
			return InteractionResult.PASS;
		});
	}
	
	//Resource reloady bits
	
	private static final List<Consumer<ResourceManager>> reloaders = new ArrayList<>();
	
	public static void installReload(String name, Consumer<ResourceManager> funny) {
		reloaders.add(funny);
		
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return id(name);
			}
			
			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				funny.accept(manager);
			}
		});
	}
	
	public static void installReloadAndRunNow(String name, Runnable funny) {
		installReload(name, (ignoredManager) -> funny.run());
		funny.run();
	}
	
	//called from the `/apathy reload` command, just reruns my own loaders and not the whole game
	public static void reloadNow(MinecraftServer server) {
		ResourceManager rm = ((MinecraftServerAccessor) server).apathy$getServerResources().getResourceManager();
		reloaders.forEach(reloader -> reloader.accept(rm));
	}
}
