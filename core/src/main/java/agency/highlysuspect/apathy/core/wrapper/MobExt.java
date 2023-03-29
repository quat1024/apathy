package agency.highlysuspect.apathy.core.wrapper;

import agency.highlysuspect.apathy.core.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

//TODO: merge with Attacker?
public interface MobExt {
	long NOT_PROVOKED = Long.MIN_VALUE;
	
	void apathy$setProvocationTime(long time);
	long apathy$getProvocationTime();
	long apathy$getGameTime();
	
	default void apathy$provokeNow() {
		apathy$setProvocationTime(apathy$getGameTime());
	}
	
	default long apathy$timeSinceProvocation() {
		return apathy$getGameTime() - apathy$getProvocationTime();
	}
	
	default boolean apathy$wasProvoked() {
		return apathy$getProvocationTime() != MobExt.NOT_PROVOKED;
	}
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	/// spawn position stuff ///
	
	@Nullable VecThree apathy$getSpawnPosition();
	Map<String, TriState> apathy$getOrCreateLocationPredicateCache();
}
