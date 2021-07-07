package agency.highlysuspect.apathy.playerset;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerSetArgumentType implements ArgumentType<String> {
	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}
	
	public static PlayerSetArgumentType playerSet() {
		return new PlayerSetArgumentType();
	}
	
	public static PlayerSet getPlayerSet(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
		String setName = context.getArgument(name, String.class);
		PlayerSetManager setManager = PlayerSetManager.getFor(context.getSource().getServer());
		PlayerSet set = setManager.get(setName);
		if(set == null) {
			throw new SimpleCommandExceptionType(new TranslatableText("apathy.commands.arg.notSet", setName)).create();
		} else return set;
	}
	
	public static CompletableFuture<Suggestions> suggestSelfSelectPlayerSets(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		PlayerSetManager setManager = PlayerSetManager.getFor(context.getSource().getServer());
		return CommandSource.suggestMatching(setManager.allSets().stream()
			.filter(PlayerSet::isSelfSelect)
			.map(PlayerSet::getName)
			.collect(Collectors.toList()), builder);
	}
	
	public static CompletableFuture<Suggestions> suggestAllPlayerSets(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		PlayerSetManager setManager = PlayerSetManager.getFor(context.getSource().getServer());
		return CommandSource.suggestMatching(setManager.allSets().stream()
			.map(PlayerSet::getName)
			.collect(Collectors.toList()), builder);
	}
}
