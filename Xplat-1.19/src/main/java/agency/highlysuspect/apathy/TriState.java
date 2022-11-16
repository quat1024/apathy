package agency.highlysuspect.apathy;

import java.util.Locale;

//Previously, when the mod was only for Fabric, it used fabric-api-base's TriState.
//This is a copy of just the bits of TriState that I need.
public enum TriState {
	FALSE,
	DEFAULT,
	TRUE,
	;
	
	public boolean get() {
		return this == TRUE;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public static TriState fromString(String ya) {
		return switch(ya.toLowerCase(Locale.ROOT)) {
			case "false" -> FALSE;
			case "true" -> TRUE;
			default -> DEFAULT;
		};
	}
	
	public static TriState fromBoolean(boolean b) {
		return b ? TRUE : FALSE;
	}
}
