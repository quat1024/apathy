package agency.highlysuspect.apathy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Init implements ModInitializer {
	public static Config config = new Config();
	
	public static final String MODID = "apathy";
	
	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
	
	@Override
	public void onInitialize() {
		//Yea
	}
}
