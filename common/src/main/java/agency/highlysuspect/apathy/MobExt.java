package agency.highlysuspect.apathy;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MobExt {
	long NOT_PROVOKED = Long.MIN_VALUE;
	
	void apathy$setProvocationTime(long time);
	long apathy$getProvocationTime();
	
	default void apathy$provokeNow() {
		apathy$setProvocationTime(((Mob) this).level.getGameTime());
	}
	
	default long apathy$timeSinceProvocation() {
		return ((Mob) this).level.getGameTime() - apathy$getProvocationTime();
	}
	
	default boolean apathy$wasProvoked() {
		return apathy$getProvocationTime() != MobExt.NOT_PROVOKED;
	}
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	/// spawn position stuff ///
	
	@Nullable Vec3 apathy$getSpawnPosition();
}
