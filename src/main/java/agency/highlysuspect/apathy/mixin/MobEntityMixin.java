package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Init;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	public void whenSettingTarget(@Nullable LivingEntity target, CallbackInfo ci) {
		if(target instanceof PlayerEntity&& Init.config.preventAttackTargetChange((MobEntity) (Object) this, (PlayerEntity) target)) {
			ci.cancel();
		}
	}
}
