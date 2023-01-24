package me.sirrus86.s86powers.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public final class ComPower extends ComAbstract {

	public ComPower(CommandSender sender, String... args) {
		super(sender, args);
		if (args.length == 1
				|| args[1].equalsIgnoreCase("help")) {
			comPowerHelp(args.length > 2 ? args[2] : null);
		}
		else if (args[1].equalsIgnoreCase("list")) {
			comPowerList(args.length > 2 ? PowerType.valueOf(args[2].toUpperCase()) : null,
					args.length > 3 ? args[3] : (args.length > 2 ? args[2] : null));
		}
		else {
			Power power = config.getPower(args[1]);
			if (power != null) {
				if (args.length == 2
						|| args[2].equalsIgnoreCase("info")) {
					comPowerInfo(power);
				}
				else if (args[2].equalsIgnoreCase("block")
						|| args[2].equalsIgnoreCase("unblock")) {
					comPowerBlock(power, args[2].equalsIgnoreCase("block"));
				}
				else if (args[2].equalsIgnoreCase("disable")
						|| args[2].equalsIgnoreCase("enable")) {
					comPowerEnable(power, args[2].equalsIgnoreCase("enable"));
				}
				else if (args[2].equalsIgnoreCase("kill")) {
					comPowerKill(power);
				}
				else if (args[2].equalsIgnoreCase("lock")
						|| args[2].equalsIgnoreCase("unlock")) {
					comPowerLock(power, args[2].equalsIgnoreCase("lock"));
				}
				else if (args[2].equalsIgnoreCase("option")) {
					comPowerOption(power, args.length > 3 ? args[3] : null,
							args.length > 3 ? power.getOptionByName(args[3]) : null,
							args.length > 4 ? args[4] : null);
				}
				else if (args[2].equalsIgnoreCase("reload")) {
					comPowerReload(power);
				}
				else if (args[2].equalsIgnoreCase("save")) {
					comPowerSave(power);
				}
				else if (args[2].equalsIgnoreCase("stats")) {
					comPowerStats(power, args.length > 3 ? args[3] : null,
							args.length > 3 ? power.getStat(args[3]) : null,
							args.length > 4 ? args[4] : null);
				}
				else if (args[2].equalsIgnoreCase("supply")) {
					comPowerSupply(power, args.length > 3 ? args[3] : null,
							args.length > 4 ? args[4] : null,
							args.length > 5 ? args[5] : null);
				}
				else {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_POWER.build(args[2]));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.UNKNOWN_COMMAND.build(args[1]));
			}
		}
	}
	
	private void comPowerBlock(Power power, boolean isBlock) {
		if (isBlock) {
			if (sender.hasPermission(S86Permission.POWER_BLOCK)) {
				if (config.blockPower(power)) {
					sender.sendMessage(SUCCESS + LocaleString.POWER_BLOCK_SUCCESS.build(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_BLOCK_FAIL.build(power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
		else {
			if (sender.hasPermission(S86Permission.POWER_UNBLOCK)) {
				if (config.unblockPower(power.getTag())) {
					sender.sendMessage(SUCCESS + LocaleString.POWER_UNBLOCK_SUCCESS.build(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_UNBLOCK_FAIL.build(power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
	}
	
	private void comPowerEnable(Power power, boolean isEnable) {
		if (isEnable) {
			if (sender.hasPermission(S86Permission.POWER_ENABLE)) {
				if (!power.isEnabled()) {
					if (power.setEnabled(true)) {
						sender.sendMessage(SUCCESS + LocaleString.POWER_ENABLE_SUCCESS.build(power));
					}
					else {
						sender.sendMessage(ERROR + LocaleString.POWER_ENABLE_FAIL.build(power));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_ALREADY_ENABLED.build(power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
		else {
			if (sender.hasPermission(S86Permission.POWER_DISABLE)) {
				if (power.isEnabled()) {
					if (power.setEnabled(false)) {
						sender.sendMessage(SUCCESS + LocaleString.POWER_DISABLE_SUCCESS.build(power));
					}
					else {
						sender.sendMessage(ERROR + LocaleString.POWER_DISABLE_FAIL.build(power));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_ALREADY_DISABLED.build(power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
	}
	
	private void comPowerHelp(String page) {
		if (sender.hasPermission(S86Permission.POWER_HELP)) {
			PageMaker pm;
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException ignored) {
				}
			}
			pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.POWER, HelpTopic.showHelp(sender, "POWER"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerInfo(Power power) {
		if (sender.hasPermission(S86Permission.POWER_INFO)) {
			sender.sendMessage(ChatColor.GREEN + power.getName() + ChatColor.RESET + " (" + ChatColor.GRAY + power.getTag() + ChatColor.RESET + ")");
			sender.sendMessage(LocaleString.TYPE + ": " + power.getType().getColor() + power.getType().getName() + ChatColor.RESET + ".");
			sender.sendMessage(LocaleString.AUTHOR + ": " + ChatColor.GRAY + power.getAuthor() + ChatColor.RESET + " "
					+ LocaleString.CONCEPT + ": " + ChatColor.GRAY + power.getConcept());
			sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + getPowerDesc(power));
			sender.sendMessage(LocaleString.PLAYERS + ": " + ChatColor.GRAY + getUsers(power) + ".");
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerKill(Power power) {
		if (sender.hasPermission(S86Permission.POWER_KILL)) {
			String pName = power.getName();
			config.removePower(power);
			power.setEnabled(false);
			killPower(power);
			sender.sendMessage(SUCCESS + LocaleString.POWER_KILL_SUCCESS.build(pName));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerList(PowerType type, String page) {
		if (sender.hasPermission(S86Permission.POWER_LIST)) {
			String title = LocaleString.POWERS.toString();
			int i = 1;
			Set<Power> powers;
			if (type != null) {
				title = title + " " + LocaleString.POWERS_BY_TYPE.build(type);
				powers = config.getPowersByType(type);
			}
			else {
				powers = config.getPowers();
			}
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException ignored) {
				}
			}
			if (powers != null) {
				PageMaker pm = new PageMaker(LIST + ChatColor.GREEN + title, getPowerList(powers) + ".", i);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerLock(Power power, boolean isLock) {
		if (isLock) {
			if (sender.hasPermission(S86Permission.POWER_LOCK)) {
				if (!power.isLocked()) {
					power.setLocked(true);
					sender.sendMessage(SUCCESS + LocaleString.POWER_LOCK_SUCCESS.build(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_LOCK_FAIL.build(power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
		else {
			if (sender.hasPermission(S86Permission.POWER_UNLOCK)) {
				if (power.isLocked()) {
					power.setLocked(false);
					sender.sendMessage(SUCCESS + LocaleString.POWER_UNLOCK_SUCCESS.build(power));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_UNLOCK_FAIL.build(power));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
	}
	
	private void comPowerOption(Power power, String page, PowerOption<?> option, String valueStr) {
		if (sender.hasPermission(S86Permission.POWER_OPTION)) {
			if (option != null) {
				if (valueStr != null) {
					Object value = validate(option, valueStr);
					if (!option.isLocked()) {
						if (value != null) {
							if (option.getDefaultValue() instanceof List<?>) {
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
								power.setOption(option, option.getDefaultValue() instanceof Long ? ((Number)value).longValue() : option.getDefaultValue() instanceof Float ? (Float) value : value);
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
					sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.BLUE + (power.getOption(option) instanceof ItemStack ? PowerTools.getItemName((ItemStack) power.getOption(option)) : power.getOption(option).toString()) + ChatColor.RESET + " " + LocaleString.DEFAULT + ": " + ChatColor.GRAY + option.getDefaultValue());
				}
			}
			else if (page != null) {
				try {
					int i = Integer.parseInt(page);
					PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + power.getName() + " " + LocaleString.OPTIONS, getOptions(power), i);
					pm.send(sender);
				} catch (NumberFormatException e) {
					sender.sendMessage(ERROR + LocaleString.POWER_MISSING_OPTION.build(power, page));
				}
			}
			else {
				PageMaker pm = new PageMaker(INFO + ChatColor.GREEN + power.getName() + " " + LocaleString.OPTIONS, getOptions(power), 1);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerReload(Power power) {
		if (sender.hasPermission(S86Permission.POWER_RELOAD)) {
			power.reload();
			sender.sendMessage(SUCCESS + LocaleString.POWER_RELOAD_SUCCESS.build(power));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerSave(Power power) {
		if (sender.hasPermission(S86Permission.POWER_SAVE)) {
			power.saveConfig();
			sender.sendMessage(SUCCESS + LocaleString.POWER_SAVE_SUCCESS.build(power));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerStats(Power power, String page, PowerStat stat, String valueStr) {
		if (sender.hasPermission(S86Permission.POWER_STATS)) {
			if (stat != null) {
				if (valueStr != null) {
					int value = validate(stat, valueStr);
					power.setStatValue(stat, value);
					sender.sendMessage(SUCCESS + LocaleString.SET_STAT_SUCCESS.build(stat.getPath(), value));
				}
				else {
					sender.sendMessage(ChatColor.GREEN + stat.getPath());
					sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + stat.getDescription());
					sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.BLUE + power.getStatValue(stat) + ChatColor.RESET + " " + LocaleString.DEFAULT + ": " + ChatColor.GRAY + stat.getDefaultValue());
				}
			}
			else if (page != null) {
				try {
					int i = Integer.parseInt(page);
					PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + power.getName() + " " + LocaleString.STATS, getStats(power), i);
					pm.send(sender);
				} catch (NumberFormatException e) {
					sender.sendMessage(ERROR + LocaleString.POWER_MISSING_STAT.build(power, page));
				}
			}
			else {
				PageMaker pm = new PageMaker(INFO + ChatColor.GREEN + power.getName() + " " + LocaleString.STATS, getStats(power), 1);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerSupply(Power power, String index, String value, String quantity) {
		if (sender.hasPermission(S86Permission.POWER_SUPPLY)) {
			if (index != null) {
				int i = -1;
				try {
					i = Integer.parseInt(index);
				} catch (Exception e) {
					sender.sendMessage(ERROR + LocaleString.INDEX_MUST_BE_NUMBER);
					sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.POWER_SUPPLY));
				}
				if (i >= 0) {
					if (value != null) {
						if (value.equalsIgnoreCase("null")) {
							power.removeSupply(i);
							sender.sendMessage(SUCCESS + LocaleString.POWER_SUPPLY_REMOVE.build(i));
						}
						else {
							int qty = 1;
							if (quantity != null) {
								try {
									qty = Integer.parseInt(quantity);
								} catch (Exception e) {
									sender.sendMessage(ERROR + LocaleString.QUANTITY_NOT_NUMBER);
									sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.POWER_SUPPLY));
								}
							}
							ItemStack item = createItemStack(value, qty);
							if (item != null) {
								power.setSupply(i, item);
								sender.sendMessage(SUCCESS + LocaleString.POWER_SUPPLY_ADD.build(i, PowerTools.getItemName(item)));
							}
							else {
								sender.sendMessage(ERROR + LocaleString.ITEM_CREATED_INVALID);
							}
						}
					}
					else {
						sender.sendMessage(ERROR + LocaleString.SPECIFY_ITEM_OR_NULL);
						sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.POWER_SUPPLY));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_SUPPLY_NEGATIVE);
				}
			}
			else {
				PageMaker pm = new PageMaker(INFO + ChatColor.GREEN + power.getName() + " " + LocaleString.SUPPLIES, getSupplies(power), 1);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
