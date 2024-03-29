package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

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
					if (args.length == 2
							|| args[2].equalsIgnoreCase("info")) {
						comUserInfo(user);
					}
					else if (args[2].equalsIgnoreCase("add")) {
						comUserAdd(user, args.length > 3 ? (args[3].equalsIgnoreCase("random") ? getRandomPower(user) : config.getPower(args[3])) : null);
					}
					else if (args[2].equalsIgnoreCase("clear")) {
						comUserClear(user, args.length > 3 ? args[3].toUpperCase() : null);
					}
					else if (args[2].equalsIgnoreCase("give")) {
						comUserGive(user, args.length > 3 ? config.getPower(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("option")) {
						comUserOption(user, args.length > 3 ? config.getPower(args[3]) : null,
								args.length > 4 ? args[4] : null,
								args.length > 4 ? config.getPower(args[3]).getOptionByName(args[4]) : null,
								args.length > 5 ? args[5] : null);
					}
					else if (args[2].equalsIgnoreCase("remove")) {
						comUserRemove(user, args.length > 3 ? config.getPower(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("stat")
							|| args[2].equalsIgnoreCase("stats")) {
						comUserStats(user, args.length > 3 ? config.getPower(args[3]) : null,
								args.length > 4 ? config.getPower(args[3]).getStat(args[4]) : null,
								args.length > 3 && StringUtils.isNumeric(args[3]) ? Integer.parseInt(args[3]) : -1);
					}
					else if (args[2].equalsIgnoreCase("supply")) {
						comUserSupply(user, args.length > 3 ? config.getPower(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("toggle")) {
						comUserToggle(user, args.length > 3 ? config.getPower(args[3]) : null);
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
	
	private void comUserAdd(PowerUser user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_ADD)) {
			if (power != null) {
				if (user.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.PLAYER_ALREADY_HAS_POWER.build(power, user));
				}
				else if (power.getType() == PowerType.UTILITY) {
					sender.sendMessage(ERROR + LocaleString.PLAYER_ASSIGN_UTILITY);
				}
				else if (!sender.hasPermission(power.getAssignPermission())) {
					sender.sendMessage(ERROR + LocaleString.POWER_ASSIGN_NO_PERMISSION.build(power));
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
							sender.sendMessage(SUCCESS + LocaleString.PLAYER_REMOVE_POWER_SUCCESS.build(user, removePower));
							user.addPower(power);
							sender.sendMessage(SUCCESS + LocaleString.PLAYER_ADD_POWER_SUCCESS.build(user, power));
						}
						else {
							sender.sendMessage(ERROR + LocaleString.PLAYER_TOO_MANY_POWERS_TYPE.build(user, power.getType()));
						}
					}
					else if (user.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL) {
						sender.sendMessage(ERROR + LocaleString.PLAYER_TOO_MANY_POWERS.build(user));
					}
				}
				else {
					user.addPower(power);
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_ADD_POWER_SUCCESS.build(user, power));
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
	
	private void comUserClear(PowerUser user, String type) {
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
				sender.sendMessage(SUCCESS + LocaleString.PLAYER_REMOVE_POWER_SUCCESS.build(user, power));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserGive(PowerUser user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_CLEAR)) {
			if (user.isOnline()) {
				if (power != null) {
					ItemStack book = PowerTools.createPowerBook(power);
					Collection<ItemStack> items = user.getPlayer().getInventory().addItem(book).values();
					for (ItemStack item : items) {
						user.getPlayer().getWorld().dropItem(user.getPlayer().getLocation(), item);
					}
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_GAVE_POWER_BOOK.build(user, power));
					user.getPlayer().sendMessage(LocaleString.SELF_RECEIVED_POWER_BOOK.build(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_POWER);
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.PLAYER_NOT_ONLINE.build(user));
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
				} catch (NumberFormatException ignored) {
				}
			}
			PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.PLAYER, HelpTopic.showHelp(sender, "PLAYER"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserInfo(PowerUser user) {
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
				} catch (Exception ignored) {
				}
			}
			PageMaker pm = new PageMaker(LIST + ChatColor.GREEN + LocaleString.PLAYERS, getUsers() + ".", i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserOption(PowerUser user, Power power, String page, PowerOption<?> option, String valueStr) {
		if (sender.hasPermission(S86Permission.PLAYER_OPTION)) {
			if (power != null) {
				if (option != null) {
					if (valueStr != null) {
						Object value = !valueStr.equalsIgnoreCase("null") ? validate(option, valueStr) : valueStr;
						if (!option.isLocked()) {
							if (value != null) {
								if (value.toString().equalsIgnoreCase("null")) {
									sUser.removeOption(option);
									sender.sendMessage(SUCCESS + LocaleString.REMOVE_OPTION_SUCCESS.build(option.getPath()));
								}
								else if (option.getDefaultValue() instanceof List<?>) {
									List<Object> optList = new ArrayList<>((List<?>) power.getOption(option));
									if (optList.contains(value)) {
										optList.remove(value);
										power.setOption(option, optList);
										sender.sendMessage(SUCCESS + LocaleString.SET_OPTION_REMOVE_SUCCESS.build(option.getPath(), value));
									}
									else {
										optList.add(value);
										power.setOption(option, optList);
										sender.sendMessage(SUCCESS + LocaleString.SET_OPTION_ADD_SUCCESS.build(option.getPath(), value));
									}
								}
								else {
									sUser.setOption(option, option.getDefaultValue() instanceof Long ? ((Number)value).longValue() : value);
									sender.sendMessage(SUCCESS + LocaleString.SET_OPTION_SUCCESS.build(option.getPath(), value));
								}
							}
							else {
								if (option.getDefaultValue() instanceof List<?>) {
									sender.sendMessage(ERROR + LocaleString.SET_OPTION_FAIL.build(option.getPath(), ((List<?>)option.getDefaultValue()).get(0).getClass(), valueStr));
								}
								else {
									sender.sendMessage(ERROR + LocaleString.SET_OPTION_FAIL.build(option.getPath(), option.getDefaultValue().getClass(), valueStr));
								}
							}
						}
						else {
							sender.sendMessage(ERROR + LocaleString.SET_OPTION_LOCKED.build(option.getPath(), valueStr));
						}
					}
					else {
						Object value = user.getOptionValue(option);
						sender.sendMessage(ChatColor.GREEN + option.getPath());
						sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + option.getDescription());
						sender.sendMessage(LocaleString.TYPE + ": " + ChatColor.GRAY + option.getDefaultValue().getClass().getSimpleName());
						sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.BLUE + (value == null ? option.getDefaultValue() : value instanceof ItemStack ? PowerTools.getItemName((ItemStack) value) : value.toString()) + ChatColor.RESET + " " + LocaleString.DEFAULT + ": " + ChatColor.GRAY + option.getDefaultValue());
					}
				}
				else if (page != null) {
					try {
						int i = Integer.parseInt(page);
						PageMaker pm = new PageMaker(INFO + ChatColor.GOLD + user.getName() + " " + ChatColor.GREEN + power.getName() + " " + LocaleString.OPTIONS, getOptions(power, user), i);
						pm.send(sender);
					} catch (NumberFormatException e) {
						sender.sendMessage(ERROR + LocaleString.POWER_MISSING_OPTION.build(power, page));
					}
				}
				else {
					PageMaker pm = new PageMaker(INFO + ChatColor.GOLD + user.getName() + " " + ChatColor.GREEN + power.getName() + " " + LocaleString.OPTIONS, getOptions(power, user), 1);
					pm.send(sender);
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
	
	private void comUserRemove(PowerUser user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_REMOVE)) {
			if (power != null) {
				if (!user.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.PLAYER_MISSING_POWER.build(user, power));
				}
				else {
					user.removePower(power);
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_REMOVE_POWER_SUCCESS.build(user, power));
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
	
	private void comUserStats(PowerUser user, Power power, PowerStat stat, int value) {
		if (sender.hasPermission(S86Permission.PLAYER_STATS)) {
			if (power == null
					|| user.hasPower(power)) {
				if (power != null
						&& stat != null) {
					if (sender.hasPermission(S86Permission.SELF_STATS_SET)) {
						if (value > -1) {
							user.increaseStat(stat, value - user.getStatCount(stat));
							sender.sendMessage(SUCCESS + LocaleString.SET_STAT_SUCCESS.build(stat.getPath(), user.getStatCount(stat)));
						}
						else {
							sender.sendMessage(INFO + getUserName(user) + " " + LocaleString.STATS);
							sender.sendMessage(ChatColor.GREEN + stat.getPath() + ChatColor.RESET + ": " + user.getStatCount(stat));
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
					StringBuilder tmp = new StringBuilder();
					for (Power nextPower : powers) {
						List<PowerStat> stats = new ArrayList<>(nextPower.getStats().keySet());
						Collections.sort(stats);
						if (!stats.isEmpty()) {
							tmp.append(nextPower.getType().getColor()).append(nextPower.getName()).append("\n");
							for (PowerStat powerStat : stats) {
								tmp.append(ChatColor.RESET).append(" ").append(powerStat.getDescription()).append(": ").append(user.getStatCount(powerStat)).append("/").append(nextPower.getStatValue(powerStat)).append("\n").append("  ").append(LocaleString.REWARD).append(": ").append(!user.hasStatMaxed(powerStat) ? ChatColor.GRAY : "").append(PowerTools.getFilteredText(nextPower, powerStat.getReward())).append("\n");
							}
						}
					}
					if (tmp.toString().equalsIgnoreCase("")) {
						tmp = new StringBuilder(LocaleString.NO_STATS_RECORDED.toString());
					}
					if (tmp.toString().endsWith("\n")) {
						tmp = new StringBuilder(tmp.substring(0, tmp.lastIndexOf("\n")));
					}
					sender.sendMessage(tmp.toString());
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.PLAYER_MISSING_POWER.build(user, power));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserSupply(PowerUser user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_SUPPLY)) {
			if (user.isOnline()) {
				if (power != null) {
					supply(user, power);
				}
				else {
					for (Power pwr : user.getPowers()) {
						supply(user, pwr);
					}
				}
				sender.sendMessage(SUCCESS + LocaleString.PLAYER_SUPPLY_SUCCESS.build(user));
			}
			else {
				sender.sendMessage(ERROR + LocaleString.PLAYER_NOT_ONLINE.build(user));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comUserToggle(PowerUser user, Power power) {
		if (sender.hasPermission(S86Permission.PLAYER_TOGGLE)) {
			if (power != null) {
				if (user.hasPowerAssigned(power)) {
					if (user.hasPowerEnabled(power)) {
						sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWER_DISABLED.build(user, power));
					}
					else {
						sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWER_ENABLED.build(user, power));
					}
					user.setPowerEnabled(power, !user.hasPowerEnabled(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.PLAYER_MISSING_POWER.build(user, power));
				}
			}
			else {
				if (user.hasPowersEnabled()) {
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWERS_DISABLED.build(user));
				}
				else {
					sender.sendMessage(SUCCESS + LocaleString.PLAYER_POWERS_ENABLED.build(user));
				}
				user.setPowersEnabled(!user.hasPowersEnabled());
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
