package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerAdapter;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.PowerUserAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ComPlayer extends ComAbstract {
	
	public ComPlayer(CommandSender sender, String... args) {
		super(sender, args);
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("help")) {
				comUserHelp(args.length > 2 ? args[2] : null);
			}
			else if (args[1].equalsIgnoreCase("list")) {
				comUserList(args.length > 2 ? args[2] : null);
			}
			else {
				PowerUser user = config.getUser(args[1]);
				if (user != null) {
					PowerUserAdapter uCont = PowerUserAdapter.getAdapter(user);
					if (args.length == 2
							|| args[2].equalsIgnoreCase("info")) {
						comUserInfo(uCont);
					}
					else if (args[2].equalsIgnoreCase("add")) {
						comUserAdd(uCont, args.length > 3 ? (args[3].equalsIgnoreCase("random") ? getRandomPower(uCont) : config.getPower(args[3])) : null);
					}
					else if (args[2].equalsIgnoreCase("clear")) {
						comUserClear(uCont, args.length > 3 ? args[3].toUpperCase() : null);
					}
					else if (args[2].equalsIgnoreCase("remove")) {
						comUserRemove(uCont, args.length > 3 ? config.getPower(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("stat")
							|| args[2].equalsIgnoreCase("stats")) {
						comUserStats(uCont, args.length > 3 ? config.getPower(args[3]) : null,
								args.length > 2 ? PowerAdapter.getAdapter(config.getPower(args[1])).getStat(args[2]) : null,
								args.length > 3 && StringUtils.isNumeric(args[3]) ? Integer.parseInt(args[3]) : -1);
					}
					else if (args[2].equalsIgnoreCase("supply")) {
						comUserSupply(uCont, args.length > 3 ? config.getPower(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("toggle")) {
						comUserToggle(uCont, args.length > 3 ? config.getPower(args[3]) : null);
					}
					else {
						sender.sendMessage(ERROR + LocaleString.UNKNOWN_COMMAND.build(args[2]));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_PLAYER);
				}
			}
		}
		else {
			comUserHelp(null);
		}
	}
	
	private void comUserAdd(PowerUserAdapter user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_ADD)) {
			if (power != null) {
				if (user.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.PLAYER_ALREADY_HAS_POWER.build(power, user.getUser()));
				}
				else if (power.getType() == PowerType.UTILITY) {
					sender.sendMessage(ERROR + LocaleString.PLAYER_ASSIGN_UTILITY);
				}
				else if (!sender.hasPermission("s86powers.admin")
						&& ConfigOption.Users.ENFORCE_POWER_CAP
						&& (user.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE
								|| user.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL)) {
					if (user.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE) {
						if (ConfigOption.Users.REPLACE_POWERS_OF_SAME_TYPE) {
							List<Power> powers = Lists.newArrayList(user.getAssignedPowersByType(power.getType()));
							Collections.shuffle(powers);
							Power removePower = powers.get(0);
							user.removePower(removePower);
							sender.sendMessage(SUCCESS + LocaleString.PLAYER_REMOVE_POWER_SUCCESS.build(user.getUser(), removePower));
							user.addPower(power);
							sender.sendMessage(SUCCESS + LocaleString.PLAYER_ADD_POWER_SUCCESS.build(user.getUser(), power));
						}
						else {
							sender.sendMessage(ERROR + LocaleString.PLAYER_TOO_MANY_POWERS_TYPE.build(user.getUser(), power.getType()));
						}
					}
					else if (user.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL) {
						sender.sendMessage(ERROR + LocaleString.PLAYER_TOO_MANY_POWERS.build(user.getUser()));
					}
				}
				else {
					user.addPower(power);
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_ADD_POWER_SUCCESS.build(user.getUser(), power));
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
	
	private void comUserClear(PowerUserAdapter user, String type) {
		if (sender.hasPermission(S86Permission.PLAYER_CLEAR)) {
			PowerType pType = null;
			if (type != null) {
				try {
					pType = PowerType.valueOf(type);
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_TYPE.build(type));
					return;
				}
			}
			Set<Power> powers = pType != null ? Sets.newHashSet(user.getAssignedPowersByType(pType)) : Sets.newHashSet(user.getAssignedPowers());
			for (Power power : powers) {
				user.removePower(power);
				sender.sendMessage(SUCCESS + LocaleString.PLAYER_REMOVE_POWER_SUCCESS.build(user.getUser(), power));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserHelp(String page) {
		if (sender.hasPermission(S86Permission.PLAYER_HELP)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException e) {
					i = 1;
				}
			}
			PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.PLAYER, HelpTopic.showHelp(sender, "PLAYER"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserInfo(PowerUserAdapter user) {
		if (sender.hasPermission(S86Permission.PLAYER_INFO)) {
			sender.sendMessage(INFO + getUserName(user));
			sender.sendMessage(LocaleString.POWERS + ": " + getPowers(user));
			sender.sendMessage(LocaleString.GROUPS + ": " + getGroups(user));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserList(String page) {
		if (sender.hasPermission(S86Permission.PLAYER_LIST)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (Exception e) {
				}
			}
			PageMaker pm = new PageMaker(LIST + ChatColor.GREEN + LocaleString.PLAYERS, getUsers() + ".", i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserRemove(PowerUserAdapter user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_REMOVE)) {
			if (power != null) {
				if (!user.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.PLAYER_MISSING_POWER.build(user.getUser(), power));
				}
				else {
					user.removePower(power);
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_REMOVE_POWER_SUCCESS.build(user.getUser(), power));
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
	
	private void comUserStats(PowerUserAdapter user, Power power, PowerStat stat, int value) {
		if (sender.hasPermission(S86Permission.PLAYER_STATS)) {
			if (power == null
					|| user.hasPower(power)) {
				if (power != null
						&& stat != null) {
					if (sender.hasPermission(S86Permission.SELF_STATS_SET)) {
						if (value > -1) {
							user.getUser().increaseStat(stat, value - user.getUser().getStatCount(stat));
							sender.sendMessage(SUCCESS + LocaleString.SET_STAT_SUCCESS.build(stat.getPath(), user.getUser().getStatCount(stat)));
						}
						else {
							sender.sendMessage(INFO + getUserName(user) + " " + LocaleString.STATS);
							sender.sendMessage(ChatColor.GREEN + stat.getPath() + ChatColor.RESET + ": " + user.getUser().getStatCount(stat));
						}
					}
					else {
						sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
					}
				}
				else {
					sender.sendMessage(INFO + getUserName(user) + " " + LocaleString.STATS);
					List<Power> powers = power != null ? Lists.newArrayList(power) : new ArrayList<>(user.getPowers());
					Collections.sort(powers);
					String tmp = "";
					for (int i = 0; i < powers.size(); i ++) {
						Power nextPower = powers.get(i);
						List<PowerStat> stats = new ArrayList<PowerStat>(PowerAdapter.getAdapter(nextPower).getStats().keySet());
						Collections.sort(stats);
						if (!stats.isEmpty()) {
							tmp += nextPower.getType().getColor() + nextPower.getName() + "\n";
							for (int j = 0; j < stats.size(); j ++) {
								tmp += ChatColor.RESET + " " + stats.get(j).getDescription() + ": " + user.getUser().getStatCount(stats.get(j)) + "/" + nextPower.getStatValue(stats.get(j)) + "\n";
								tmp += "  " + LocaleString.REWARD + ": " + (!user.getUser().hasStatMaxed(stats.get(j)) ? ChatColor.GRAY : "") + PowerTools.getFilteredText(nextPower, stats.get(j).getReward()) + "\n";
							}
						}
					}
					if (tmp.equalsIgnoreCase("")) {
						tmp = LocaleString.NO_STATS_RECORDED.toString();
					}
					if (tmp.endsWith("\n")) {
						tmp = tmp.substring(0, tmp.lastIndexOf("\n"));
					}
					sender.sendMessage(tmp);
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.PLAYER_MISSING_POWER.build(user.getUser(), power));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserSupply(PowerUserAdapter user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_SUPPLY)) {
			if (user.getUser().isOnline()) {
				if (power != null) {
					supply(user, power);
				}
				else {
					for (Power pwr : user.getPowers()) {
						supply(user, pwr);
					}
				}
				sender.sendMessage(SUCCESS + LocaleString.PLAYER_SUPPLY_SUCCESS.build(user.getUser()));
			}
			else {
				sender.sendMessage(ERROR + LocaleString.PLAYER_NOT_ONLINE.build(user.getUser()));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserToggle(PowerUserAdapter user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_TOGGLE)) {
			if (power != null) {
				if (user.hasPowerAssigned(power)) {
					if (user.hasPowerEnabled(power)) {
						sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWER_DISABLED.build(user.getUser(), power));
					}
					else {
						sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWER_ENABLED.build(user.getUser(), power));
					}
					user.setPowerEnabled(power, !user.hasPowerEnabled(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.PLAYER_MISSING_POWER.build(user.getUser(), power));
				}
			}
			else {
				if (user.hasPowersEnabled()) {
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWERS_DISABLED.build(user.getUser()));
				}
				else {
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWERS_ENABLED.build(user.getUser()));
				}
				user.setPowersEnabled(!user.hasPowersEnabled());
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
