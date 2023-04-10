package agency.highlysuspect.apathy.platform.fabric.mixin;

import agency.highlysuspect.apathy.Apathy116;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

//please tell me i can mixin to default interface methods, please please
@Mixin(EntityGetter.class)
public interface EntityGetterMixin {
	@ModifyVariable(
		method = "getNearbyEntities", ordinal = 1 /* idk why i need this, there's only one return point */, at = @At("RETURN")
	)
	default <T extends LivingEntity> List<T> apathy$filterTargets(List<T> targets, Class<T> entityClass, TargetingConditions targetPredicate, LivingEntity targetingEntity, AABB box) {
		//Miscellaneous method used for some "find nearby things to target" tasks in the game.
		//Normally this is a bit redundant, the main purpose of Apathy is to hook MobEntity#setTarget, which catches most cases.
		//This mixin catches some cases where an entity is attacked without formally being targeted, like the Wither firing black skulls.
		if(targetingEntity instanceof Mob) {
			targets.removeIf(target -> target instanceof ServerPlayer && !Apathy116.instance116.allowedToTargetPlayer((Mob) targetingEntity, (ServerPlayer) target));
		}
		
		return targets;
	}
}
