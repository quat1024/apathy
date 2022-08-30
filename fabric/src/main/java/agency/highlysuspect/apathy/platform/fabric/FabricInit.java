package agency.highlysuspect.apathy.platform.fabric;

import agency.highlysuspect.apathy.Apathy;
import net.fabricmc.api.ModInitializer;

public class FabricInit implements ModInitializer {
	@Override
	public void onInitialize() {
		Apathy.INSTANCE = new Apathy(new FabricPlatformSupport());
		Apathy.INSTANCE.init();
	}
}
