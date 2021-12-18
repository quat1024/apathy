package agency.highlysuspect.apathy;

import net.minecraft.world.entity.Mob;

public interface MobExt {
	void apathy$provokeNow();
	long apathy$timeSinceProvocation();
	boolean apathy$wasProvoked();
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	static MobExt cast(Mob entity) {
		return (MobExt) entity;
	}
}
