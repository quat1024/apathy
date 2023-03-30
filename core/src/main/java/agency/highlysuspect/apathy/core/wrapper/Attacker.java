package agency.highlysuspect.apathy.core.wrapper;

public interface Attacker {
	/**
	 * This sourceset doesn't refer to Minecraft directly wow i love layers of separation.
	 * So this returns Object.
	 */
	Object apathy$getMob();
	
	Object apathy$getEntityType();
	
	ApathyDifficulty apathy$getDifficulty();
}
