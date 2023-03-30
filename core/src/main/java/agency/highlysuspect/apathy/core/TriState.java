package agency.highlysuspect.apathy.core;

import java.util.Locale;

//Previously, when the mod was only for Fabric, it used fabric-api-base's TriState.
//This is a copy of just the bits of TriState that I need.
public enum TriState {
	/** DENY  */ FALSE,
	/** PASS  */ DEFAULT,
	/** ALLOW */ TRUE;
	
	public boolean get() {
		return this == TRUE;
	}
	
	@Override
	public String toString() {
		return name().toLowerCase(Locale.ROOT);
	}
	
	public String toAllowDenyPassString() {
		switch(this) {
			case FALSE: return "deny";
			case DEFAULT: return "pass";
			case TRUE: return "allow";
			default: throw new IllegalArgumentException("Someone's been tampering with the universe!");
		}
	}
	
	public static TriState fromString(String ya) {
		switch(ya.toLowerCase(Locale.ROOT)) {
			case "false": return FALSE;
			case "true": return TRUE;
			default: return DEFAULT;
		}
	}
	
	public static TriState fromAllowDenyPassString(String ya) {
		switch(ya.toLowerCase(Locale.ROOT)) {
			case "deny": return FALSE;
			case "pass": return DEFAULT;
			case "allow": return TRUE;
			default: throw new IllegalArgumentException("expected 'allow', 'deny', or 'pass'");
		}
	}
	
	public static TriState fromBoolean(boolean b) {
		return b ? TRUE : FALSE;
	}
}
