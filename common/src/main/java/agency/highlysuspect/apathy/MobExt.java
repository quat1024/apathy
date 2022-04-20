package agency.highlysuspect.apathy;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MobExt {
	void apathy$provokeNow();
	long apathy$timeSinceProvocation();
	boolean apathy$wasProvoked();
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	@Nullable Vec3 apathy$getSpawnPosition();
	
	static MobExt cast(Mob entity) {
		return (MobExt) entity;
	}
}
