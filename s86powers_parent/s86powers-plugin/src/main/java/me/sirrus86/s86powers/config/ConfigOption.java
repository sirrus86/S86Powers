package me.sirrus86.s86powers.config;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.utils.PowerTime;

public final class ConfigOption {
	
	public static final class Admin {
		
		private final static String PARENT = "admin.";
		
		public static boolean BYPASS_COOLDOWN = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "bypass-cooldown", false);
		
		public static boolean BYPASS_PERMISSION = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "bypass-permission", false);
		
	}
	
	public static final class Plugin {
		
		private final static String PARENT = "plugin.";

		public static boolean AUTO_SAVE = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "auto-save", true);

		public static long AUTO_SAVE_COOLDOWN = S86Powers.getConfigManager().getConfig().getLong(PARENT + "auto-save-cooldown", PowerTime.toMillis(5, 0));

		public static boolean ENABLE_PERMISSION_ASSIGNMENTS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "enable-permission-assignments", false);
		
		public static String LOCALIZATION = S86Powers.getConfigManager().getConfig().getString(PARENT + "localization", "enUS");
		
		public static boolean SAVE_ON_DISABLE = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "save-on-disable", true);
		
		public static boolean SHOW_COLORS_IN_CONSOLE = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-color-in-console", true);
		
		public static boolean SHOW_COMMAND_HEADER = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-command-header", true);
		
		public static boolean SHOW_COMMAND_LINES = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-command-lines", true);
		
		public static boolean SHOW_CONFIG_STATUS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-config-status", false);
		
		public static boolean SHOW_DEBUG_MESSAGES = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-debug-messages", false);
		
		public static boolean SHOW_INPUT_ERRORS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-input-errors", false);
		
		public static boolean SHOW_PACKET_ERRORS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-packet-errors", true);
		
		public static boolean USE_GUI = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "use-gui", true);
		
		public static boolean USE_LOOT_TABLES = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "use-loot-tables", false);
		
		public static boolean USE_METRICS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "use-metrics", true);
		
	}
	
	public static final class Powers {
		
		private final static String PARENT = "powers.";

		public static boolean BYPASS_PROTOCOLLIB_REQUIREMENT = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "bypass-protocollib-requirement", false);

		public static boolean DAMAGE_PLAYERS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "damage-players", true);

		public static boolean LOAD_INCOMPLETE_POWERS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "load-incomplete-powers", false);

		public static boolean PREVENT_GRIEFING = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "prevent-griefing", true);

		public static boolean SHOW_COOLDOWN_ON_ITEM = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-cooldown-on-item", true);

		public static boolean SHOW_HEARTS_ON_TAMED = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-hearts-on-tamed", true);

		public static boolean SHOW_NEUTRALIZING_BEACON = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-neutralizing-beacon", true);
	}
	
	public static final class Users {
		
		private final static String PARENT = "users.";

		public static boolean AUTO_ASSIGN = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "auto-assign", false);

		public static boolean ENFORCE_POWER_CAP = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "enforce-power-cap", true);

		public static int POWER_CAP_PER_TYPE = S86Powers.getConfigManager().getConfig().getInt(PARENT + "power-cap-per-type", 1);

		public static int POWER_CAP_TOTAL = S86Powers.getConfigManager().getConfig().getInt(PARENT + "power-cap-total", 3);

		public static boolean REMOVE_POWERS_ON_DEATH = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "remove-powers-on-death", false);

		public static boolean REPLACE_POWERS_OF_SAME_TYPE = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "replace-powers-of-same-type", false);
		
		public static boolean SAVE_FILES_BY_NAME = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "save-files-by-name", false);
		
		public static boolean SHOW_MESSAGES_IN_ACTION_BAR = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "show-messages-in-action-bar", true);
		
		public static boolean VIEW_INCOMPLETE_STAT_REWARDS = S86Powers.getConfigManager().getConfig().getBoolean(PARENT + "view-incomplete-stat-rewards", false);
		
	}
	
}
