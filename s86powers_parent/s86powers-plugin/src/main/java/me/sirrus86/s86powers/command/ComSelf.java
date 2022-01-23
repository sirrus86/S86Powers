package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ComSelf extends ComAbstract {

	public ComSelf(CommandSender sender, String... args) {
		super(sender, args);
		if (sUser != null) {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("add")) {
					comSelfAdd(args.length > 1 ? (args[1].equalsIgnoreCase("random") ? getRandomPower(sUser) : config.getPower(args[1])) : null);
				}
				else if (args[0].equalsIgnoreCase("clear")) {
					comSelfClear(args.length > 1 ? args[1].toUpperCase() : null);
				}
				else if (args[0].equalsIgnoreCase("info")) {
					comSelfInfo();
				}
				else if (args[0].equalsIgnoreCase("option")) {
					comSelfOption(args.length > 1 ? config.getPower(args[1]) : null,
							args.length > 2 ? args[2] : null,
							args.length > 2 ? config.getPower(args[1]).getOptionByName(args[2]) : null,
							args.length > 3 ? args[3] : null);
				}
				else if (args[0].equalsIgnoreCase("remove")) {
					comSelfRemove(args.length > 1 ? config.getPower(args[1]) : null);
				}
				else if (args[0].equalsIgnoreCase("stats")) {
					comSelfStats(args.length > 1 ? config.getPower(args[1]) : null,
							args.length > 2 ? config.getPower(args[1]).getStat(args[2]) : null,
							args.length > 3 && StringUtils.isNumeric(args[3]) ? Integer.parseInt(args[3]) : -1);
				}
				else if (args[0].equalsIgnoreCase("supply")) {
					comSelfSupply(args.length > 1 ? config.getPower(args[1]) : null);
				}
				else if (args[0].equalsIgnoreCase("toggle")) {
					comSelfToggle(args.length > 1 ? config.getPower(args[1]) : null);
				}
				else {
					sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
				}
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.SELF_FROM_CONSOLE);
		}
	}
	
	private void comSelfAdd(Power power) {
		if (sender.hasPermission(S86Permission.SELF_ADD)) {
			if (power != null) {
				if (sUser.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.SELF_ALREADY_HAS_POWER.build(power));
				}
				else if (power.getType() == PowerType.UTILITY) {
					sender.sendMessage(ERROR + LocaleString.SELF_ASSIGN_UTILITY);
				}
				else if (!sender.hasPermission(power.getAssignPermission())) {
					sender.sendMessage(ERROR + LocaleString.POWER_ASSIGN_NO_PERMISSION.build(power));
				}
				else if (!sUser.isAdmin()
						&& ConfigOption.Users.ENFORCE_POWER_CAP
						&& (sUser.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE
								|| sUser.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL)) {
					if (sUser.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE) {
						if (ConfigOption.Users.REPLACE_POWERS_OF_SAME_TYPE) {
							List<Power> powers = Lists.newArrayList(sUser.getAssignedPowersByType(power.getType()));
							Collections.shuffle(powers);
							Power removePower = powers.get(0);
							sUser.removePower(removePower);
							sender.sendMessage(SUCCESS + LocaleString.SELF_REMOVE_POWER_SUCCESS.build(removePower));
							sUser.addPower(power);
							sender.sendMessage(SUCCESS + LocaleString.SELF_ADD_POWER_SUCCESS.build(power));
						}
						else {
							sender.sendMessage(ERROR + LocaleString.SELF_TOO_MANY_POWERS_TYPE.build(power.getType()));
						}
					}
					else if (sUser.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL) {
						sender.sendMessage(ERROR + LocaleString.SELF_TOO_MANY_POWERS);
					}
				}
				else {
					sUser.addPower(power);
					sender.sendMessage(SUCCESS + LocaleString.SELF_ADD_POWER_SUCCESS.build(power));
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
	
	private void comSelfClear(String type) {
		if (sender.hasPermission(S86Permission.SELF_CLEAR)) {
			PowerType pType = null;
			if (type != null) {
				try {
					pType = PowerType.valueOf(type);
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_TYPE.build(type));
					return;
				}
			}
			Set<Power> powers = pType != null ? Sets.newHashSet(sUser.getAssignedPowersByType(pType)) : Sets.newHashSet(sUser.getAssignedPowers());
			if (!powers.isEmpty()) {
				for (Power power : powers) {
					sUser.removePower(power);
					sender.sendMessage(SUCCESS + LocaleString.SELF_REMOVE_POWER_SUCCESS.build(power));
				}
			}
			else {
				sender.sendMessage(INFO + LocaleString.SELF_REMOVE_NO_POWERS);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comSelfInfo() {
		if (sender.hasPermission(S86Permission.SELF_INFO)) {
			sender.sendMessage(INFO + getUserName(sUser));
			sender.sendMessage(LocaleString.POWERS + ": " + getPowers(sUser));
			sender.sendMessage(LocaleString.GROUPS + ": " + getGroups(sUser));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private final void comSelfOption(Power power, String page, PowerOption<?> option, String valueStr) {
		if (sender.hasPermission(S86Permission.SELF_OPTION)) {
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
									sUser.setOption(option, option.getDefaultValue() instanceof Long ? (Long) value : value);
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
						sender.sendMessage(ChatColor.GREEN + option.getPath());
						sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + option.getDescription());
						sender.sendMessage(LocaleString.TYPE + ": " + ChatColor.GRAY + option.getDefaultValue().getClass().getSimpleName());
						sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.BLUE + (sUser.getOptionValue(option) instanceof ItemStack ? PowerTools.getItemName((ItemStack) sUser.getOptionValue(option)) : sUser.getOptionValue(option).toString()) + ChatColor.RESET + " " + LocaleString.DEFAULT + ": " + ChatColor.GRAY + option.getDefaultValue());
					}
				}
				else if (page != null) {
					try {
						int i = Integer.parseInt(page);
						PageMaker pm = new PageMaker(INFO + ChatColor.GOLD + sUser.getName() + " " + ChatColor.GREEN + power.getName() + " " + LocaleString.OPTIONS, getOptions(power, sUser), i);
						pm.send(sender);
					} catch (NumberFormatException e) {
						sender.sendMessage(ERROR + LocaleString.POWER_MISSING_OPTION.build(power, page));
					}
				}
				else {
					PageMaker pm = new PageMaker(INFO + ChatColor.GOLD + sUser.getName() + " " + ChatColor.GREEN + power.getName() + " " + LocaleString.OPTIONS, getOptions(power, sUser), 1);
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
	
	private void comSelfRemove(Power power) {
		if (sender.hasPermission(S86Permission.SELF_REMOVE)) {
			if (power != null) {
				if (!sUser.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.SELF_MISSING_POWER.build(power));
				}
				else {
					sUser.removePower(power);
					sender.sendMessage(SUCCESS + LocaleString.SELF_REMOVE_POWER_SUCCESS.build(power));
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
	
	private void comSelfStats(Power power, PowerStat stat, int value) {
		if (sender.hasPermission(S86Permission.SELF_STATS)) {
			if (power == null
					|| sUser.hasPower(power)) {
				if (power != null
						&& stat != null) {
					if (sender.hasPermission(S86Permission.SELF_STATS_SET)) {
						if (value > -1) {
							sUser.increaseStat(stat, value - sUser.getStatCount(stat));
							sender.sendMessage(SUCCESS + LocaleString.SET_STAT_SUCCESS.build(stat.getPath(), sUser.getStatCount(stat)));
						}
						else {
							sender.sendMessage(INFO + getUserName(sUser) + " " + LocaleString.STATS);
							sender.sendMessage(ChatColor.GREEN + stat.getPath() + ChatColor.RESET + ": " + sUser.getStatCount(stat));
						}
					}
					else {
						sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
					}
				}
				else {
					sender.sendMessage(INFO + getUserName(sUser) + " " + LocaleString.STATS);
					List<Power> powers = power != null ? Lists.newArrayList(power) : new ArrayList<>(sUser.getPowers());
					Collections.sort(powers);
					String tmp = "";
					for (int i = 0; i < powers.size(); i ++) {
						Power nextPower = powers.get(i);
						List<PowerStat> stats = new ArrayList<PowerStat>(power.getStats().keySet());
						Collections.sort(stats);
						if (!stats.isEmpty()) {
							tmp += nextPower.getType().getColor() + nextPower.getName() + "\n";
							for (int j = 0; j < stats.size(); j ++) {
								tmp += ChatColor.RESET + " " + stats.get(j).getDescription() + ": " + sUser.getStatCount(stats.get(j)) + "/" + nextPower.getStatValue(stats.get(j)) + "\n";
								if (ConfigOption.Users.VIEW_INCOMPLETE_STAT_REWARDS
										|| sUser.hasStatMaxed(stats.get(j))) {
									tmp += "  " + LocaleString.REWARD + ": " + (!sUser.hasStatMaxed(stats.get(j)) ? ChatColor.GRAY : "") + PowerTools.getFilteredText(nextPower, stats.get(j).getReward()) + "\n";
								}
								else {
									tmp += "  " + LocaleString.REWARD + ": " + ChatColor.GRAY + "???\n";
								}
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
				sender.sendMessage(ERROR + LocaleString.SELF_MISSING_POWER.build(power));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comSelfSupply(Power power) {
		if (sender.hasPermission(S86Permission.SELF_SUPPLY)) {
			if (power != null) {
				supply(sUser, power);
			}
			else {
				for (Power pwr : sUser.getPowers()) {
					supply(sUser, pwr);
				}
			}
			sender.sendMessage(SUCCESS + LocaleString.SELF_SUPPLY_SUCCESS);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comSelfToggle(Power power) {
		if (sender.hasPermission(S86Permission.SELF_TOGGLE)) {
			if (power != null) {
				if (sUser.hasPowerAssigned(power)) {
					if (sUser.hasPowerEnabled(power)) {
						sender.sendMessage(SUCCESS + LocaleString.SELF_POWER_DISABLED.build(power));
					}
					else {
						sender.sendMessage(SUCCESS + LocaleString.SELF_POWER_ENABLED.build(power));
					}
					sUser.setPowerEnabled(power, !sUser.hasPowerEnabled(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.SELF_MISSING_POWER.build(power));
				}
			}
			else {
				if (sUser.hasPowersEnabled()) {
					sender.sendMessage(SUCCESS + LocaleString.SELF_POWERS_DISABLED);
				}
				else {
					sender.sendMessage(SUCCESS + LocaleString.SELF_POWERS_ENABLED);
				}
				sUser.setPowersEnabled(!sUser.hasPowersEnabled());
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
