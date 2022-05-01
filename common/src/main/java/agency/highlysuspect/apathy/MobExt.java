package agency.highlysuspect.apathy;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MobExt {
	//TODO clean this shit up (these should be helper methods)
	void apathy$provokeNow();
	long apathy$timeSinceProvocation();
	boolean apathy$wasProvoked();
	void apathy$directlySetProvocationTime(long time);
	long apathy$directlyGetProvocationTime();
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	@Nullable Vec3 apathy$getSpawnPosition();
	
	static MobExt cast(Mob entity) {
		return (MobExt) entity;
	}
}
