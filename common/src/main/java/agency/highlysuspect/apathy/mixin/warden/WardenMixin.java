package agency.highlysuspect.apathy.mixin.warden;

import agency.highlysuspect.apathy.Apathy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public class WardenMixin {
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
	public void whenCheckingCanTargetEntity(Entity ent, CallbackInfoReturnable<Boolean> cir) {
		Warden me = (Warden) (Object) this;
		if(ent instanceof ServerPlayer player && !Apathy.INSTANCE.mobConfig.allowedToTargetPlayer(me, player)) cir.setReturnValue(false);
	}
}
