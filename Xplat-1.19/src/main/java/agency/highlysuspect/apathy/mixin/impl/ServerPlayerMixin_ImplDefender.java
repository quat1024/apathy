package agency.highlysuspect.apathy.mixin.impl;

import agency.highlysuspect.apathy.hell.rule.Defender;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin_ImplDefender implements Defender {
	@Override
	public Object apathy$getServerPlayer() {
		return this;
	}
}
