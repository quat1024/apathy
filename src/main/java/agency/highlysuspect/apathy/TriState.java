package agency.highlysuspect.apathy;

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
}
