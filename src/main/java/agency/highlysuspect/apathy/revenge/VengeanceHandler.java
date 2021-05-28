package agency.highlysuspect.apathy.revenge;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.ActionResult;

public class VengeanceHandler {
	public static void onInitialize() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!world.isClient && entity instanceof MobEntity) {
				((MobEntityExt) entity).apathy$provokeNow();
			}
			
			return ActionResult.PASS;
		});
	}
	
	public static boolean wasProvoked(MobEntity entity) {
		return ((MobEntityExt) entity).apathy$wasProvoked();
	}
	
	public static long timeSinceProvocation(MobEntity entity) {
		return ((MobEntityExt) entity).apathy$timeSinceProvocation();
	}
}
