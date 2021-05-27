package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.list.PlayerList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;

import static agency.highlysuspect.apathy.list.PlayerList.Arg.*;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class Commands {
	public static void registerArgumentTypes() {
		ArgumentTypes.register(Init.id("player_list").toString(), PlayerList.Arg.class, new ConstantArgumentSerializer<>(PlayerList.Arg::playerList));
	}
	
	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
		dispatcher.register(literal(Init.MODID)
			.then(literal("list")
				.then(literal("join")
					.then(argument("list", playerList()).suggests(PlayerList.Arg::suggestSelfSelectLists)
						.executes(cmd -> join(cmd, Collections.singletonList(cmd.getSource().getPlayer()), getPlayerList(cmd, "list"), true))))
				.then(literal("part")
					.then(argument("list", playerList()).suggests(PlayerList.Arg::suggestSelfSelectLists)
						.executes(cmd -> part(cmd, Collections.singletonList(cmd.getSource().getPlayer()), getPlayerList(cmd, "list"), true)))))
			.then(literal("list-admin")
				.requires(src -> src.hasPermissionLevel(2))
				.then(literal("join")
					.then(argument("who", players())
						.then(argument("list", playerList()).suggests(PlayerList.Arg::suggestAllLists)
							.executes(cmd -> join(cmd, getPlayers(cmd, "who"), getPlayerList(cmd, "list"), false)))))
				.then(literal("part").then(argument("who", players())
					.then(argument("list", playerList()).suggests(PlayerList.Arg::suggestAllLists)
						.executes(cmd -> part(cmd, getPlayers(cmd, "who"), getPlayerList(cmd, "list"), false))))))
		);
	}
	
	private static int join(CommandContext<ServerCommandSource> cmd, Collection<ServerPlayerEntity> players, PlayerList list, boolean requireSelfSelect) {
		if(requireSelfSelect && !list.selfSelect) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.list.notSelfSelect", list.name));
			return 0;
		}
		
		int success = 0;
		for(ServerPlayerEntity player : players) {
			success += frobnicate(cmd, player, list, "apathy.commands.list.joinSuccess", "apathy.commands.list.alreadyInList", PlayerList::join);
		}
		return success;
	}
	
	private static int part(CommandContext<ServerCommandSource> cmd, Collection<ServerPlayerEntity> players, PlayerList list, boolean requireSelfSelect) {
		if(requireSelfSelect && !list.selfSelect) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.list.notSelfSelect", list.name));
			return 0;
		}
		
		int success = 0;
		for(ServerPlayerEntity player : players) {
			success += frobnicate(cmd, player, list, "apathy.commands.list.partSuccess", "apathy.commands.list.notInList", PlayerList::part);
		}
		return success;
	}
	
	private static int frobnicate(CommandContext<ServerCommandSource> cmd, ServerPlayerEntity player, PlayerList list, String success, String fail, BiFunction<PlayerList, ServerPlayerEntity, Boolean> thing) {
		if(thing.apply(list, player)) {
			cmd.getSource().sendFeedback(new TranslatableText(success, player.getName(), list.name), true);
			return 1;
		} else {
			cmd.getSource().sendError(new TranslatableText(fail, player.getName(), list.name));
			return 0;
		}
	}
}
