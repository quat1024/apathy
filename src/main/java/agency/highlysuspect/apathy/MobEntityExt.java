package agency.highlysuspect.apathy;

import net.minecraft.world.entity.Mob;

public interface MobEntityExt {
	void apathy$provokeNow();
	long apathy$timeSinceProvocation();
	boolean apathy$wasProvoked();
	
	default boolean apathy$lastAttackedWithin(long timeframe) {
		return apathy$wasProvoked() && apathy$timeSinceProvocation() <= timeframe;
	}
	
	static MobEntityExt cast(Mob entity) {
		return (MobEntityExt) entity;
	}
}
