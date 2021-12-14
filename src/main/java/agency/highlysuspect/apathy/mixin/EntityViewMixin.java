package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.EntityView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

//please tell me i can mixin to default interface methods, please please
@Mixin(EntityView.class)
public interface EntityViewMixin {
	@ModifyVariable(
		method = "getTargets", ordinal = 1 /* idk why i need this, there's only one return point */, at = @At("RETURN")
	)
	default <T extends LivingEntity> List<T> filterTargets(List<T> targets, Class<T> entityClass, TargetPredicate targetPredicate, LivingEntity targetingEntity, Box box) {
		//Miscellaneous method used for some "find nearby things to target" tasks in the game.
		//Normally this is a bit redundant, the main purpose of Apathy is to hook MobEntity#setTarget, which catches most cases.
		//This mixin catches some cases where an entity is attacked without formally being targeted, like the Wither firing black skulls.
		if(targetingEntity instanceof MobEntity) {
			MobEntity mob = (MobEntity) targetingEntity; 
			targets.removeIf(target -> target instanceof ServerPlayerEntity && !Init.mobConfig.allowedToTargetPlayer(mob, (ServerPlayerEntity) target));
		}
		
		return targets;
	}
}
