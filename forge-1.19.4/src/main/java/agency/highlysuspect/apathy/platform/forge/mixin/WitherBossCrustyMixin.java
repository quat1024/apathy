package agency.highlysuspect.apathy.platform.forge.mixin;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(WitherBoss.class)
public class WitherBossCrustyMixin {
	@SuppressWarnings("InvalidInjectorMethodSignature") //mcdev is having a moment
	@ModifyVariable(
		method = "customServerAiStep",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/level/Level;getNearbyEntities(Ljava/lang/Class;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
		)
	)
	private List<LivingEntity> filterGetNearbyEntitiesCrusty(List<LivingEntity> nearbyEntities) {
		@SuppressWarnings("ConstantConditions")
		WitherBoss wither = (WitherBoss) (Object) this;
		
		//In fabric I filter (Level/EntityGetter)#getNearbyEntities directly, which seems like the best way to go about this.
		//Forge has a crusty version of mixin that doesn't support targeting default interface methods.		
		//So under forge I do a less general-purpose patch, that only targets the Wither.
		//I also did this in fabric on 1.17- which also had a crusty version of mixin.
		
		List<LivingEntity> defensiveCopy = new ArrayList<>(nearbyEntities);
		defensiveCopy.removeIf(nearby -> nearby instanceof ServerPlayer player && !Apathy.INSTANCE.allowedToTargetPlayer(wither, player));
		return defensiveCopy;
	}
}
