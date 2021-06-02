package agency.highlysuspect.apathy.list;

import agency.highlysuspect.apathy.Init;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerSetManager extends PersistentState {
	public PlayerSetManager() {
		super(KEY);
	}
	
	public static final String KEY = "apathy-player-sets";
	
	public static PlayerSetManager getFor(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(PlayerSetManager::new, KEY);
	}
	
	//Idk if it needs to be a concurrent map really but.... okay
	private final ConcurrentHashMap<String, PlayerSet> playerSets = new ConcurrentHashMap<>();
	
	public boolean hasSet(String name) {
		return playerSets.containsKey(name);
	}
	
	public PlayerSet get(String name) {
		return playerSets.get(name);
	}
	
	public PlayerSet getOrCreate(String name, boolean selfSelect) {
		PlayerSet yea = get(name);
		if(yea != null) return yea;
		else return createSet(name, selfSelect);
	}
	
	public PlayerSet createSet(String name, boolean selfSelect) {
		PlayerSet newOne = new PlayerSet(this, name, selfSelect);
		playerSets.put(name, newOne);
		
		markDirty();
		
		return newOne;
	}
	
	public void deleteSet(String name) {
		playerSets.remove(name);
		markDirty();
	}
	
	public boolean isEmpty() {
		return playerSets.isEmpty();
	}
	
	public Collection<PlayerSet> allSets() {
		return playerSets.values();
	}
	
	public Collection<PlayerSet> allSetsContaining_KindaSlow_DontUseThisOnTheHotPath(ServerPlayerEntity player) {
		//It's just used in commands, i can afford to be slow here.
		return playerSets.values().stream().filter(set -> set.contains(player)).collect(Collectors.toList());
	}
	
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		CompoundTag allSets = new CompoundTag();
		for(Map.Entry<String, PlayerSet> entry : playerSets.entrySet()) {
			allSets.put(entry.getKey(), entry.getValue().toTag());
		}
		tag.put("PlayerSets", allSets);
		
		return tag;
	}
	
	@Override
	public void fromTag(CompoundTag tag) {
		playerSets.clear();
		
		CompoundTag allSets = tag.getCompound("PlayerSets");
		for(String name : allSets.getKeys()) {
			playerSets.put(name, PlayerSet.fromTag(this, name, allSets.getCompound(name)));
		}
	}
	
	public static void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			PlayerSetManager mgr = PlayerSetManager.getFor(server);
			Init.config.playerSetName.ifPresent(s -> {
				if(mgr.hasSet(s)) mgr.get(s).setSelfSelect(Init.config.playerSetSelfSelect);
				else mgr.createSet(s, Init.config.playerSetSelfSelect);
			});
		});
	}
}
