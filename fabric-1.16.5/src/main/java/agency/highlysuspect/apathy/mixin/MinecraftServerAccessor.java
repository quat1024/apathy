package agency.highlysuspect.apathy.mixin;

import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	//Fairly unimportant mixin, but needed for the /apathy reload command, pretty sure.
	@Accessor("serverResourceManager") ServerResourceManager apathy$getServerResourceManager();
}
