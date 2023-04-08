package agency.highlysuspect.apathy.core.wrapper;

public interface Attacker {
	/**
	 * This sourceset doesn't refer to Minecraft directly wow i love layers of separation.
	 * So this returns Object.
	 */
	Object apathy$underlyingObject();
	AttackerType apathy$getType();
	ApathyDifficulty apathy$getDifficulty();
	
	default boolean apathy$hasType(AttackerType type) {
		return type.apathy$hasType(this);
	}
	
	default boolean apathy$is(AttackerTag tag) {
		return tag.apathy$is(this);
	}
}
