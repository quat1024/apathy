package agency.highlysuspect.apathy;

import net.minecraft.entity.mob.MobEntity;

public interface MobEntityExt {
	void apathy$provokeNow();
	long apathy$timeSinceProvocation();
	boolean apathy$wasProvoked();
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	static MobEntityExt cast(MobEntity entity) {
		return (MobEntityExt) entity;
	}
}
