package agency.highlysuspect.apathy;

import clojure.java.api.Clojure;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class Init implements ModInitializer {
	public static final String MODID = "apathy";
	public static final Logger LOG = LogManager.getLogger(MODID);
	
	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
	
	@Override
	public void onInitialize() {
		//Load the little bootstrap script
		loadIntoClojure("Initializing Clojure library and loading built-in script (might take a hot minute)", Init.class.getClassLoader().getResourceAsStream("apathy-startup.clj"));
		
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Init.id("load_clojure");
			}
			
			@Override
			public void apply(ResourceManager manager) {
				List<Resource> resources;
				try {
					resources = manager.getAllResources(Init.id("on-resource-load.clj"));
				} catch (IOException e) {
					LOG.error("Problem loading list of Clojure scripts", e);
					return;
				}
				
				resources.forEach(resource -> loadIntoClojure("Loading Clojure script from " + resource.getId().toString(), resource.getInputStream()));
			}
		});
	}
	
	private static void loadIntoClojure(String message, InputStream yea) {
		try(InputStreamReader reader = new InputStreamReader(yea)) {
			LOG.info(message);
			Clojure.var("clojure.core", "load-reader").invoke(reader);
			LOG.info("Success!");
		} catch (RuntimeException | IOException e) {
			LOG.error("Failure.");
			LOG.error("Problem loading clojure file", e);
			throw new RuntimeException(e);
		}
	}
}
