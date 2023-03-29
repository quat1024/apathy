package agency.highlysuspect.apathy.core.etc;

public enum PortalInitialStateN {
	CLOSED,
	OPEN,
	OPEN_WITH_EGG,
	;
	
	public boolean isOpenByDefault() {
		return this != CLOSED;
	}
	
	public boolean hasEgg() {
		return this == OPEN_WITH_EGG;
	}
}
