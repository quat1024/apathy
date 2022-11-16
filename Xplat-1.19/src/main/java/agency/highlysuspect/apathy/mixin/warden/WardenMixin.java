package agency.highlysuspect.apathy.mixin.warden;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
@SuppressWarnings("ConstantConditions")
public class WardenMixin {
	@Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
	public void apathy$whenCheckingCanTargetEntity(Entity ent, CallbackInfoReturnable<Boolean> cir) {
		Warden me = (Warden) (Object) this;
		if(ent instanceof ServerPlayer player && !Apathy.INSTANCE.mobConfig.allowedToTargetPlayer(me, player)) cir.setReturnValue(false);
	}
	
	@Inject(method = "setAttackTarget", at = @At("HEAD"), cancellable = true)
	public void apathy$whenSettingAttackTarget(LivingEntity ent, CallbackInfo ci) {
		Warden me = (Warden) (Object) this;
		if(ent instanceof ServerPlayer player && !Apathy.INSTANCE.mobConfig.allowedToTargetPlayer(me, player)) ci.cancel();
	}
}
