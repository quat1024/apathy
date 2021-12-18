package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import net.fabricmc.api.ModInitializer;

public class FabricInit implements ModInitializer {
	@Override
	public void onInitialize() {
		PlatformSupport.instance = new FabricPlatformSupport();
		Apathy.init();
	}
}
