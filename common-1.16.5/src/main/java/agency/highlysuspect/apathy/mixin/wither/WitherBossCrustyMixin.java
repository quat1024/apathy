package agency.highlysuspect.apathy.mixin.wither;

import agency.highlysuspect.apathy.Apathy116;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * original comment (1.18/19):
 * //In fabric I filter (Level/EntityGetter)#getNearbyEntities directly, which seems like the best way to go about this.
 * //Forge has a crusty version of mixin that doesn't support targeting default interface methods.		
 * //So under forge I do a less general-purpose patch, that only targets the Wither.
 * //I also did this in fabric on 1.17- which also had a crusty version of mixin.
 * 
 * Turns out that Fabric 1.16 also has the crusty version of mixin in question, so I need to use this on both sides.
 * I have pasted it into the common module then
 */
@Mixin(WitherBoss.class)
public class WitherBossCrustyMixin {
	@SuppressWarnings({"MixinAnnotationTarget", "InvalidInjectorMethodSignature"}) //mcdev is crashing LOL
	@ModifyVariable(
		method = "customServerAiStep",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/level/Level;getNearbyEntities(Ljava/lang/Class;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
		)
	)
	private List<LivingEntity> apathy$filterGetNearbyEntitiesCrusty(List<LivingEntity> nearbyEntities) {
		@SuppressWarnings("ConstantConditions")
		WitherBoss wither = (WitherBoss) (Object) this;
		
		//In fabric I filter (Level/EntityGetter)#getNearbyEntities directly, which seems like the best way to go about this.
		//Forge has a crusty version of mixin that doesn't support targeting default interface methods.		
		//So under forge I do a less general-purpose patch, that only targets the Wither.
		//I also did this in fabric on 1.17- which also had a crusty version of mixin.
		
		List<LivingEntity> defensiveCopy = new ArrayList<>(nearbyEntities);
		defensiveCopy.removeIf(nearby -> nearby instanceof ServerPlayer && !Apathy116.instance116.allowedToTargetPlayer(wither, (ServerPlayer) nearby));
		return defensiveCopy;
	}
}

