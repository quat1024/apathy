package agency.highlysuspect.apathy.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	//Fairly unimportant mixin, but needed for the /apathy reload command, pretty sure.
	@Accessor("resources") ServerResources apathy$getServerResources();
}
