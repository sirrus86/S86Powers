package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.UserContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

public class ComSelf extends ComAbstract {
	
	private UserContainer uCont;

	public ComSelf(CommandSender sender, String... args) {
		super(sender, args);
		if (sUser != null) {
			uCont = UserContainer.getContainer(sUser);
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("add")) {
					comSelfAdd(args.length > 1 ? config.getPower(args[1]) : null);
				}
				else if (args[0].equalsIgnoreCase("info")) {
					comSelfInfo();
				}
				else if (args[0].equalsIgnoreCase("remove")) {
					comSelfRemove(args.length > 1 ? config.getPower(args[1]) : null);
				}
				else if (args[0].equalsIgnoreCase("stats")) {
					comSelfStats(args.length > 1 ? config.getPower(args[1]) : null,
							args.length > 2 ? PowerContainer.getContainer(config.getPower(args[1])).getStat(args[2]) : null,
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
				if (uCont.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.SELF_ALREADY_HAS_POWER.build(power));
				}
				else if (power.getType() == PowerType.UTILITY) {
					sender.sendMessage(ERROR + LocaleString.SELF_ASSIGN_UTILITY);
				}
				else if (!uCont.isAdmin()
						&& ConfigOption.Users.ENFORCE_POWER_CAP
						&& (uCont.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE
								|| uCont.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL)) {
					if (uCont.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE) {
						sender.sendMessage(ERROR + LocaleString.SELF_TOO_MANY_POWERS_TYPE.build(power.getType()));
					}
					else if (uCont.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL) {
						sender.sendMessage(ERROR + LocaleString.SELF_TOO_MANY_POWERS);
					}
				}
				else {
					uCont.addPower(power);
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
	
	private void comSelfInfo() {
		if (sender.hasPermission(S86Permission.SELF_INFO)) {
			sender.sendMessage(INFO + getUserName(uCont));
			sender.sendMessage(LocaleString.POWERS + ": " + getPowers(uCont));
			sender.sendMessage(LocaleString.GROUPS + ": " + getGroups(uCont));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comSelfRemove(Power power) {
		if (sender.hasPermission(S86Permission.SELF_REMOVE)) {
			if (power != null) {
				if (!uCont.getAssignedPowers().contains(power)) {
					sender.sendMessage(ERROR + LocaleString.SELF_MISSING_POWER.build(power));
				}
				else {
					uCont.removePower(power);
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
					|| uCont.hasPower(power)) {
				if (power != null
						&& stat != null) {
					if (sender.hasPermission(S86Permission.SELF_STATS_SET)) {
						if (value > -1) {
							sUser.increaseStat(stat, value - sUser.getStatCount(stat));
							sender.sendMessage(SUCCESS + LocaleString.SET_STAT_SUCCESS.build(stat.getPath(), sUser.getStatCount(stat)));
						}
						else {
							sender.sendMessage(INFO + getUserName(uCont) + " " + LocaleString.STATS);
							sender.sendMessage(ChatColor.GREEN + stat.getPath() + ChatColor.RESET + ": " + sUser.getStatCount(stat));
						}
					}
					else {
						sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
					}
				}
				else {
					sender.sendMessage(INFO + getUserName(uCont) + " " + LocaleString.STATS);
					List<Power> powers = power != null ? Lists.newArrayList(power) : new ArrayList<>(uCont.getPowers());
					Collections.sort(powers);
					String tmp = "";
					for (int i = 0; i < powers.size(); i ++) {
						Power nextPower = powers.get(i);
						PowerContainer pCont = PowerContainer.getContainer(nextPower);
						List<PowerStat> stats = new ArrayList<PowerStat>(pCont.getStats().keySet());
						Collections.sort(stats);
						if (!stats.isEmpty()) {
							tmp += nextPower.getType().getColor() + nextPower.getName() + "\n";
							for (int j = 0; j < stats.size(); j ++) {
								tmp += ChatColor.RESET + " " + stats.get(j).getDescription() + ": " + sUser.getStatCount(stats.get(j)) + "/" + nextPower.getStatValue(stats.get(j)) + "\n";
								if (ConfigOption.Users.VIEW_INCOMPLETE_STAT_REWARDS
										|| sUser.hasStatMaxed(stats.get(j))) {
									tmp += "  " + LocaleString.REWARD + ": " + (!sUser.hasStatMaxed(stats.get(j)) ? ChatColor.GRAY : "") + pCont.getFilteredText(stats.get(j).getReward()) + "\n";
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
				supply(uCont, power);
			}
			else {
				for (Power pwr : uCont.getPowers()) {
					supply(uCont, pwr);
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
				if (uCont.hasPowerAssigned(power)) {
					if (uCont.hasPowerEnabled(power)) {
						sender.sendMessage(SUCCESS + LocaleString.SELF_POWER_DISABLED.build(power));
					}
					else {
						sender.sendMessage(SUCCESS + LocaleString.SELF_POWER_ENABLED.build(power));
					}
					uCont.setPowerEnabled(power, !uCont.hasPowerEnabled(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.SELF_MISSING_POWER.build(power));
				}
			}
			else {
				if (uCont.hasPowersEnabled()) {
					sender.sendMessage(SUCCESS + LocaleString.SELF_POWERS_DISABLED);
				}
				else {
					sender.sendMessage(SUCCESS + LocaleString.SELF_POWERS_ENABLED);
				}
				uCont.setPowersEnabled(!uCont.hasPowersEnabled());
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
