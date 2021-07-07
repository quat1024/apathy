package agency.highlysuspect.apathy.playerset;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
	
	public boolean join(ServerPlayerEntity player) {
		this.owner.markDirty();
		return members.add(player.getUuid());
	}
	
	public boolean part(ServerPlayerEntity player) {
		this.owner.markDirty();
		return members.remove(player.getUuid());
	}
	
	public boolean contains(ServerPlayerEntity player) {
		return members.contains(player.getUuid());
	}
	
	public Collection<UUID> members() {
		return members;
	}
	
	public boolean isSelfSelect() {
		return selfSelect;
	}
	
	public void setSelfSelect(boolean selfSelect) {
		if(this.selfSelect != selfSelect) this.owner.markDirty();
		this.selfSelect = selfSelect;
	}
	
	public String getName() {
		return name;
	}
	
	public Text toLiteralText() {
		return new LiteralText(String.format(selfSelect ? "%s (self-select)" : "%s", name));
	}
	
	public static PlayerSet fromTag(PlayerSetManager owner, String name, NbtCompound tag) {
		HashSet<UUID> members = new HashSet<>();
		
		NbtList memberList = tag.getList("Members", 11);
		for(NbtElement value : memberList) {
			members.add(NbtHelper.toUuid(value));
		}
		
		boolean selfSelect = tag.getBoolean("SelfSelect");
		
		return new PlayerSet(owner, members, name, selfSelect);
	}
	
	public NbtCompound toTag() {
		NbtCompound tag = new NbtCompound();
		
		NbtList memberList = new NbtList();
		for(UUID uuid : members) {
			memberList.add(NbtHelper.fromUuid(uuid));
		}
		tag.put("Members", memberList);
		tag.putBoolean("SelfSelect", selfSelect);
		return tag;
	}
}
