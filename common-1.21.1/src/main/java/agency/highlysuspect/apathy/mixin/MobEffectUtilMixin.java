package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.Apathy121;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(MobEffectUtil.class)
public class MobEffectUtilMixin {
	@SuppressWarnings("InvalidInjectorMethodSignature") //Mcdev seems to be getting the locals table wrong
	@Inject(method = "addEffectToPlayersAround", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void apathy$onAddEffectToPlayersAround(ServerLevel level, @Nullable Entity provoker, Vec3 what, double huh, MobEffectInstance effect, int hmm, CallbackInfoReturnable<List<ServerPlayer>> cir, Holder<MobEffect> hrmm, List<ServerPlayer> original) {
		Apathy121.instance121.filterMobEffectUtilCall(level, provoker, original);
	}
}
