package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.clojure.ClojureProxy;
import agency.highlysuspect.apathy.clojure.ClojureSupport;
import agency.highlysuspect.apathy.config.Config;
import agency.highlysuspect.apathy.revenge.VengeanceHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Init implements ModInitializer {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	public static final String CONFIG_FILENAME = Init.MODID + ".cfg";
	
	public static Config config;
	public static ClojureProxy clojureProxy = ClojureProxy.NO_CLOJURE;
	public static boolean clojureLoaded = false;
	
	//Keeping these in a separate list so the /apathy reload command can get to it. (Not implemented yet)
	public static List<Consumer<ResourceManager>> reloaders = new ArrayList<>();
	
	@Override
	public void onInitialize() {
		clojureLoaded = FabricLoader.getInstance().isModLoaded("clojurelib");
		
		Commands.registerArgumentTypes();
		CommandRegistrationCallback.EVENT.register(Commands::registerCommands);
		
		VengeanceHandler.onInitialize();
		
		installReloadAndRunNow("reload-config", () -> {
			try {
				config = Config.fromPath(FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILENAME));
			} catch (Exception e) {
				throw new RuntimeException("Problem loading config file.", e);
			}
		});
		
		if(config.useClojure) {
			if(clojureLoaded) {
				LOG.info("Clojure is enabled in config and ClojureLib is present. Enabling Clojure support");
				ClojureSupport.onInitialize();
			} else {
				LOG.error("Clojure support is enabled in config, but ClojureLib is not present. Please install ClojureLib to make use of the Clojure API.");
			}
		}
	}
	
	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
	
	public static void installReload(String name, Consumer<ResourceManager> funny) {
		reloaders.add(funny);
		
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return id(name);
			}
			
			@Override
			public void apply(ResourceManager manager) {
				funny.accept(manager);
			}
		});
	}
	
	public static void installReloadAndRunNow(String name, Runnable funny) {
		//run now
		funny.run();
		//and on resource reload.
		installReload(name, (ignoredManager) -> funny.run());
	}
}
