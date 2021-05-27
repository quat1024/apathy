package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.list.PlayerList;
import agency.highlysuspect.apathy.list.ServerPlayerEntityExt;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerEntityExt {
	@Unique Set<PlayerList> joinedPlayerLists = new HashSet<>();
	
	@Override
	public boolean apathy$isInList(PlayerList list) {
		return joinedPlayerLists.contains(list);
	}
	
	@Override
	public boolean apathy$joinList(PlayerList list) {
		return joinedPlayerLists.add(list);
	}
	
	@Override
	public boolean apathy$partList(PlayerList list) {
		return joinedPlayerLists.remove(list);
	}
}
