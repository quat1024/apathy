package agency.highlysuspect.apathy.playerset;

import agency.highlysuspect.apathy.Init;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerSetManager extends PersistentState {
	public PlayerSetManager() {
		this.playerSets = new ConcurrentHashMap<>();
	}
	
	public PlayerSetManager(Map<String, PlayerSet> playerSets) {
		this();
		this.playerSets.putAll(playerSets);
	}
	
	public PlayerSetManager(NbtCompound tag) {
		this();
		NbtCompound allSets = tag.getCompound("PlayerSets");
		for(String name : allSets.getKeys()) {
			playerSets.put(name, PlayerSet.fromTag(this, name, allSets.getCompound(name)));
		}
	}
	
	public static PlayerSetManager getFor(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(
			PlayerSetManager::new, //Nbt constructor
			PlayerSetManager::new, //Default constructor
			"apathy-player-sets"
		);
	}
	
	public static PlayerSetManager getFor(CommandContext<ServerCommandSource> cmdCtx) {
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
	public NbtCompound writeNbt(NbtCompound tag) {
		NbtCompound allSets = new NbtCompound();
		for(Map.Entry<String, PlayerSet> entry : playerSets.entrySet()) {
			allSets.put(entry.getKey(), entry.getValue().toTag());
		}
		tag.put("PlayerSets", allSets);
		
		return tag;
	}
	
	public static void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			PlayerSetManager mgr = PlayerSetManager.getFor(server);
			Init.mobConfig.playerSetName.ifPresent(s -> {
				if(mgr.hasSet(s)) mgr.get(s).setSelfSelect(Init.mobConfig.playerSetSelfSelect);
				else mgr.createSet(s, Init.mobConfig.playerSetSelfSelect);
			});
		});
	}
	
	public static CompletableFuture<Suggestions> suggestSelfSelectPlayerSets(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		PlayerSetManager setManager = getFor(context);
		return CommandSource.suggestMatching(setManager.allSets().stream()
			.filter(PlayerSet::isSelfSelect)
			.map(PlayerSet::getName)
			.collect(Collectors.toList()), builder);
	}
	
	public static CompletableFuture<Suggestions> suggestAllPlayerSets(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		PlayerSetManager setManager = getFor(context);
		return CommandSource.suggestMatching(setManager.allSets().stream()
			.map(PlayerSet::getName)
			.collect(Collectors.toList()), builder);
	}
}
