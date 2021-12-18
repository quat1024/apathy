package agency.highlysuspect.apathy.platform;

//Shameless copy of fabric-api-base's TriState.
public enum TriState {
	FALSE,
	DEFAULT,
	TRUE,
	;
	
	public boolean get() {
		return this == TRUE;
	}
}
