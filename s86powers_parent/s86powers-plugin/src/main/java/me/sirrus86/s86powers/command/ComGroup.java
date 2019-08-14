package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class ComGroup extends ComAbstract {
	
	public ComGroup(CommandSender sender, String... args) {
		super(sender, args);
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("help")) {
				comGroupHelp(args.length > 2 ? args[2] : null);
			}
			else if (args[1].equalsIgnoreCase("list")) {
				comGroupList(args.length > 2 ? args[2] : null);
			}
			else {
				PowerGroup group = config.getGroup(args[1]);
				if (group != null
						|| (args.length > 2 && args[2].equalsIgnoreCase("create"))) {
					if (args.length == 2
							|| args[2].equalsIgnoreCase("info")) {
						comGroupInfo(group);
					}
					else if (args[2].equalsIgnoreCase("add")) {
						comGroupAdd(group, args.length > 3 ? (args[3].equalsIgnoreCase("random") ? getRandomPower(group) : config.getPower(args[3])) : null);
					}
					else if (args[2].equalsIgnoreCase("assign")) {
						comGroupAssign(group, args.length > 3 ? config.getUser(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("create")) {
						comGroupCreate(args[1]);
					}
					else if (args[2].equalsIgnoreCase("delete")) {
						comGroupDelete(group);
					}
					else if (args[2].equalsIgnoreCase("kick")) {
						comGroupKick(group, args.length > 3 ? config.getUser(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("remove")) {
						comGroupRemove(group, args.length > 3 ? config.getPower(args[3]) : null);
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_GROUP.build(args[1]));
				}
			}
		}
	}
	
	private void comGroupAdd(PowerGroup group, Power power) {
		if (sender.hasPermission(S86Permission.GROUP_ADD)) {
			if (power != null) {
				if (power.getType() == PowerType.UTILITY) {
					sender.sendMessage(ERROR + LocaleString.GROUP_ASSIGN_UTILITY);
				}
				else if (group.hasPower(power)) {
					sender.sendMessage(ERROR + LocaleString.GROUP_ALREADY_HAS_POWER.build(group, power));
				}
				else {
					group.addPower(power);
					sender.sendMessage(SUCCESS + LocaleString.GROUP_ADD_POWER_SUCCESS.build(group, power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.UNKNOWN_POWER);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupAssign(PowerGroup group, PowerUser user) {
		if (sender.hasPermission(S86Permission.GROUP_ASSIGN)) {
			if (user != null) {
				if (!group.hasMember(user)) {
					group.addMember(user);
					sender.sendMessage(SUCCESS + LocaleString.GROUP_ADD_PLAYER_SUCCESS.build(group, user));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.GROUP_ALREADY_HAS_PLAYER.build(group, user));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.UNKNOWN_PLAYER);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupCreate(String name) {
		if (sender.hasPermission(S86Permission.GROUP_CREATE)) {
			if (name != null) {
				if (config.getGroup(name) == null) {
					PowerGroup group = new PowerGroup(name);
					config.addGroup(group);
					sender.sendMessage(SUCCESS + LocaleString.GROUP_CREATE_SUCCESS.build(group));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.GROUP_ALREADY_EXISTS.build(name));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.GROUP_MISSING_NAME);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupDelete(PowerGroup group) {
		if (sender.hasPermission(S86Permission.GROUP_DELETE)) {
			Bukkit.getServer().getPluginManager().removePermission(group.getRequiredPermission());
			config.removeGroup(group);
			sender.sendMessage(SUCCESS + LocaleString.GROUP_DELETE_SUCCESS.build(group));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupHelp(String page) {
		if (sender.hasPermission(S86Permission.GROUP_HELP)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException e) {
					i = 1;
				}
			}
			PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.GROUP, HelpTopic.showHelp(sender, "GROUP"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupInfo(PowerGroup group) {
		if (sender.hasPermission(S86Permission.GROUP_INFO)) {
			sender.sendMessage(ChatColor.GREEN + group.getName() + ChatColor.RESET);
			sender.sendMessage(LocaleString.POWERS + ": " + getPowers(group) + ChatColor.RESET + ".");
			sender.sendMessage(LocaleString.PLAYERS + ": " + ChatColor.GRAY + getUsers(group) + ".");
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupKick(PowerGroup group, PowerUser user) {
		if (sender.hasPermission(S86Permission.GROUP_KICK)) {
			if (user != null) {
				if (group.hasMember(user)) {
					group.removeMember(user);
					sender.sendMessage(SUCCESS + LocaleString.GROUP_REMOVE_PLAYER_SUCCESS.build(group, user));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.GROUP_MISSING_PLAYER.build(group, user));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.UNKNOWN_PLAYER);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupList(String page) {
		if (sender.hasPermission(S86Permission.GROUP_LIST)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (Exception e) {
				}
			}
			PageMaker pm = new PageMaker(LIST + ChatColor.GREEN + LocaleString.GROUPS, getGroups() + ".", i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comGroupRemove(PowerGroup group, Power power) {
		if (sender.hasPermission(S86Permission.GROUP_REMOVE)) {
			if (power != null) {
				if (group.hasPower(power)) {
					group.removePower(power);
					sender.sendMessage(SUCCESS + LocaleString.GROUP_REMOVE_POWER_SUCCESS.build(group, power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.GROUP_MISSING_POWER.build(group, power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.UNKNOWN_POWER);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
