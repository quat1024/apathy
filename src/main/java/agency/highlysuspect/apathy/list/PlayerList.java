package agency.highlysuspect.apathy.list;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerList {
	public static PlayerList getOrCreate(String name, boolean selfSelect) {
		return playerLists.computeIfAbsent(name, (s) -> new PlayerList(name, selfSelect));
	}
	
	public static PlayerList get(String name) {
		return playerLists.get(name);
	}
	
	public static final Map<String, PlayerList> playerLists = new HashMap<>();
	
	private PlayerList(String name, boolean selfSelect) {
		this.name = name;
		this.selfSelect = selfSelect;
	}
	
	public final String name;
	public final boolean selfSelect; //Whether players can join this list on their own.
	
	public boolean contains(ServerPlayerEntity player) {
		return ((ServerPlayerEntityExt) player).apathy$isInList(this);
	}
	
	public boolean join(ServerPlayerEntity player) {
		return ((ServerPlayerEntityExt) player).apathy$joinList(this);
	}
	
	public boolean part(ServerPlayerEntity player) {
		return ((ServerPlayerEntityExt) player).apathy$partList(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		PlayerList that = (PlayerList) o;
		
		return name.equals(that.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public static class Arg implements ArgumentType<PlayerList> {
		@Override
		public PlayerList parse(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();
			
			String listName = reader.readString();
			PlayerList list = playerLists.get(listName);
			if(list == null) {
				reader.setCursor(start);
				throw new SimpleCommandExceptionType(new LiteralText("lkasdlaskjd " + listName)).createWithContext(reader); //TODO
			} else return list;
		}
		
		public static Arg playerList() {
			return new Arg();
		}
		
		public static PlayerList getPlayerList(CommandContext<ServerCommandSource> xd, String name) {
			return xd.getArgument(name, PlayerList.class);
		}
		
		public static CompletableFuture<Suggestions> suggestSelfSelectLists(CommandContext<?> ctx, SuggestionsBuilder builder) {
			List<String> selfSelectLists = playerLists.values()
				.stream()
				.filter(x -> x.selfSelect)
				.map(x -> x.name)
				.collect(Collectors.toList());;
			
			return CommandSource.suggestMatching(selfSelectLists, builder);
		}
		
		public static CompletableFuture<Suggestions> suggestAllLists(CommandContext<?> ctx, SuggestionsBuilder builder) {
			List<String> lists = playerLists.values()
				.stream()
				.map(x -> x.name)
				.collect(Collectors.toList());;
			
			return CommandSource.suggestMatching(lists, builder);
		}
	}
}
