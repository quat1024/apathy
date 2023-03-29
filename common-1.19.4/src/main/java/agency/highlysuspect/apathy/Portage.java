package agency.highlysuspect.apathy;

import net.minecraft.network.chat.Component;

public class Portage {
	public static String stringifyComponent(Component c) {
		return c.getString();
	}
	
	public static Component literal(String s) {
		return Component.literal(s);
	}
}
