package agency.highlysuspect.apathy.platform;

import agency.highlysuspect.apathy.Apathy;
import net.fabricmc.api.ModInitializer;

public class FabricInit implements ModInitializer {
	@Override
	public void onInitialize() {
		PlatformSupport.INSTANCE = new FabricPlatform();
		Apathy.setup();
	}
}
