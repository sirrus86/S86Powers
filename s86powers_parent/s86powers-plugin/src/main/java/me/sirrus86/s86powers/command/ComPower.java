package me.sirrus86.s86powers.command;

import java.util.Set;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class ComPower extends ComAbstract {

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
				PowerContainer pCont = PowerContainer.getContainer(power);
				if (args.length <= 2
						|| args[2].equalsIgnoreCase("info")) {
					comPowerInfo(pCont);
				}
				else if (args[2].equalsIgnoreCase("block")
						|| args[2].equalsIgnoreCase("unblock")) {
					comPowerBlock(pCont, args[2].equalsIgnoreCase("block"));
				}
				else if (args[2].equalsIgnoreCase("disable")
						|| args[2].equalsIgnoreCase("enable")) {
					comPowerEnable(pCont, args[2].equalsIgnoreCase("enable"));
				}
				else if (args[2].equalsIgnoreCase("kill")) {
					comPowerKill(pCont);
				}
				else if (args[2].equalsIgnoreCase("lock")
						|| args[2].equalsIgnoreCase("unlock")) {
					comPowerLock(pCont, args[2].equalsIgnoreCase("lock"));
				}
				else if (args[2].equalsIgnoreCase("option")) {
					comPowerOption(pCont, args.length > 3 ? args[3] : null,
							args.length > 3 ? pCont.getOption(args[3]) : null,
							args.length > 4 ? args[4] : null);
				}
				else if (args[2].equalsIgnoreCase("reload")) {
					comPowerReload(pCont);
				}
				else if (args[2].equalsIgnoreCase("save")) {
					comPowerSave(pCont);
				}
				else if (args[2].equalsIgnoreCase("stats")) {
					comPowerStats(pCont, args.length > 3 ? args[3] : null,
							args.length > 3 ? pCont.getStat(args[3]) : null,
							args.length > 4 ? args[4] : null);
				}
				else if (args[2].equalsIgnoreCase("supply")) {
					comPowerSupply(pCont, args.length > 3 ? args[3] : null,
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
	
	private void comPowerBlock(PowerContainer power, boolean isBlock) {
		if (isBlock) {
			if (sender.hasPermission(S86Permission.POWER_BLOCK)) {
				if (config.blockPower(power.getPower())) {
					sender.sendMessage(SUCCESS + LocaleString.POWER_BLOCK_SUCCESS.build(power.getPower()));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_BLOCK_FAIL.build(power.getPower()));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
		else {
			if (sender.hasPermission(S86Permission.POWER_UNBLOCK)) {
				if (config.unblockPower(power.getTag())) {
					sender.sendMessage(SUCCESS + LocaleString.POWER_UNBLOCK_SUCCESS.build(power.getPower()));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_UNBLOCK_FAIL.build(power.getPower()));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
	}
	
	private void comPowerEnable(PowerContainer power, boolean isEnable) {
		if (isEnable) {
			if (sender.hasPermission(S86Permission.POWER_ENABLE)) {
				if (!power.isEnabled()) {
					if (power.setEnabled(true)) {
						sender.sendMessage(SUCCESS + LocaleString.POWER_ENABLE_SUCCESS.build(power.getPower()));
					}
					else {
						sender.sendMessage(ERROR + LocaleString.POWER_ENABLE_FAIL.build(power.getPower()));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_ALREADY_ENABLED.build(power.getPower()));
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
						sender.sendMessage(SUCCESS + LocaleString.POWER_DISABLE_SUCCESS.build(power.getPower()));
					}
					else {
						sender.sendMessage(ERROR + LocaleString.POWER_DISABLE_FAIL.build(power.getPower()));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_ALREADY_DISABLED.build(power.getPower()));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
	}
	
	private void comPowerHelp(String page) {
		if (sender.hasPermission(S86Permission.POWER_HELP)) {
			PageMaker pm = null;
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException e) {
					i = 1;
				}
			}
			pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.POWER, HelpTopic.showHelp(sender, "POWER"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerInfo(PowerContainer power) {
		if (sender.hasPermission(S86Permission.POWER_INFO)) {
			sender.sendMessage(ChatColor.GREEN + power.getPower().getName() + ChatColor.RESET + " (" + ChatColor.GRAY + power.getTag() + ChatColor.RESET + ")");
			sender.sendMessage(LocaleString.TYPE + ": " + power.getPower().getType().getColor() + power.getPower().getType().getName() + ChatColor.RESET + ".");
			sender.sendMessage(LocaleString.AUTHOR + ": " + ChatColor.GRAY + power.getAuthor() + ChatColor.RESET + " "
					+ LocaleString.CONCEPT + ": " + ChatColor.GRAY + power.getConcept());
			sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + getPowerDesc(power));
			sender.sendMessage(LocaleString.PLAYERS + ": " + ChatColor.GRAY + getUsers(power) + ".");
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerKill(PowerContainer power) {
		if (sender.hasPermission(S86Permission.POWER_KILL)) {
			String pName = power.getPower().getName();
			config.removePower(power.getPower());
			power.setEnabled(false);
			killPower(power.getPower());
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
			Set<Power> powers = null;
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
				} catch (NumberFormatException e) {
					i = 1;
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
	
	private void comPowerLock(PowerContainer power, boolean isLock) {
		if (isLock) {
			if (sender.hasPermission(S86Permission.POWER_LOCK)) {
				if (!power.isLocked()) {
					power.setLocked(true);
					sender.sendMessage(SUCCESS + LocaleString.POWER_LOCK_SUCCESS.build(power.getPower()));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_LOCK_FAIL.build(power.getPower()));
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
					sender.sendMessage(SUCCESS + LocaleString.POWER_UNLOCK_SUCCESS.build(power.getPower()));
				}
				else {
					sender.sendMessage(ERROR + LocaleString.POWER_UNLOCK_FAIL.build(power.getPower()));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
			}
		}
	}
	
	private void comPowerOption(PowerContainer power, String page, PowerOption option, String valueStr) {
		if (sender.hasPermission(S86Permission.POWER_OPTION)) {
			if (option != null) {
				if (valueStr != null) {
					Object value = validate(option, valueStr);
					if (value != null) {
						power.setOption(option, option.getDefaultValue() instanceof Long ? (Long) value : value);
						sender.sendMessage(SUCCESS + LocaleString.SET_OPTION_SUCCESS.build(option.getPath(), value));
					}
					else {
						sender.sendMessage(ERROR + LocaleString.SET_OPTION_FAIL.build(option.getPath(), option.getDefaultValue().getClass(), valueStr));
					}
				}
				else {
					sender.sendMessage(ChatColor.GREEN + option.getPath());
					sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + option.getDescription());
					sender.sendMessage(LocaleString.TYPE + ": " + ChatColor.GRAY + power.getOptionValue(option).getClass().getSimpleName());
					sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.BLUE + (power.getOptionValue(option) instanceof ItemStack ? getItemName((ItemStack) power.getOptionValue(option)) : power.getOptionValue(option).toString()) + ChatColor.RESET + " " + LocaleString.DEFAULT + ": " + ChatColor.GRAY + option.getDefaultValue());
				}
			}
			else if (page != null) {
				try {
					int i = Integer.parseInt(page);
					PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + power.getPower().getName() + " " + LocaleString.OPTIONS, getOptions(power), i);
					pm.send(sender);
				} catch (NumberFormatException e) {
					sender.sendMessage(ERROR + LocaleString.POWER_MISSING_OPTION.build(power, page));
				}
			}
			else {
				PageMaker pm = new PageMaker(INFO + ChatColor.GREEN + power.getPower().getName() + " " + LocaleString.OPTIONS, getOptions(power), 1);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerReload(PowerContainer power) {
		if (sender.hasPermission(S86Permission.POWER_RELOAD)) {
			power.reload();
			sender.sendMessage(SUCCESS + LocaleString.POWER_RELOAD_SUCCESS.build(power.getPower()));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerSave(PowerContainer power) {
		if (sender.hasPermission(S86Permission.POWER_SAVE)) {
			power.getPower().saveConfig();
			sender.sendMessage(SUCCESS + LocaleString.POWER_SAVE_SUCCESS.build(power.getPower()));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerStats(PowerContainer power, String page, PowerStat stat, String valueStr) {
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
					PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + power.getPower().getName() + " " + LocaleString.STATS, getStats(power), i);
					pm.send(sender);
				} catch (NumberFormatException e) {
					sender.sendMessage(ERROR + LocaleString.POWER_MISSING_STAT.build(power, page));
				}
			}
			else {
				PageMaker pm = new PageMaker(INFO + ChatColor.GREEN + power.getPower().getName() + " " + LocaleString.STATS, getStats(power), 1);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comPowerSupply(PowerContainer power, String index, String value, String quantity) {
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
								sender.sendMessage(SUCCESS + LocaleString.POWER_SUPPLY_ADD.build(i, getItemName(item)));
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
				PageMaker pm = new PageMaker(INFO + ChatColor.GREEN + power.getPower().getName() + " " + LocaleString.SUPPLIES, getSupplies(power), 1);
				pm.send(sender);
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
