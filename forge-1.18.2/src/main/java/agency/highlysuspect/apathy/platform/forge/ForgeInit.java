package agency.highlysuspect.apathy.platform.forge;

import agency.highlysuspect.apathy.Apathy;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod("apathy")
public class ForgeInit {
	public ForgeInit() {
		//borrowed from IExtensionPoint javadoc in fmlcore
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				// Ignore this mod if not present on the client
				() -> NetworkConstants.IGNORESERVERONLY,
				// If present on the client, accept any version if from a server
				(remoteVersion, isFromServer) -> true
			)
		);
		
		Apathy.init();
	}
}
