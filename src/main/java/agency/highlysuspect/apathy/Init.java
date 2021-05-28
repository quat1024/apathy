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
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class Init implements ModInitializer {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	public static final String CONFIG_FILENAME = Init.MODID + ".cfg";
	
	public static Config config;
	public static ClojureProxy clojureProxy = ClojureProxy.NO_CLOJURE;
	public static boolean clojureLoaded = false;
	
	@Override
	public void onInitialize() {
		clojureLoaded = FabricLoader.getInstance().isModLoaded("clojurelib");
		
		Commands.registerArgumentTypes();
		CommandRegistrationCallback.EVENT.register(Commands::registerCommands);
		
		VengeanceHandler.onInitialize();
		
		installAndRunReloadListener("reload-config", () -> {
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
	
	//quick wrapper, so i don't need a giant anonymous class for every reload listener.
	public static void installReloadListener(String name, Consumer<ResourceManager> funny) {
		Identifier id = id(name);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return id;
			}
			
			@Override
			public void apply(ResourceManager manager) {
				funny.accept(manager);
			}
		});
	}
	
	public static void installAndRunReloadListener(String name, Runnable funny) {
		//run now
		funny.run();
		//and on resource reload.
		installReloadListener(name, (ignoredManager) -> funny.run());
		//Can't be a Consumer<ResourceManager> since I don't have one right now
	}
}
