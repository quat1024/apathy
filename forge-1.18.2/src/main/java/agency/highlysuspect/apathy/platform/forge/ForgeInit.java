package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod("apathy")
public class ForgeInit {
	public ForgeInit() {
		//Mark this mod as server-only. Hey Forge quick question. What the fuck is this
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		
		Apathy.init();
	}
}
