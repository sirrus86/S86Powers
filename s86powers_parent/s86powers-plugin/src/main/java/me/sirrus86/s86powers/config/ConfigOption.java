package me.sirrus86.s86powers.config;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.utils.PowerTime;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigOption {

	private final static S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	
	public static class Admin {
		
		private final static String PARENT = "admin.";
		
		public static boolean BYPASS_PERMISSION = plugin.getConfigManager().getConfig().getBoolean(PARENT + "bypass-permission", false);
		
	}
	
	public static class Plugin {
		
		private final static String PARENT = "plugin.";

		public static boolean AUTO_SAVE = plugin.getConfigManager().getConfig().getBoolean(PARENT + "auto-save", true);

		public static long AUTO_SAVE_COOLDOWN = plugin.getConfigManager().getConfig().getLong(PARENT + "auto-save-cooldown", PowerTime.toMillis(5, 0));

		public static boolean ENABLE_PERMISSION_ASSIGNMENTS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "enable-permission-assignments", false);
		
		public static String LOCALIZATION = plugin.getConfigManager().getConfig().getString(PARENT + "localization", "enUS");
		
		public static boolean SAVE_ON_DISABLE = plugin.getConfigManager().getConfig().getBoolean(PARENT + "save-on-disable", true);
		
		public static boolean SHOW_COLORS_IN_CONSOLE = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-color-in-console", true);
		
		public static boolean SHOW_COMMAND_HEADER = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-command-header", true);
		
		public static boolean SHOW_CONFIG_STATUS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-config-status", false);
		
		public static boolean SHOW_DEBUG_MESSAGES = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-debug-messages", false);
		
		public static boolean SHOW_INPUT_ERRORS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-input-errors", false);
		
		public static boolean SHOW_PACKET_ERRORS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-packet-errors", true);
		
		public static boolean USE_GUI = plugin.getConfigManager().getConfig().getBoolean(PARENT + "use-gui", true);
		
		public static boolean USE_LOOT_TABLES = plugin.getConfigManager().getConfig().getBoolean(PARENT + "use-loot-tables", false);
		
		public static boolean USE_METRICS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "use-metrics", true);
		
	}
	
	public static class Powers {
		
		private final static String PARENT = "powers.";

		public static boolean LOAD_INCOMPLETE_POWERS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "load-incomplete-powers", false);

		public static boolean PREVENT_GRIEFING = plugin.getConfigManager().getConfig().getBoolean(PARENT + "prevent-griefing", true);

		public static boolean SHOW_COOLDOWN_ON_ITEM = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-cooldown-on-item", true);

		public static boolean SHOW_HEARTS_ON_TAMED = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-hearts-on-tamed", true);
	}
	
	public static class Users {
		
		private final static String PARENT = "users.";

		public static boolean AUTO_ASSIGN = plugin.getConfigManager().getConfig().getBoolean(PARENT + "auto-assign", false);

		public static boolean ENFORCE_POWER_CAP = plugin.getConfigManager().getConfig().getBoolean(PARENT + "enforce-power-cap", true);

		public static int POWER_CAP_PER_TYPE = plugin.getConfigManager().getConfig().getInt(PARENT + "power-cap-per-type", 1);

		public static int POWER_CAP_TOTAL = plugin.getConfigManager().getConfig().getInt(PARENT + "power-cap-total", 3);
		
		public static boolean SHOW_MESSAGES_IN_ACTION_BAR = plugin.getConfigManager().getConfig().getBoolean(PARENT + "show-messages-in-action-bar", true);
		
		public static boolean VIEW_INCOMPLETE_STAT_REWARDS = plugin.getConfigManager().getConfig().getBoolean(PARENT + "view-incomplete-stat-rewards", false);
		
	}
	
}
