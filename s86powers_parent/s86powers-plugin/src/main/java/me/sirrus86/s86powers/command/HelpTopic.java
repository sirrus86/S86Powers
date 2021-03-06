package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

public enum HelpTopic {
	
	HELP(S86Permission.HELP, "/powers help [#|topic]"),
	
	SELF_ADD(S86Permission.SELF_ADD, "/powers add <power>"),
	SELF_INFO(S86Permission.SELF_INFO, "/powers info"),
	SELF_OPTION(S86Permission.SELF_OPTION, "/powers option [#|power] [option] [value]"),
	SELF_REMOVE(S86Permission.SELF_REMOVE, "/powers remove <power>"),
	SELF_STATS(S86Permission.SELF_STATS, "/powers stats [power]"),
	SELF_SUPPLY(S86Permission.SELF_SUPPLY, "/powers supply [power]"),
	SELF_TOGGLE(S86Permission.SELF_TOGGLE, "/powers toggle [power]"),

	CONFIG_HELP(S86Permission.CONFIG_HELP, "/powers config help [#]"),
	CONFIG_INFO(S86Permission.CONFIG_INFO, "/powers config info <option>"),
	CONFIG_LIST(S86Permission.CONFIG_LIST, "/powers config list [#]"),
	CONFIG_RELOAD(S86Permission.CONFIG_RELOAD, "/powers config reload"),
	CONFIG_SAVE(S86Permission.CONFIG_SAVE, "/powers config save"),
	CONFIG_SET(S86Permission.CONFIG_SET, "/powers config set <option> <value>"),
	
	GROUP_ADD(S86Permission.GROUP_ADD, "/powers group <group> add <power>"),
	GROUP_ASSIGN(S86Permission.GROUP_ASSIGN, "/powers group <group> assign <player>"),
	GROUP_CREATE(S86Permission.GROUP_CREATE, "/powers group <group> create"),
	GROUP_DELETE(S86Permission.GROUP_DELETE, "/powers group <group> delete"),
	GROUP_HELP(S86Permission.GROUP_HELP, "/powers group help [#]"),
	GROUP_INFO(S86Permission.GROUP_INFO, "/powers group <group> info"),
	GROUP_KICK(S86Permission.GROUP_KICK, "/powers group <group> kick <player>"),
	GROUP_LIST(S86Permission.GROUP_LIST, "/powers group list [#]"),
	GROUP_REMOVE(S86Permission.GROUP_REMOVE, "/powers group <group> remove <power>"),
	
	PLAYER_ADD(S86Permission.PLAYER_ADD, "/powers player <player> add <power>"),
	PLAYER_HELP(S86Permission.PLAYER_HELP, "/powers player help [#]"),
	PLAYER_INFO(S86Permission.PLAYER_INFO, "/powers player <player> info"),
	PLAYER_LIST(S86Permission.PLAYER_LIST, "/powers player list [#]"),
	PLAYER_OPTION(S86Permission.PLAYER_OPTION, "/powers player option [#|power] [option] [value]"),
	PLAYER_REMOVE(S86Permission.PLAYER_REMOVE, "/powers player <player> remove <power>"),
	PLAYER_STATS(S86Permission.PLAYER_STATS, "/powers player <player> stats [power]"),
	PLAYER_SUPPLY(S86Permission.PLAYER_SUPPLY, "/powers player <player> supply [power]"),
	PLAYER_TOGGLE(S86Permission.PLAYER_TOGGLE, "/powers player <player> toggle [power]"),
	
	POWER_BLOCK(S86Permission.POWER_BLOCK, "/powers power <power> block"),
	POWER_DISABLE(S86Permission.POWER_DISABLE, "/powers power <power> disable"),
	POWER_ENABLE(S86Permission.POWER_ENABLE, "/powers power <power> enable"),
	POWER_HELP(S86Permission.POWER_HELP, "/powers power help [#]"),
	POWER_INFO(S86Permission.POWER_INFO, "/powers power <power> info"),
	POWER_KILL(S86Permission.POWER_KILL, "/powers power <power> kill"),
	POWER_LIST(S86Permission.POWER_LIST, "/powers power list [#|type]"),
	POWER_LOCK(S86Permission.POWER_LOCK, "/powers power <power> lock"),
	POWER_OPTION(S86Permission.POWER_OPTION, "/powers power <power> option [option] [value]"),
	POWER_RELOAD(S86Permission.POWER_RELOAD, "/powers power <power> reload"),
	POWER_SAVE(S86Permission.POWER_SAVE, "/powers power <power> save"),
	POWER_STATS(S86Permission.POWER_STATS, "/powers power <power> stats [stat] [value]"),
	POWER_SUPPLY(S86Permission.POWER_SUPPLY, "/powers power <power> supply [#] [item|null] [qty]"),
	POWER_UNBLOCK(S86Permission.POWER_UNBLOCK, "/powers power <power> unblock"),
	POWER_UNLOCK(S86Permission.POWER_UNLOCK, "/powers power <power> unlock"),
	
	REGION_CREATE(S86Permission.REGION_CREATE, "/powers region <region> create [world]"),
	REGION_DELETE(S86Permission.REGION_DELETE, "/powers region <region> delete"),
	REGION_HELP(S86Permission.REGION_HELP, "/powers region help"),
	REGION_INFO(S86Permission.REGION_INFO, "/powers region <region> info"),
	REGION_LIST(S86Permission.REGION_LIST, "/powers region list"),
	REGION_RESIZE(S86Permission.REGION_RESIZE, "/powers region <region> resize <x1> <y1> <z1> <x2> <y2> <z2>"),
	REGION_TOGGLE(S86Permission.REGION_TOGGLE, "/powers region <region> toggle [true|false]");
	
	private final String syntax;
	private final Permission perm;
	
	private HelpTopic(final Permission perm, final String syntax) {
		this.perm = perm;
		this.syntax = syntax;
	}
	
	private final String getDescription() {
		return LocaleString.valueOf(this.name() + "_HELP").toString();
	}
	
	private final Permission getPermission() {
		return perm;
	}
	
	public final String getSyntax() {
		return syntax;
	}
	
	protected static String showHelp(final CommandSender sender, final String topic) {
		String tmp = "";
		for (HelpTopic help : HelpTopic.values()) {
			if (help.toString().startsWith(topic.toUpperCase())
					&& (help.getPermission() != null ? sender.hasPermission(help.getPermission()) : true)) {
				tmp = tmp + ChatColor.AQUA + help.getSyntax() + ChatColor.RESET + " - " + help.getDescription() + "\n";
			}
		}
		return tmp;
	}
	
}
