package me.sirrus86.s86powers.permissions;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class S86Permission {
	
	public static final Permission ADMIN = new Permission("s86powers.admin", "Gives user complete control of managing powers, players, groups, and regions.", PermissionDefault.OP);

	public static final Permission ENABLE = new Permission("s86powers.enable", "Enables powers for current player/rank/world.", PermissionDefault.TRUE);

	public static final Permission CONFIG = new Permission("s86powers.manage.config", "Allows user to use config commands.", PermissionDefault.FALSE);
	public static final Permission CONFIG_HELP = new Permission("s86powers.manage.config.help", "Allows user to view config help.", PermissionDefault.FALSE);
	public static final Permission CONFIG_INFO = new Permission("s86powers.manage.config.info", "Allows user to view config info.", PermissionDefault.FALSE);
	public static final Permission CONFIG_LIST = new Permission("s86powers.manage.config.list", "Allows user to view a list of config options.", PermissionDefault.FALSE);
	public static final Permission CONFIG_RELOAD = new Permission("s86powers.manage.config.reload", "Allows user to reload config from disk.", PermissionDefault.FALSE);
	public static final Permission CONFIG_SAVE = new Permission("s86powers.manage.config.save", "Allows user to save config to disk.", PermissionDefault.FALSE);
	public static final Permission CONFIG_SET = new Permission("s86powers.manage.config.set", "Allows user to set config options.", PermissionDefault.FALSE);

	public static final Permission GROUP = new Permission("s86powers.manage.group", "Allows user to use group commands.", PermissionDefault.TRUE);
	public static final Permission GROUP_ADD = new Permission("s86powers.manage.group.add", "Allows user to add powers to groups.", PermissionDefault.FALSE);
	public static final Permission GROUP_ASSIGN = new Permission("s86powers.manage.group.assign", "Allows user to add players into groups.", PermissionDefault.FALSE);
	public static final Permission GROUP_CREATE = new Permission("s86powers.manage.group.create", "Allows user to create groups.", PermissionDefault.FALSE);
	public static final Permission GROUP_DELETE = new Permission("s86powers.manage.group.delete", "Allows user to delete groups.", PermissionDefault.FALSE);
	public static final Permission GROUP_HELP = new Permission("s86powers.manage.group.help", "Allows user to view group help.", PermissionDefault.TRUE);
	public static final Permission GROUP_INFO = new Permission("s86powers.manage.group.info", "Allows user to view group info.", PermissionDefault.TRUE);
	public static final Permission GROUP_KICK = new Permission("s86powers.manage.group.kick", "Allows user to remove players from groups.", PermissionDefault.FALSE);
	public static final Permission GROUP_LIST = new Permission("s86powers.manage.group.list", "Allows user to view a list of all groups.", PermissionDefault.TRUE);
	public static final Permission GROUP_REMOVE = new Permission("s86powers.manage.group.remove", "Allows user to remove powers from groups.", PermissionDefault.FALSE);
	
	public static final Permission HELP = new Permission("s86powers.manage.help", "Allows user to view help topics.", PermissionDefault.TRUE);

	public static final Permission PLAYER = new Permission("s86powers.manage.player", "Allows user to use player commands.", PermissionDefault.TRUE);
	public static final Permission PLAYER_ADD = new Permission("s86powers.manage.player.add", "Allows user to add powers to other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_CLEAR = new Permission("s86powers.manage.player.clear", "Allows user to remove all powers from other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_GIVE = new Permission("s86powers.manage.player.give", "Allows user to give power books to other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_HELP = new Permission("s86powers.manage.player.help", "Allows user to view player help.", PermissionDefault.TRUE);
	public static final Permission PLAYER_INFO = new Permission("s86powers.manage.player.info", "Allows user to view info on other players.", PermissionDefault.TRUE);
	public static final Permission PLAYER_LIST = new Permission("s86powers.manage.player.list", "Allows user to view a list of all players.", PermissionDefault.TRUE);
	public static final Permission PLAYER_OPTION = new Permission("s86powers.manage.player.option", "Allows user to modify options of other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_REMOVE = new Permission("s86powers.manage.player.remove", "Allows user to remove powers from other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_STATS = new Permission("s86powers.manage.player.stats", "Allows user to view the power stats of other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_STATS_SET = new Permission("s86powers.manage.player.stats.set", "Allows user to set the power stats of other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_SUPPLY = new Permission("s86powers.manage.player.supply", "Allows user to supply other players.", PermissionDefault.FALSE);
	public static final Permission PLAYER_TOGGLE = new Permission("s86powers.manage.player.toggle", "Allows user to toggle the powers of other players.", PermissionDefault.FALSE);

	public static final Permission POWER = new Permission("s86powers.manage.power", "Allows user to use power commands.", PermissionDefault.TRUE);
	public static final Permission POWER_BLOCK = new Permission("s86powers.manage.power.block", "Allows user to block powers.", PermissionDefault.FALSE);
	public static final Permission POWER_DISABLE = new Permission("s86powers.manage.power.disable", "Allows user to disable powers.", PermissionDefault.FALSE);
	public static final Permission POWER_ENABLE = new Permission("s86powers.manage.power.enable", "Allows user to enable powers.", PermissionDefault.FALSE);
	public static final Permission POWER_HELP = new Permission("s86powers.manage.power.help", "Allows user to view power help.", PermissionDefault.TRUE);
	public static final Permission POWER_INFO = new Permission("s86powers.manage.power.info", "Allows user to view power info.", PermissionDefault.TRUE);
	public static final Permission POWER_KILL = new Permission("s86powers.manage.power.kill", "Allows user to kill powers.", PermissionDefault.FALSE);
	public static final Permission POWER_LIST = new Permission("s86powers.manage.power.list", "Allows user to view a list of all powers.", PermissionDefault.TRUE);
	public static final Permission POWER_LOCK = new Permission("s86powers.manage.power.lock", "Allows user to lock powers.", PermissionDefault.FALSE);
	public static final Permission POWER_OPTION = new Permission("s86powers.manage.power.option", "Allows user to view and modify power options.", PermissionDefault.FALSE);
	public static final Permission POWER_RELOAD = new Permission("s86powers.manage.power.reload", "Allows user to reload powers.", PermissionDefault.FALSE);
	public static final Permission POWER_SAVE = new Permission("s86powers.manage.power.save", "Allows user to save power options to disk.", PermissionDefault.FALSE);
	public static final Permission POWER_STATS = new Permission("s86powers.manage.power.stats", "Allows user to view and modify power stats.", PermissionDefault.FALSE);
	public static final Permission POWER_SUPPLY = new Permission("s86powers.manage.power.supplies", "Allows user to view and modify power supplies.", PermissionDefault.FALSE);
	public static final Permission POWER_UNBLOCK = new Permission("s86powers.manage.power.unblock", "Allows user to unblock powers.", PermissionDefault.FALSE);
	public static final Permission POWER_UNLOCK = new Permission("s86powers.manage.power.unlock", "Allows user to unlock powers.", PermissionDefault.FALSE);
	
	public static final Permission REGION = new Permission("s86powers.manage.self", "Allows user to use region commands.", PermissionDefault.TRUE);
	public static final Permission REGION_CREATE = new Permission("s86powers.manage.region.create", "Allows user to create regions.", PermissionDefault.FALSE);
	public static final Permission REGION_DELETE = new Permission("s86powers.manage.region.delete", "Allows user to delete regions.", PermissionDefault.FALSE);
	public static final Permission REGION_HELP = new Permission("s86powers.manage.region.help", "Allows user to view region help.", PermissionDefault.TRUE);
	public static final Permission REGION_INFO = new Permission("s86powers.manage.region.info", "Allows user to view region info.", PermissionDefault.FALSE);
	public static final Permission REGION_LIST = new Permission("s86powers.manage.region.list", "Allows user to view a list of all regions.", PermissionDefault.FALSE);
	public static final Permission REGION_TOGGLE = new Permission("s86powers.manage.region.neutral", "Allows user to neutralize regions.", PermissionDefault.FALSE);
	public static final Permission REGION_RESIZE = new Permission("s86powers.manage.region.resize", "Allows user to resize regions.", PermissionDefault.FALSE);

	public static final Permission SELF_ADD = new Permission("s86powers.manage.self.add", "Allows user to add powers to themselves.", PermissionDefault.TRUE);
	public static final Permission SELF_CLEAR = new Permission("s86powers.manage.self.clear", "Allows user to remove all powers from themselves.", PermissionDefault.TRUE);
	public static final Permission SELF_GIVE = new Permission("s86powers.manage.self.give", "Allows user to give themselves power books.", PermissionDefault.FALSE);
	public static final Permission SELF_INFO = new Permission("s86powers.manage.self.info", "Allows user to view info on themselves.", PermissionDefault.TRUE);
	public static final Permission SELF_OPTION = new Permission("s86powers.manage.self.option", "Allows user to add and remove their own personal power options.", PermissionDefault.FALSE);
	public static final Permission SELF_REMOVE = new Permission("s86powers.manage.self.remove", "Allows user to remove powers from themselves.", PermissionDefault.TRUE);
	public static final Permission SELF_STATS = new Permission("s86powers.manage.self.stats", "Allows user to view their own power stats.", PermissionDefault.TRUE);
	public static final Permission SELF_STATS_SET = new Permission("s86powers.manage.self.stats.set", "Allows user to set their own power stats.", PermissionDefault.FALSE);
	public static final Permission SELF_SUPPLY = new Permission("s86powers.manage.self.supply", "Allows user to supply themselves.", PermissionDefault.FALSE);
	public static final Permission SELF_TOGGLE = new Permission("s86powers.manage.self.toggle", "Allows user to toggle their own powers.", PermissionDefault.TRUE);
	
}
