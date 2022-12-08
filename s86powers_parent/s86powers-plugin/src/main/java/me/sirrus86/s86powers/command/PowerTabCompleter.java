package me.sirrus86.s86powers.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;

public final class PowerTabCompleter implements TabCompleter {

	private static final List<String> BASE = Lists.newArrayList("add", "clear", "config", "give", "group", "help", "info", "option", "player", "power", "remove", "stats", "supply", "toggle"),
			CONFIG = Lists.newArrayList("info", "list", "reload", "save", "set"),
			GROUP = Lists.newArrayList("add", "assign", "create", "delete", "info", "kick", "remove"),
			POWER = Lists.newArrayList("block", "disable", "enable", "info", "kill", "lock", "option", "reload", "save", "stats", "supply", "unblock", "unlock"),
			PLAYER = Lists.newArrayList("add", "clear", "give", "info", "option", "remove", "stats", "supply", "toggle"),
			REGION = Lists.newArrayList("create", "delete", "info", "resize", "toggle");
	
	private static final List<String> empty = new ArrayList<>();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length > 0) {
			if (args.length > 1) {
				if (args[0].equalsIgnoreCase("config")
						&& sender.hasPermission(S86Permission.CONFIG)) {
					if (args.length > 2) {
						if ((args[1].equalsIgnoreCase("info") && sender.hasPermission(S86Permission.CONFIG_INFO))
								|| (args[1].equalsIgnoreCase("set") && sender.hasPermission(S86Permission.CONFIG_SET))) {
							return StringUtil.copyPartialMatches(args[2], Lists.newArrayList(S86Powers.getConfigManager().getConfigOptions().keySet()), new ArrayList<>());
						}
						else if (args.length > 3) {
							return empty;
						}
					}
					else if (args.length <= 2) {
						return StringUtil.copyPartialMatches(args[1], getList(sender, "config", CONFIG), new ArrayList<>());
					}
				}
				else if (args[0].equalsIgnoreCase("group")
						&& sender.hasPermission(S86Permission.GROUP)) {
					if (args.length > 2) {
						if (args.length > 3) {
							if (args[2].equalsIgnoreCase("add")
									&& sender.hasPermission(S86Permission.GROUP_ADD)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getPowerList(false), new ArrayList<>());
							}
							else if (args[2].equalsIgnoreCase("assign")
									&& sender.hasPermission(S86Permission.GROUP_ASSIGN)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getPlayerList(false), new ArrayList<>());
							}
							else if (args[2].equalsIgnoreCase("remove")
									&& sender.hasPermission(S86Permission.GROUP_REMOVE)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getGroupPowerList(args[2]), new ArrayList<>());
							}
							else if (args[2].equalsIgnoreCase("kick")
									&& sender.hasPermission(S86Permission.GROUP_KICK)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getGroupUserList(args[2]), new ArrayList<>());
							}
						}
						else if (args.length <= 3) {
							return StringUtil.copyPartialMatches(args[2], getList(sender, "group", GROUP), new ArrayList<>());
						}
					}
					else if (args.length <= 2) {
						return StringUtil.copyPartialMatches(args[1], getGroupList(true), new ArrayList<>());
					}
				}
				else if (args[0].equalsIgnoreCase("player")
						&& sender.hasPermission(S86Permission.PLAYER)) {
					if (args.length > 2) {
						if (args.length > 3) {
							if (args[2].equalsIgnoreCase("add")
									&& sender.hasPermission(S86Permission.PLAYER_ADD)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getPowerList(false), new ArrayList<>());
							}
							else if (args[2].equalsIgnoreCase("clear")
									&& sender.hasPermission(S86Permission.PLAYER_CLEAR)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], Lists.newArrayList("DEFENSE", "OFFENSE", "PASSIVE"), new ArrayList<>());
							}
							else if (args[2].equalsIgnoreCase("give")
									&& sender.hasPermission(S86Permission.PLAYER_GIVE)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getPowerList(false), new ArrayList<>());
							}
							else if ((args[2].equalsIgnoreCase("option") && sender.hasPermission(S86Permission.PLAYER_OPTION))
									|| (args[2].equalsIgnoreCase("remove") && sender.hasPermission(S86Permission.PLAYER_REMOVE))
									|| (args[2].equalsIgnoreCase("stats") && sender.hasPermission(S86Permission.PLAYER_STATS))
									|| (args[2].equalsIgnoreCase("supply") && sender.hasPermission(S86Permission.PLAYER_SUPPLY))
									|| (args[2].equalsIgnoreCase("toggle") && sender.hasPermission(S86Permission.PLAYER_TOGGLE))) {
								if (args.length > 4
										&& args[2].equalsIgnoreCase("option")
										&& sender.hasPermission(S86Permission.PLAYER_OPTION)
										&& args.length <= 5) {
									return StringUtil.copyPartialMatches(args[4], getPlayerOptList(args[1], args[3]), new ArrayList<>());
								}
								else if (args.length > 4
										&& args[2].equalsIgnoreCase("stats")
										&& sender.hasPermission(S86Permission.PLAYER_STATS_SET)
										&& args.length <= 5) {
									return StringUtil.copyPartialMatches(args[4], getPlayerStatList(args[1], args[3]), new ArrayList<>());
								}
								else if (args.length <= 4) {
									return StringUtil.copyPartialMatches(args[3], getPlayerPowerList(args[1]), new ArrayList<>());
								}
							}
						}
						else if (args.length <= 3) {
							return StringUtil.copyPartialMatches(args[2], getList(sender, "player", PLAYER), new ArrayList<>());
						}
					}
					else if (args.length <= 2) {
						return StringUtil.copyPartialMatches(args[1], getPlayerList(true), new ArrayList<>());
					}
				}
				else if (args[0].equalsIgnoreCase("power")
						&& sender.hasPermission(S86Permission.POWER)) {
					if (args.length > 2) {
						if (args[1].equalsIgnoreCase("list")
								&& sender.hasPermission(S86Permission.POWER_LIST)
								&& args.length <= 3) {
							return StringUtil.copyPartialMatches(args[2], Lists.newArrayList("defense", "offense", "passive", "utility"), new ArrayList<>());
						}
						else if (args.length > 3) {
							if (args[2].equalsIgnoreCase("option")
									&& sender.hasPermission(S86Permission.POWER_OPTION)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getPowerOptionList(args[1]), new ArrayList<>());
							}
							else if (args[2].equalsIgnoreCase("stats")
									&& sender.hasPermission(S86Permission.POWER_STATS)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getPowerStatList(args[1]), new ArrayList<>());
							}
						}
						else if (args.length <= 3) {
							return StringUtil.copyPartialMatches(args[2], getList(sender, "power", POWER), new ArrayList<>());
						}
					}
					else if (args.length <= 2) {
						return StringUtil.copyPartialMatches(args[1], getPowerList(true), new ArrayList<>());
					}
				}
				else if (args[0].equalsIgnoreCase("region")
						&& sender.hasPermission(S86Permission.REGION)) {
					if (args.length > 2) {
						if (args.length > 3) {
							if (args[2].equalsIgnoreCase("create")
									&& sender.hasPermission(S86Permission.REGION_CREATE)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], getWorldList(), new ArrayList<>());
							}
							if (args[2].equalsIgnoreCase("toggle")
									&& sender.hasPermission(S86Permission.REGION_TOGGLE)
									&& args.length <= 4) {
								return StringUtil.copyPartialMatches(args[3], Lists.newArrayList("true", "false"), new ArrayList<>());
							}
						}
						else if (args.length <= 3) {
							return StringUtil.copyPartialMatches(args[2], getList(sender, "region", REGION), new ArrayList<>());
						}
					}
					else if (args.length <= 2){
						return StringUtil.copyPartialMatches(args[1], getRegionList(true), new ArrayList<>());
					}
				}
				else if (args[0].equalsIgnoreCase("add")
						&& sender.hasPermission(S86Permission.SELF_ADD)
						&& args.length <= 2) {
					return StringUtil.copyPartialMatches(args[1], getPowerList(false), new ArrayList<>());
				}
				else if (args[0].equalsIgnoreCase("clear")
						&& sender.hasPermission(S86Permission.SELF_CLEAR)
						&& args.length <= 2) {
					return StringUtil.copyPartialMatches(args[1], Lists.newArrayList("DEFENSE", "OFFENSE", "PASSIVE"), new ArrayList<>());
				}
				else if (args[0].equalsIgnoreCase("give")
						&& sender.hasPermission(S86Permission.SELF_GIVE)
						&& args.length <= 2) {
					return StringUtil.copyPartialMatches(args[1], getPowerList(false), new ArrayList<>());
				}
				else if ((args[0].equalsIgnoreCase("option") && sender.hasPermission(S86Permission.SELF_OPTION))
						|| (args[0].equalsIgnoreCase("remove") && sender.hasPermission(S86Permission.SELF_REMOVE))
						|| (args[0].equalsIgnoreCase("stats") && sender.hasPermission(S86Permission.SELF_STATS))
						|| (args[0].equalsIgnoreCase("supply") && sender.hasPermission(S86Permission.SELF_SUPPLY))
						|| (args[0].equalsIgnoreCase("toggle") && sender.hasPermission(S86Permission.SELF_TOGGLE))) {
					if (args.length > 2
							&& args[0].equalsIgnoreCase("option")
							&& sender.hasPermission(S86Permission.SELF_OPTION)
							&& args.length <= 3) {
						return StringUtil.copyPartialMatches(args[2], getPlayerOptList(sender.getName(), args[1]), new ArrayList<>());
					}
					else if (args.length > 2
							&& args[0].equalsIgnoreCase("stats")
							&& sender.hasPermission(S86Permission.SELF_STATS_SET)
							&& args.length <= 3) {
						return StringUtil.copyPartialMatches(args[2], getPlayerStatList(sender.getName(), args[1]), new ArrayList<>());
					}
					else if (args.length <= 3){
						return StringUtil.copyPartialMatches(args[1], getPlayerPowerList(sender.getName()), new ArrayList<>());
					}
				}
			}
			else if (args.length <= 1) {
				List<String> newList = BASE;
				for (int i = 0; i < BASE.size(); i ++) {
					String com = BASE.get(i);
					switch(com) {
						case "add": case "info": case "remove": case "option":  case "stats": case "supply": case "toggle": {
							if (!(sender instanceof Player)
									|| !sender.hasPermission("s86powers.manage.self")
									|| !sender.hasPermission("s86powers.manage.self." + com)) {
								newList.remove(com);
							}
							break;
						}
						default: {
							if (!sender.hasPermission("s86powers.manage." + com)) {
								newList.remove(com);
							}
							break;
						}
					}
				}
				return StringUtil.copyPartialMatches(args[0], newList, new ArrayList<>());
			}
		}
		return empty;		
	}
	
	private List<String> getList(CommandSender sender, String prefix, List<String> commands) {
		List<String> newList = commands;
		for (int i = 0; i < commands.size(); i ++) {
			if (!sender.hasPermission("s86powers.manage." + prefix + "." + commands.get(i))) {
				newList.remove(commands.get(i));
			}
		}
		return newList.isEmpty() ? null : newList;
	}
	
	private List<String> getGroupList(boolean isGroupCommand) {
		List<String> newList = new ArrayList<>();
		for (PowerGroup group : S86Powers.getConfigManager().getGroups()) {
			newList.add(group.getName());
		}
		Collections.sort(newList);
		if (isGroupCommand) {
			newList.add(0, "help");
			newList.add(1, "list");
		}
		return newList;
	}
	
	private List<String> getGroupPowerList(String name) {
		PowerGroup group = S86Powers.getConfigManager().getGroup(name);
		List<String> pList = new ArrayList<>();
		if (group != null) {
			for (Power power : group.getPowers()) {
				pList.add(power.getTag());
			}
			Collections.sort(pList);
		}
		return pList;
	}
	
	private List<String> getGroupUserList(String name) {
		PowerGroup group = S86Powers.getConfigManager().getGroup(name);
		List<String> pList = new ArrayList<>();
		if (group != null) {
			for (PowerUser user : group.getMembers()) {
				if (user.getName() != null) {
					pList.add(user.getName());
				}
			}
			Collections.sort(pList);
		}
		return pList;
	}
	
	private List<String> getPlayerList(boolean isUserCommand) {
		List<String> newList = new ArrayList<>();
		for (PowerUser user : S86Powers.getConfigManager().getUserList()) {
			if (user.getName() != null) {
				newList.add(user.getName());
			}
		}
		Collections.sort(newList);
		if (isUserCommand) {
			newList.add(0, "help");
			newList.add(1, "list");
		}
		return newList;
	}
	
	private List<String> getPlayerOptList(String player, String pName) {
		PowerUser user = S86Powers.getConfigManager().getUser(player);
		Power power = S86Powers.getConfigManager().getPower(pName);
		List<String> optList = new ArrayList<>();
		if (user != null
				&& power != null) {
			for (PowerOption<?> option : power.getOptions().keySet()) {
				optList.add(option.getPath());
			}
		}
		return optList;
	}
	
	private List<String> getPlayerPowerList(String player) {
		PowerUser user = S86Powers.getConfigManager().getUser(player);
		List<String> pList = new ArrayList<>();
		if (user != null) {
			for (Power power : user.getPowers()) {
				pList.add(power.getTag());
			}
			Collections.sort(pList);
		}
		return pList;
	}
	
	private List<String> getPlayerStatList(String player, String pName) {
		PowerUser user = S86Powers.getConfigManager().getUser(player);
		Power power = S86Powers.getConfigManager().getPower(pName);
		List<String> statList = new ArrayList<>();
		if (user != null
				&& power != null) {
			for (PowerStat stat : power.getStats().keySet()) {
				statList.add(stat.getPath());
			}
		}
		return statList;
	}
	
	private List<String> getPowerOptionList(String name) {
		List<String> optionList = new ArrayList<>();
		Power power = S86Powers.getConfigManager().getPower(name);
		if (power != null) {
			for (PowerOption<?> option : power.getOptions().keySet()) {
				optionList.add(option.getPath());
			}
		}
		return optionList;
	}
	
	private List<String> getPowerList(boolean isPowerCommand) {
		List<String> newList = new ArrayList<>();
		for (Power power : S86Powers.getConfigManager().getPowers()) {
			if (power.getType() != PowerType.UTILITY
					|| isPowerCommand) {
				newList.add(power.getTag());
			}
		}
		Collections.sort(newList);
		if (isPowerCommand) {
			newList.add(0, "help");
			newList.add(1, "list");
		}
		else {
			newList.add(0, "random");
		}
		return newList;
	}
	
	private List<String> getPowerStatList(String name) {
		List<String> statList = new ArrayList<>();
		Power power = S86Powers.getConfigManager().getPower(name);
		if (power != null) {
			for (PowerStat stat : power.getStats().keySet()) {
				statList.add(stat.getPath());
			}
		}
		return statList;
	}
	
	private List<String> getRegionList(boolean isRegionCommand) {
		List<String> newList = new ArrayList<>();
		for (NeutralRegion region : S86Powers.getConfigManager().getRegions()) {
			newList.add(region.getName());
		}
		Collections.sort(newList);
		if (isRegionCommand) {
			newList.add(0, "help");
			newList.add(1, "list");
		}
		return newList;
	}
	
	private List<String> getWorldList() {
		List<String> wList = new ArrayList<>();
		for (World world : Bukkit.getServer().getWorlds()) {
			wList.add(world.getName());
		}
		Collections.sort(wList);
		return wList;
	}

}
