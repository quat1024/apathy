package agency.highlysuspect.apathy.core.wrapper;

import agency.highlysuspect.apathy.core.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Attacker {
	/**
	 * This sourceset doesn't refer to Minecraft directly wow i love layers of separation.
	 * So this returns Object.
	 */
	Object apathy$underlyingObject();
	AttackerType apathy$getType();
	ApathyDifficulty apathy$getDifficulty();
	
	/// revenge timer stuff ///
	long NOT_PROVOKED = Long.MIN_VALUE;
	
	void apathy$setProvocationTime(long time);
	long apathy$getProvocationTime();
	long apathy$now();
	
	/// spawn position stuff ///
	
	@Nullable VecThree apathy$getSpawnPosition();
	Map<String, TriState> apathy$getOrCreateLocationPredicateCache();
	
	/// PartialSpecRandom ///
	
	int apathy$uuidBits();
}
