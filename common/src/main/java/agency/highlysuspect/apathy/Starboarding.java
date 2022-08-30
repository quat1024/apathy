package agency.highlysuspect.apathy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

//The opposite of a good time porting.
public class Starboarding {
	public static Component newTextComponent(String key) {
		return new TextComponent(key);
	}
}
