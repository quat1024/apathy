package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy;
import agency.highlysuspect.apathy.platform.PlatformSupport;
import net.minecraftforge.fml.common.Mod;

@Mod("apathy")
public class ForgeInit {
	public ForgeInit() {
		PlatformSupport.instance = new ForgePlatformSupport();
		Apathy.init();
	}
}
