package agency.highlysuspect.apathy.playerset;

import agency.highlysuspect.apathy.platform.PlatformSupport;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerSetManager extends SavedData {
	public PlayerSetManager() {
		this.playerSets = new ConcurrentHashMap<>();
	}
	
	public PlayerSetManager(CompoundTag tag) {
		this();
		CompoundTag allSets = tag.getCompound("PlayerSets");
		for(String name : allSets.getAllKeys()) {
			playerSets.put(name, PlayerSet.fromTag(this, name, allSets.getCompound(name)));
		}
	}
	
	public static PlayerSetManager getFor(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(
			PlayerSetManager::new, //Nbt constructor
			PlayerSetManager::new, //Default constructor
			"apathy-player-sets"
		);
	}
	
	public static PlayerSetManager getFor(CommandContext<CommandSourceStack> cmdCtx) {
		return getFor(cmdCtx.getSource().getServer());
	}
	
	//Idk if it needs to be a concurrent map really but.... okay
	private final ConcurrentHashMap<String, PlayerSet> playerSets;
	
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
		
		setDirty();
		
		return newOne;
	}
	
	public void deleteSet(String name) {
		playerSets.remove(name);
		setDirty();
	}
	
	public boolean isEmpty() {
		return playerSets.isEmpty();
	}
	
	public Collection<PlayerSet> allSets() {
		return playerSets.values();
	}
	
	public Collection<PlayerSet> allSetsContaining_KindaSlow_DontUseThisOnTheHotPath(ServerPlayer player) {
		//It's just used in commands, i can afford to be slow here.
		return playerSets.values().stream().filter(set -> set.contains(player)).collect(Collectors.toList());
	}
	
	@Override
	public CompoundTag save(CompoundTag tag) {
		CompoundTag allSets = new CompoundTag();
		for(Map.Entry<String, PlayerSet> entry : playerSets.entrySet()) {
			allSets.put(entry.getKey(), entry.getValue().toTag());
		}
		tag.put("PlayerSets", allSets);
		
		return tag;
	}
	
	public static CompletableFuture<Suggestions> suggestSelfSelectPlayerSets(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		PlayerSetManager setManager = getFor(context);
		return SharedSuggestionProvider.suggest(setManager.allSets().stream()
			.filter(PlayerSet::isSelfSelect)
			.map(PlayerSet::getName)
			.collect(Collectors.toList()), builder);
	}
	
	public static CompletableFuture<Suggestions> suggestAllPlayerSets(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
		PlayerSetManager setManager = getFor(context);
		return SharedSuggestionProvider.suggest(setManager.allSets().stream()
			.map(PlayerSet::getName)
			.collect(Collectors.toList()), builder);
	}
}
