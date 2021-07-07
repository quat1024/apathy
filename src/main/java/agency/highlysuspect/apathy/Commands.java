package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.playerset.PlayerSet;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("SameReturnValue")
public class Commands {
	public static void onInitialize() {
		CommandRegistrationCallback.EVENT.register(Commands::registerCommands);
	}
	
	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
		//Do not trust the indentation lmao
		//I've been burned before
		//Be careful
		dispatcher.register(literal(Init.MODID)
			.then(literal("set")
				.then(literal("join")
					.then(argument("set", string()).suggests(PlayerSetManager::suggestSelfSelectPlayerSets)
						.executes(cmd -> joinSet(cmd, Collections.singletonList(cmd.getSource().getPlayer()), getString(cmd, "set"), true))))
				.then(literal("part")
					.then(argument("set", string()).suggests(PlayerSetManager::suggestSelfSelectPlayerSets)
						.executes(cmd -> partSet(cmd, Collections.singletonList(cmd.getSource().getPlayer()), getString(cmd, "set"), true))))
				.then(literal("show")
					.executes(cmd -> showSet(cmd, Collections.singletonList(cmd.getSource().getPlayer())))))
			.then(literal("set-admin")
				.requires(src -> src.hasPermissionLevel(2))
				.then(literal("join")
					.then(argument("who", players())
						.then(argument("set", string()).suggests(PlayerSetManager::suggestAllPlayerSets)
							.executes(cmd -> joinSet(cmd, getPlayers(cmd, "who"), getString(cmd, "set"), false)))))
				.then(literal("part")
					.then(argument("who", players())
						.then(argument("set", string()).suggests(PlayerSetManager::suggestAllPlayerSets)
							.executes(cmd -> partSet(cmd, getPlayers(cmd, "who"), getString(cmd, "set"), false)))))
				.then(literal("show-all")
					.executes(Commands::showAll))
				.then(literal("delete")
					.then(argument("set", string()).suggests(PlayerSetManager::suggestAllPlayerSets)
						.executes(cmd -> deleteSet(cmd, getString(cmd, "set")))))
				.then(literal("create")
					.then(argument("name", word())
						.then(argument("self-select", bool())
							.executes(cmd -> createSet(cmd, getString(cmd, "name"), getBool(cmd, "self-select"))))))
				.then(literal("edit")
					.then(argument("set", string()).suggests(PlayerSetManager::suggestAllPlayerSets)
						.then(argument("self-select", bool())
							.executes(cmd -> editSet(cmd, getString(cmd, "set"), getBool(cmd, "self-select")))))))
			.then(literal("reload")
				.requires(src -> src.hasPermissionLevel(2))
				.executes(Commands::reloadNow))
		);
	}
	
	private static int joinSet(CommandContext<ServerCommandSource> cmd, Collection<ServerPlayerEntity> players, String setName, boolean requireSelfSelect) {
		PlayerSet set = PlayerSetManager.getFor(cmd).get(setName);
		if(set == null) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.error.noSet", setName));
			return 0;
		}
		
		if(requireSelfSelect && !set.isSelfSelect()) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.set.notSelfSelect", set.getName()));
			return 0;
		}
		
		int success = 0;
		for(ServerPlayerEntity player : players) {
			success += runOnPlayerSet(cmd, player, set, "apathy.commands.set.joinSuccess", "apathy.commands.set.alreadyInSet", PlayerSet::join);
		}
		return success;
	}
	
	private static int partSet(CommandContext<ServerCommandSource> cmd, Collection<ServerPlayerEntity> players, String setName, boolean requireSelfSelect) {
		PlayerSet set = PlayerSetManager.getFor(cmd).get(setName);
		if(set == null) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.error.noSet", setName));
			return 0;
		}
		
		if(requireSelfSelect && !set.isSelfSelect()) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.set.notSelfSelect", set.getName()));
			return 0;
		}
		
		int success = 0;
		for(ServerPlayerEntity player : players) {
			success += runOnPlayerSet(cmd, player, set, "apathy.commands.set.partSuccess", "apathy.commands.set.notInSet", PlayerSet::part);
		}
		return success;
	}
	
	private static int runOnPlayerSet(CommandContext<ServerCommandSource> cmd, ServerPlayerEntity player, PlayerSet set, String success, String fail, BiFunction<PlayerSet, ServerPlayerEntity, Boolean> thing) {
		if(thing.apply(set, player)) {
			cmd.getSource().sendFeedback(new TranslatableText(success, player.getName(), set.getName()), true);
			return 1;
		} else {
			cmd.getSource().sendError(new TranslatableText(fail, player.getName(), set.getName()));
			return 0;
		}
	}
	
	private static int showSet(CommandContext<ServerCommandSource> cmd, Collection<ServerPlayerEntity> players) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		
		if(setManager.isEmpty()) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.set.available.none"));
		} else {
			cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.set.available", Texts.join(setManager.allSets(), PlayerSet::toText)), false);
		}
		
		int success = 0;
		
		for(ServerPlayerEntity player : players) {
			Collection<PlayerSet> yea = setManager.allSetsContaining_KindaSlow_DontUseThisOnTheHotPath(player);
			
			if(yea.isEmpty()) {
				cmd.getSource().sendError(new TranslatableText("apathy.commands.set.show.none", player.getName()));
			} else {
				cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.set.show", player.getName(), Texts.join(yea, PlayerSet::toText)), false);
				success++;
			}
		}
		
		return success;
	}
	
	private static int showAll(CommandContext<ServerCommandSource> cmd) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		
		if(setManager.isEmpty()) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.show-all.none"));
		} else {
			cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.show-all.list", Texts.join(setManager.allSets(), PlayerSet::toText)), false);
			PlayerManager mgr = cmd.getSource().getServer().getPlayerManager();
			
			for(PlayerSet set : setManager.allSets()) {
				cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.show-all.set", set.getName(), set.members().size()), false);
				for(UUID uuid : set.members()) {
					ServerPlayerEntity player = mgr.getPlayer(uuid);
					TranslatableText asdf = player == null ?
						new TranslatableText("apathy.commands.show-all.set.member.offline-player", uuid) :
						new TranslatableText("apathy.commands.show-all.set.member.online-player", player.getName(), uuid);
					cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.show-all.set.member", asdf), false);
				}
			}
		}
		return 0;
	}
	
	private static int createSet(CommandContext<ServerCommandSource> cmd, String name, boolean selfSelect) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		if(setManager.hasSet(name)) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.add.fail.already-exists", name));
			return 0;
		}
		
		setManager.createSet(name, selfSelect);
		cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.add.success" + (selfSelect ? ".self-select" : ""), name), false);
		return 1;
	}
	
	private static int editSet(CommandContext<ServerCommandSource> cmd, String setName, boolean selfSelect) {
		PlayerSet set = PlayerSetManager.getFor(cmd).get(setName);
		if(set == null) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.error.noSet", setName));
			return 0;
		}
		
		Optional<String> yeayehhehh = Init.mobConfig.playerSetName;
		if(yeayehhehh.isPresent() && yeayehhehh.get().equals(set.getName())) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.edit.fail.config", set.getName()));
			return 0;
		}
		
		set.setSelfSelect(selfSelect);
		
		cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.edit.success" + (selfSelect ? ".self-select" : ""), set.getName()), false);
		return 1;
	}
	
	private static int deleteSet(CommandContext<ServerCommandSource> cmd, String setName) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		PlayerSet set = setManager.get(setName);
		if(set == null) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.error.noSet", setName));
			return 0;
		}
		
		Optional<String> yeayehhehh = Init.mobConfig.playerSetName;
		if(yeayehhehh.isPresent() && yeayehhehh.get().equals(set.getName())) {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.delete.fail.config", set.getName()));
			return 0;
		}
		
		if(setManager.hasSet(set.getName())) {
			setManager.deleteSet(set.getName());
			cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.delete.success", set.getName()), false);
			return 1;
		} else {
			cmd.getSource().sendError(new TranslatableText("apathy.commands.delete.fail.noSet", set.getName()));
			return 0;
		}
	}
	
	private static int reloadNow(CommandContext<ServerCommandSource> cmd) {
		Init.reloadNow(cmd.getSource().getServer());
		cmd.getSource().sendFeedback(new TranslatableText("apathy.commands.reload"), true);
		return 0;
	}
}
