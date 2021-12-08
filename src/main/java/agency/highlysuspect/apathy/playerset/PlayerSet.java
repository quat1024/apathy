package agency.highlysuspect.apathy.playerset;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSet {
	public PlayerSet(PlayerSetManager owner, Set<UUID> members, String name, boolean selfSelect) {
		this.owner = owner;
		this.members = members;
		this.name = name;
		this.selfSelect = selfSelect;
	}
	
	public PlayerSet(PlayerSetManager owner, String name, boolean selfSelect) {
		this(owner, new HashSet<>(), name, selfSelect);
	}
	
	private final PlayerSetManager owner;
	private final Set<UUID> members;
	private final String name;
	private boolean selfSelect;
	
	public boolean join(ServerPlayer player) {
		this.owner.setDirty();
		return members.add(player.getUUID());
	}
	
	public boolean part(ServerPlayer player) {
		this.owner.setDirty();
		return members.remove(player.getUUID());
	}
	
	public boolean contains(ServerPlayer player) {
		return members.contains(player.getUUID());
	}
	
	public Collection<UUID> members() {
		return members;
	}
	
	public boolean isSelfSelect() {
		return selfSelect;
	}
	
	public void setSelfSelect(boolean selfSelect) {
		if(this.selfSelect != selfSelect) this.owner.setDirty();
		this.selfSelect = selfSelect;
	}
	
	public String getName() {
		return name;
	}
	
	public Component toLiteralText() {
		return new TextComponent(String.format(selfSelect ? "%s (self-select)" : "%s", name));
	}
	
	public static PlayerSet fromTag(PlayerSetManager owner, String name, CompoundTag tag) {
		HashSet<UUID> members = new HashSet<>();
		
		ListTag memberList = tag.getList("Members", 11);
		for(Tag value : memberList) {
			members.add(NbtUtils.loadUUID(value));
		}
		
		boolean selfSelect = tag.getBoolean("SelfSelect");
		
		return new PlayerSet(owner, members, name, selfSelect);
	}
	
	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		
		ListTag memberList = new ListTag();
		for(UUID uuid : members) {
			memberList.add(NbtUtils.createUUID(uuid));
		}
		tag.put("Members", memberList);
		tag.putBoolean("SelfSelect", selfSelect);
		return tag;
	}
}
