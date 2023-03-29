package agency.highlysuspect.apathy.core.etc;

public enum ElderGuardianEffectN {
	DEFAULT,
	ONLY_SOUND,
	ONLY_PARTICLE,
	DISABLED,
	;
	
	public boolean removeParticle() {
		return this == ONLY_SOUND || this == DISABLED;
	}
	
	public boolean removeSound() {
		return this == ONLY_PARTICLE || this == DISABLED;
	}
}
