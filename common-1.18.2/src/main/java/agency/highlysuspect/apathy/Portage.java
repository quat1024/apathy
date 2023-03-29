package agency.highlysuspect.apathy;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class Portage {
	public static String stringifyComponent(Component c) {
		return c.getContents();
	}
	
	public static Component literal(String s) {
		return new TextComponent(s);
	}
}
