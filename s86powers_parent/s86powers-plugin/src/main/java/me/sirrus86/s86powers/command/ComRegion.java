package me.sirrus86.s86powers.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.regions.NeutralRegion;

public class ComRegion extends ComAbstract {
	
	public ComRegion(CommandSender sender, String... args) {
		super(sender, args);
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("help")) {
				comRegionHelp(args.length > 2 ? args[2] : null);
			}
			else if (args[1].equalsIgnoreCase("list")) {
				comRegionList(args.length > 2 ? args[2] : null);
			}
			else {
				NeutralRegion region = config.getRegion(args[1]);
				if (region != null
						|| (args.length > 2 && args[2].equalsIgnoreCase("create"))) {
					if (args.length == 2
							|| args[2].equalsIgnoreCase("info")) {
						comRegionInfo(region);
					}
					else if (args[2].equalsIgnoreCase("create")) {
						comRegionCreate(args[1], args.length > 3 ? Bukkit.getServer().getWorld(args[3]) : null);
					}
					else if (args[2].equalsIgnoreCase("delete")) {
						comRegionDelete(region);
					}
					else if (args[2].equalsIgnoreCase("resize")) {
						String[] dimensions = new String[6];
						for (int i = 0; i < 6; i ++) {
							if (args.length > i + 3) {
								dimensions[i] = args[i + 3];
							}
						}
						comRegionResize(region, dimensions);
					}
					else if (args[2].equalsIgnoreCase("toggle")
							&& region != null) {
						comRegionToggle(region, args.length > 3 ? Boolean.parseBoolean(args[3]) : !region.isActive());
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.UNKNOWN_REGION.build(args[1]));
				}
			}
		}
	}
	
	private void comRegionCreate(String name, World world) {
		if (sender.hasPermission(S86Permission.REGION_CREATE)) {
			if (world != null
					|| sender instanceof Player) {
				if (world == null) {
					world = ((Player) sender).getWorld();
				}
				if (name != null) {
					if (config.getRegion(name) == null) {
						NeutralRegion region = new NeutralRegion(name, world);
						config.addRegion(region);
						sender.sendMessage(SUCCESS + LocaleString.REGION_CREATE_SUCCESS.build(region));
					}
					else {
						sender.sendMessage(ERROR + LocaleString.REGION_ALREADY_EXISTS.build(name));
					}
				}
				else {
					sender.sendMessage(ERROR + LocaleString.REGION_MISSING_NAME);
					sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.REGION_CREATE));
				}
			}
			else {
				sender.sendMessage(ERROR + LocaleString.REGION_MISSING_WORLD);
				sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.REGION_CREATE));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comRegionDelete(NeutralRegion region) {
		if (sender.hasPermission(S86Permission.REGION_DELETE)) {
			config.removeRegion(region);
			sender.sendMessage(SUCCESS + LocaleString.REGION_DELETE_SUCCESS.build(region));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comRegionHelp(String page) {
		if (sender.hasPermission(S86Permission.REGION_HELP)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (NumberFormatException ignored) {
				}
			}
			PageMaker pm = new PageMaker(HELP + ChatColor.GREEN + LocaleString.REGION, HelpTopic.showHelp(sender, "REGION"), i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comRegionInfo(NeutralRegion region) {
		if (sender.hasPermission(S86Permission.REGION_INFO)) {
			sender.sendMessage(ChatColor.GREEN + region.getName() + ChatColor.RESET);
			sender.sendMessage(LocaleString.WORLD + ": " + region.getWorld().getName());
			sender.sendMessage(LocaleString.DIMENSIONS + ": " + region.getMinCoords().toString() + " x " + region.getMaxCoords().toString());
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	

	
	private void comRegionList(String page) {
		if (sender.hasPermission(S86Permission.REGION_LIST)) {
			int i = 1;
			if (page != null) {
				try {
					i = Integer.parseInt(page);
				} catch (Exception ignored) {
				}
			}
			PageMaker pm = new PageMaker(LIST + ChatColor.GREEN + LocaleString.REGIONS, getRegions() + ".", i);
			pm.send(sender);
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comRegionToggle(NeutralRegion region, boolean neutral) {
		if (sender.hasPermission(S86Permission.REGION_TOGGLE)) {
			region.setNeutral(neutral);
			if (region.isActive()) {
				sender.sendMessage(SUCCESS + LocaleString.REGION_TOGGLE_ENABLE.build(region));
			}
			else {
				sender.sendMessage(SUCCESS + LocaleString.REGION_TOGGLE_DISABLE.build(region));
			}
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}
	
	private void comRegionResize(NeutralRegion region, String[] dimensions) {
		if (sender.hasPermission(S86Permission.REGION_RESIZE)) {
			double[] coords = new double[6];
			for (int i = 0; i < dimensions.length; i ++) {
				try {
					coords[i] = Double.parseDouble(dimensions[i]);
				} catch(NullPointerException e) {
					sender.sendMessage(ERROR + LocaleString.REGION_NOT_ENOUGH_COORDS);
					sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.REGION_RESIZE));
					return;
				} catch(NumberFormatException e1) {
					sender.sendMessage(ERROR + LocaleString.REGION_COORD_NOT_NUMBER);
					sender.sendMessage(LocaleString.EXPECTED_FORMAT.build(HelpTopic.REGION_RESIZE));
					return;
				}
			}
			region.resize(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
			sender.sendMessage(SUCCESS + LocaleString.REGION_RESIZE_SUCCESS.build(region));
		}
		else {
			sender.sendMessage(ERROR + LocaleString.NO_PERMISSION);
		}
	}

}
