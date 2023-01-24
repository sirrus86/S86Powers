package me.sirrus86.s86powers.command;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.gui.GUIBase;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PowerComExecutor implements CommandExecutor {

	private final static String HEADER = "-----[" + ChatColor.GOLD + "S86 Powers" + ChatColor.RESET + " v" + JavaPlugin.getPlugin(S86Powers.class).getDescription().getVersion() + " by sirrus86]---------------";
	private static String FOOTER;
	
	private final GUIBase gui = new GUIBase();
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command com, @NotNull String string, String[] args) {
		if (ConfigOption.Plugin.USE_GUI
					&& sender instanceof Player
					&& args.length == 0) {
			gui.open((Player) sender);
		}
		else {
			if (ConfigOption.Plugin.SHOW_COMMAND_LINES) {
				FOOTER = createFooter(sender);
				sender.sendMessage(ConfigOption.Plugin.SHOW_COMMAND_HEADER ? HEADER : FOOTER);
			}
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("config")) {
					new ComConfig(sender, args);
				}
				else if (args[0].equalsIgnoreCase("group")) {
					new ComGroup(sender, args);
				}
				else if (args[0].equalsIgnoreCase("help")) {
					new ComHelp(sender, args);
				}
				else if (args[0].equalsIgnoreCase("player")
						|| args[0].equalsIgnoreCase("user")) {
					new ComPlayer(sender, args);
				}
				else if (args[0].equalsIgnoreCase("power")) {
					new ComPower(sender, args);
				}
				else if (args[0].equalsIgnoreCase("region")) {
					new ComRegion(sender, args);
				}
				else {
					new ComSelf(sender, args);
				}
			}
			else {
				new ComHelp(sender, args);
			}
			if (ConfigOption.Plugin.SHOW_COMMAND_LINES) {
				sender.sendMessage(FOOTER);
			}
		}
		return true;
	}
	
	private String createFooter(CommandSender sender) {
		return "-".repeat(Math.max(0, HEADER.length() - (sender instanceof Player ? 7 : 4)));
	}

}
