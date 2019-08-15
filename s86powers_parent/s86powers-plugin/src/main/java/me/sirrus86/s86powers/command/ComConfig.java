package me.sirrus86.s86powers.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;

public final class ComConfig extends ComAbstract {

	private boolean save = false;
	
	public ComConfig(CommandSender sender, String... args) {
		super(sender, args);
		if (args.length == 1
				|| args[1].equalsIgnoreCase("help")) {
			comConfigHelp(args.length > 2 ? args[2] : null);
		}
		else if (args[1].equalsIgnoreCase("info")) {
			comConfigInfo(args.length > 2 ? args[2] : null);			
		}
		else if (args[1].equalsIgnoreCase("list")) {
			comConfigList(args.length > 2 ? args[2] : null);
		}
		else if (args[1].equalsIgnoreCase("reload")) {
			comConfigReload();
		}
		else if (args[1].equalsIgnoreCase("save")) {
			comConfigSave();
		}
		else if (args[1].equalsIgnoreCase("set")) {
			comConfigSet(args.length > 2 ? args[2] : null, args.length > 3 ? args[3] : null);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.UNKNOWN_COMMAND.build(args[1]));
		}
		if (save) {
			config.savePluginConfig();
		}
	}
	
	private final void comConfigHelp(String page) {
		if (sender.hasPermission(S86Permission.CONFIG_HELP)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException e) {
					i = 1;
				}
			}
			PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.CONFIG, HelpTopic.showHelp(sender, "CONFIG"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private final void comConfigInfo(String option) {
		if (sender.hasPermission(S86Permission.CONFIG_INFO)) {
			if (option != null
					&& config.getConfigOptions().containsKey(option)) {
				Object value = config.getConfigValue(option);
				sender.sendMessage(INFO + ChatColor.GREEN + option);
				sender.sendMessage(LocaleString.DESCRIPTION + ": " + ChatColor.GRAY + LocaleString.getString(option.substring(option.indexOf(".") + 1).toUpperCase().replaceAll("-", "_") + "_CONFIG"));
				try {
					sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.BLUE + value.toString());
				} catch (Exception e) {
					sender.sendMessage(LocaleString.VALUE + ": " + ChatColor.GRAY + LocaleString.VIEW_VALUE_FAIL);
					e.printStackTrace();
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.MUST_SPECIFY_OPTION);
				sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.CONFIG_INFO));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private final void comConfigList(String page) {
		if (sender.hasPermission(S86Permission.CONFIG_LIST)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException e) {
					i = 1;
				}
			}
			PageMaker pm = new PageMaker(LIST + ChatColor.GREEN + "Config", optList(), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private final void comConfigReload() {
		if (sender.hasPermission(S86Permission.CONFIG_RELOAD)) {
			config.loadPluginConfig();
			sender.sendMessage(SUCCESS + LocaleString.CONFIG_RELOADED);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private final void comConfigSave() {
		if (sender.hasPermission(S86Permission.CONFIG_SAVE)) {
			config.savePluginConfig();
			sender.sendMessage(SUCCESS + LocaleString.CONFIG_SAVED);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private final void comConfigSet(String option, Object value) {
		if (sender.hasPermission(S86Permission.CONFIG_SET)) {
			if (option != null
					&& config.getConfigOptions().containsKey(option)) {
				if (value != null) {
					if (config.setConfigValue(option, value)) {
						sender.sendMessage(SUCCESS + LocaleString.SET_OPTION_SUCCESS.build(option, value));
					}
					else {
						Object oldValue = config.getConfigValue(option);
						sender.sendMessage(ERROR + LocaleString.VALUE_WRONG_TYPE.build(oldValue.getClass().getSimpleName()));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.MUST_SPECIFY_VALUE);
					sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.CONFIG_SET));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.UNKNOWN_OPTION.build(option));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
}
