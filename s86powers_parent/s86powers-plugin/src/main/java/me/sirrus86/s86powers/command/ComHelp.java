package me.sirrus86.s86powers.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;

public class ComHelp extends ComAbstract {
	
	public ComHelp(CommandSender sender, String... args) {
		super(sender, args);
		comHelp(args.length > 1 ? args[1] : null, args.length > 2 ? args[2] : args.length > 1 ? args[1] : null);
	}
	
	private void comHelp(String topic, String page) {
		if (sender.hasPermission(S86Permission.HELP)) {
			int i = 1, j = 0;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (Exception e) {
					i = 1;
					page = null;
				}
			}
			if (page == null && topic != null) {
				try {
					j = Integer.parseInt(topic);
				} catch (Exception e) {
				}
				if (j != 0) topic = null;
			}
			PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + (topic != null && topic != page ? topic : ""), HelpTopic.showHelp(sender, topic != null && topic != page ? topic.toUpperCase() : ""), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
