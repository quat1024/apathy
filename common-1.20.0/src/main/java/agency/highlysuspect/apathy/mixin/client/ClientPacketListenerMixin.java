package agency.highlysuspect.apathy.mixin.client;

import agency.highlysuspect.apathy.core.Apathy;
import agency.highlysuspect.apathy.core.CoreBossOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//XXX: probably not the best way to implement this?
@Mixin(ClientPacketListener.class)
@SuppressWarnings("UnnecessaryReturnStatement")
public class ClientPacketListenerMixin {
	@Redirect(
		method = "handleGameEvent",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/ClientLevel;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
		)
	)
	private void apathy$dontPlaySound(ClientLevel instance, Player player, double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
		if(SoundEvents.ELDER_GUARDIAN_CURSE.equals(soundEvent) && Apathy.instance.bossCfg.get(CoreBossOptions.elderGuardianEffect).removeSound()) {
			return;
		} else {
			instance.playSound(player, x, y, z, soundEvent, soundSource, volume, pitch);
		}
	}
	
	@Redirect(
		method = "handleGameEvent",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
		)
	)
	private void apathy$dontAddParticle(ClientLevel instance, ParticleOptions options, double $$1, double $$2, double $$3, double $$4, double $$5, double $$6) {
		if(options.getType() == ParticleTypes.ELDER_GUARDIAN && Apathy.instance.bossCfg.get(CoreBossOptions.elderGuardianEffect).removeParticle()) {
			return;
		} else {
			instance.addParticle(options, $$1, $$2, $$3, $$4, $$5, $$6);
		}
	}
}
