package agency.highlysuspect.apathy;

import agency.highlysuspect.apathy.playerset.PlayerSet;
import agency.highlysuspect.apathy.playerset.PlayerSetManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.arguments.EntityArgument.getPlayers;
import static net.minecraft.commands.arguments.EntityArgument.players;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@SuppressWarnings("SameReturnValue")
public class Commands {
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
		//Do not trust the indentation lmao
		//I've been burned before
		//Be careful
		dispatcher.register(literal(Apathy.MODID)
			.then(literal("set")
				.then(literal("join")
					.then(argument("set", string()).suggests(PlayerSetManager::suggestSelfSelectPlayerSets)
						.executes(cmd -> joinSet(cmd, Collections.singletonList(cmd.getSource().getPlayerOrException()), getString(cmd, "set"), true))))
				.then(literal("part")
					.then(argument("set", string()).suggests(PlayerSetManager::suggestSelfSelectPlayerSets)
						.executes(cmd -> partSet(cmd, Collections.singletonList(cmd.getSource().getPlayerOrException()), getString(cmd, "set"), true))))
				.then(literal("show")
					.executes(cmd -> showSets(cmd, Collections.singletonList(cmd.getSource().getPlayerOrException())))))
			.then(literal("set-admin")
				.requires(src -> src.hasPermission(2))
				.then(literal("join")
					.then(argument("who", players())
						.then(argument("set", string()).suggests(PlayerSetManager::suggestAllPlayerSets)
							.executes(cmd -> joinSet(cmd, getPlayers(cmd, "who"), getString(cmd, "set"), false)))))
				.then(literal("part")
					.then(argument("who", players())
						.then(argument("set", string()).suggests(PlayerSetManager::suggestAllPlayerSets)
							.executes(cmd -> partSet(cmd, getPlayers(cmd, "who"), getString(cmd, "set"), false)))))
				.then(literal("show-all")
					.executes(Commands::showAllSets))
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
				.requires(src -> src.hasPermission(2))
				.executes(Commands::reloadNow))
		);
	}
	
	//(scaffolding)
	private static void err(CommandContext<CommandSourceStack> cmd, String msg, Object... args) {
		cmd.getSource().sendFailure(lit(msg, args));
	}
	
	private static void personalMsg(CommandContext<CommandSourceStack> cmd, String msg, Object... args) {
		cmd.getSource().sendSuccess(lit(msg, args), false);
	}
	
	private static void msg(CommandContext<CommandSourceStack> cmd, String msg, Object... args) {
		cmd.getSource().sendSuccess(lit(msg, args), true);
	}
	
	private static Component lit(String msg, Object... args) {
		for(int i = 0; i < args.length; i++) if(args[i] instanceof Component) args[i] = ((Component) args[i]).getContents();
		return new TextComponent(String.format(msg, args));
	}
	
	private static @Nullable PlayerSet getSet(CommandContext<CommandSourceStack> cmd, String setName) {
		PlayerSet set = PlayerSetManager.getFor(cmd).get(setName);
		if(set == null) {
			err(cmd, "No set named %s.", setName);
			return null;
		}
		return set;
	}
	
	//Joining, parting
	private static int joinSet(CommandContext<CommandSourceStack> cmd, Collection<ServerPlayer> players, String setName, boolean requireSelfSelect) {
		PlayerSet set = getSet(cmd, setName);
		if(set == null) return 0;
		
		if(requireSelfSelect && !set.isSelfSelect()) {
			err(cmd, "Set %s is not a self-select set, use /apathy set-admin.", set.getName());
			return 0;
		}
		
		int success = 0;
		for(ServerPlayer player : players) {
			if(set.join(player)) {
				msg(cmd, "%s joined set %s.", player.getName(), set.getName());
				success++;
			} else {
				err(cmd, "%s already joined set %s.", player.getName(), set.getName());
			}
		}
		
		return success;
	}
	
	private static int partSet(CommandContext<CommandSourceStack> cmd, Collection<ServerPlayer> players, String setName, boolean requireSelfSelect) {
		PlayerSet set = getSet(cmd, setName);
		if(set == null) return 0;
		
		if(requireSelfSelect && !set.isSelfSelect()) {
			err(cmd, "Set %s is not a self-select set, use /apathy set-admin.", set.getName());
			return 0;
		}
		
		int success = 0;
		for(ServerPlayer player : players) {
			if(set.part(player)) {
				msg(cmd, "%s parted set %s.", player.getName(), set.getName());
				success++;
			} else {
				err(cmd, "%s is already not in set %s.", player.getName(), set.getName());
			}
		}
		
		return success;
	}
	
	//Showing
	private static int showSets(CommandContext<CommandSourceStack> cmd, Collection<ServerPlayer> players) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		
		if(setManager.isEmpty()) {
			err(cmd, "No player sets are available.");
		} else {
			personalMsg(cmd, "The following sets are available: %s", ComponentUtils.formatList(setManager.allSets(), PlayerSet::toLiteralText));
		}
		
		int success = 0;
		
		for(ServerPlayer player : players) {
			Collection<PlayerSet> yea = setManager.allSetsContaining_KindaSlow_DontUseThisOnTheHotPath(player);
			
			if(yea.isEmpty()) {
				err(cmd, "%s is not in any sets.", player.getName());
			} else {
				personalMsg(cmd, "%s is in these sets: %s", player.getName(), ComponentUtils.formatList(yea, PlayerSet::toLiteralText));
				success++;
			}
		}
		
		return success;
	}
	
	private static int showAllSets(CommandContext<CommandSourceStack> cmd) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		
		if(setManager.isEmpty()) {
			err(cmd, "No player sets are available.");
		} else {
			personalMsg(cmd, "The following player sets exist: %s", ComponentUtils.formatList(setManager.allSets(), PlayerSet::toLiteralText));
			PlayerList mgr = cmd.getSource().getServer().getPlayerList();
			
			for(PlayerSet set : setManager.allSets()) {
				personalMsg(cmd, "Set %s contains %s members.", set.getName(), set.members().size());
				for(UUID uuid : set.members()) {
					ServerPlayer player = mgr.getPlayer(uuid);
					personalMsg(cmd, player == null ?
						String.format(" - a currently logged-out player (UUID %s)", uuid) :
						String.format(" - %s (UUID %s)", player.getName().getContents(), uuid));
				}
			}
		}
		return 0;
	}
	
	//create, edit, delete
	private static int createSet(CommandContext<CommandSourceStack> cmd, String name, boolean selfSelect) {
		PlayerSetManager setManager = PlayerSetManager.getFor(cmd);
		if(setManager.hasSet(name)) {
			err(cmd, "Cannot add a new player set named %s, as one already exists with that name.", name);
			return 0;
		}
		
		setManager.createSet(name, selfSelect);
		msg(cmd, selfSelect ? "Added new self-select player set named %s." : "Added new player set named %s.", name);
		return 1;
	}
	
	private static int editSet(CommandContext<CommandSourceStack> cmd, String setName, boolean selfSelect) {
		PlayerSet set = getSet(cmd, setName);
		if(set == null) return 0;
		
		Optional<String> yeayehhehh = Apathy.mobConfig.playerSetName;
		if(yeayehhehh.isPresent() && yeayehhehh.get().equals(set.getName())) {
			err(cmd, "Cannot edit set %s, as its settings would just be reset by the config file.", set.getName());
			return 0;
		}
		
		set.setSelfSelect(selfSelect);
		
		msg(cmd, selfSelect ? "Made set %s a self-select set." : "Made set %s a non-self-select set.", setName);
		return 1;
	}
	
	private static int deleteSet(CommandContext<CommandSourceStack> cmd, String setName) {
		PlayerSet set = getSet(cmd, setName);
		if(set == null) return 0;
		
		Optional<String> yeayehhehh = Apathy.mobConfig.playerSetName;
		if(yeayehhehh.isPresent() && yeayehhehh.get().equals(set.getName())) {
			err(cmd, "Player set %s cannot be deleted because it'd just get recreated by the config file.", set.getName());
			return 0;
		}
		
		PlayerSetManager.getFor(cmd).deleteSet(set.getName());
		msg(cmd, "Player set %s deleted.", set.getName());
		return 1;
	}
	
	private static int reloadNow(CommandContext<CommandSourceStack> cmd) {
		Apathy.reloadNow(cmd.getSource().getServer());
		msg(cmd, "Reloaded Apathy config files. Check the server log for any errors.");
		return 0;
	}
}
