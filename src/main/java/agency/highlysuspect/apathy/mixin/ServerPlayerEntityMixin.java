package agency.highlysuspect.apathy.mixin;

import agency.highlysuspect.apathy.list.PlayerList;
import agency.highlysuspect.apathy.list.ServerPlayerEntityExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
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
	
	@Override
	public Collection<PlayerList> apathy$allLists() {
		return joinedPlayerLists;
	}
	
	@Unique private static final String PLAYER_LIST_KEY = "apathy-playerlists";
	
	@Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
	public void whenSaving(CompoundTag tag, CallbackInfo ci) {
		if(!joinedPlayerLists.isEmpty()) {
			ListTag listTag = new ListTag();
			joinedPlayerLists.forEach(playerList -> listTag.add(StringTag.of(playerList.name)));
			tag.put(PLAYER_LIST_KEY, listTag);
		}
	}
	
	@Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
	public void whenLoading(CompoundTag tag, CallbackInfo ci) {
		joinedPlayerLists.clear();
		
		ListTag listTag = tag.getList(PLAYER_LIST_KEY, 8); //StringTag
		for(int i = 0; i < listTag.size(); i++) {
			String listName = listTag.getString(i);
			
			PlayerList playerList = PlayerList.get(listName);
			if(playerList != null) {
				joinedPlayerLists.add(playerList);
			}
		}
	}
}
