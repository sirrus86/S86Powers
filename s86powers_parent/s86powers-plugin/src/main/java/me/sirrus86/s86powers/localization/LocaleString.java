package me.sirrus86.s86powers.localization;

import java.io.File;

import org.bukkit.ChatColor;

import me.sirrus86.s86powers.command.HelpTopic;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.tools.version.MCServer;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

public enum LocaleString {

	CONFIG_RELOADED("command", "Configuration has been reloaded from disk."),
	CONFIG_SAVED("command", "Configuration has been saved to disk."),
	EXPECTED_FORMAT("command", "Expected format: &syntax"),
	GROUP_ADD_PLAYER_SUCCESS("command", "Added &player to &group successfully."),
	GROUP_ADD_POWER_SUCCESS("command", "Assigned &power to &group successfully."),
	GROUP_ALREADY_EXISTS("command", "A group named &string already exists."),
	GROUP_ALREADY_HAS_PLAYER("command", "&player is already a member of &group."),
	GROUP_ALREADY_HAS_POWER("command", "&group already has &power assigned."),
	GROUP_ASSIGN_UTILITY("command", "You can't assign utility powers to groups."),
	GROUP_CREATE_SUCCESS("command", "Group &group was created successfully."),
	GROUP_DELETE_SUCCESS("command", "Group &group was deleted successfully."),
	GROUP_MISSING_NAME("command", "You must specify a name for the group."),
	GROUP_MISSING_PLAYER("command", "&player isn't a member of &group."),
	GROUP_MISSING_POWER("command", "&group doesn't have &power assigned."),
	GROUP_REMOVE_PLAYER_SUCCESS("command", "Removed &player from &group successfully."),
	GROUP_REMOVE_POWER_SUCCESS("command", "Removed &power from &group successfully."),
	INDEX_MUST_BE_NUMBER("command", "Index must be a number."),
	ITEM_CREATED_INVALID("command", "Item created was invalid."),
	MUST_SPECIFY_OPTION("command", "You must specify a valid option."),
	MUST_SPECIFY_VALUE("command", "You must specify a value."),
	NO_PERMISSION("command", "You don't have permission for that command."),
	NO_STATS_RECORDED("command", "No stats recorded."),
	PAGE_OF("command", "Page &int of &string"),
	PLAYER_ADD_POWER_SUCCESS("command", "Assigned &power to &player successfully."),
	PLAYER_ALREADY_HAS_POWER("command", "&player already has &power assigned."),
	PLAYER_ASSIGN_UTILITY("command", "You can't assign utility powers to players."),
	PLAYER_MISSING_POWER("command", "&player doesn't have &power assigned."),
	PLAYER_NOT_ONLINE("command", "Player &player is not online."),
	PLAYER_POWER_DISABLED("command", "&player now has &power disabled."),
	PLAYER_POWER_ENABLED("command", "&player now has &power enabled."),
	PLAYER_POWERS_DISABLED("command", "&player now has all powers disabled."),
	PLAYER_POWERS_ENABLED("command", "&player now has all powers enabled."),
	PLAYER_REMOVE_POWER_SUCCESS("command", "Removed &power from &player successfully."),
	PLAYER_TOO_MANY_POWERS("command", "&player has too many powers assigned."),
	PLAYER_TOO_MANY_POWERS_TYPE("command", "&player has too many powers of type &type assigned."),
	PLAYER_SUPPLY_SUCCESS("command", "Supplied &player successfully."),
	POWER_ALREADY_DISABLED("command", "&power is already disabled."),
	POWER_ALREADY_ENABLED("command", "&power is already enabled."),
	POWER_ASSIGN_NO_PERMISSION("command", "You don't have permission to assign &power."),
	POWER_BLOCK_FAIL("command", "Unable to block &power, it may already be blocked."),
	POWER_BLOCK_SUCCESS("command", "&power was blocked successfully."),
	POWER_DISABLE_FAIL("command", "Unable to disable &power, it may be locked."),
	POWER_DISABLE_SUCCESS("command", "&power was disabled successfully."),
	POWER_ENABLE_FAIL("command", "Unable to enable &power, it may be locked."),
	POWER_ENABLE_SUCCESS("command", "&power was enabled successfully."),
	POWER_KILL_SUCCESS("command", "&string was killed successfully."),
	POWER_LOCK_FAIL("command", "&power is already locked."),
	POWER_LOCK_SUCCESS("command", "&power was locked successfully."),
	POWER_MISSING_OPTION("command", "&power doesn't have an option called &string."),
	POWER_MISSING_STAT("command", "&power doesn't have a stat called &string."),
	POWER_RELOAD_SUCCESS("command", "&power was reloaded successfully."),
	POWER_SAVE_SUCCESS("command", "&power's options were saved successfully."),
	POWER_SUPPLY_ADD("command", "Supply &int set to &string successfully."),
	POWER_SUPPLY_NEGATIVE("command", "You must specify a valid index (>= 0)."),
	POWER_SUPPLY_REMOVE("command", "Supply &int removed successfully."),
	POWER_UNBLOCK_FAIL("command", "Unable to unblock &power, it may not be blocked."),
	POWER_UNBLOCK_SUCCESS("command", "&power was unblocked successfully."),
	POWER_UNLOCK_FAIL("command", "&power is already unlocked."),
	POWER_UNLOCK_SUCCESS("command", "&power was unlocked successfully."),
	POWERS_BY_TYPE("command", "by Type: &type"),
	QUANTITY_NOT_NUMBER("command", "Quantity must be a number."),
	REGION_ALREADY_EXISTS("command", "A region named &string already exists."),
	REGION_COORD_NOT_NUMBER("command", "One or more provided coordinates were not numbers."),
	REGION_CREATE_SUCCESS("command", "Region &region was created successfully."),
	REGION_DELETE_SUCCESS("command", "Region &region was deleted successfully."),
	REGION_MISSING_NAME("command", "You must specify a name for the region."),
	REGION_MISSING_WORLD("command", "You must specify a world for the region."),
	REGION_TOGGLE_DISABLE("command", "Region &region is no longer neutral."),
	REGION_TOGGLE_ENABLE("command", "Region &region has been set to neutral."),
	REGION_NOT_ENOUGH_COORDS("command", "Not enough coordinates were provided."),
	REGION_RESIZE_SUCCESS("command", "Region &region was resized successfully."),
	REMOVE_OPTION_SUCCESS("command", "Option &string was reset to its default value."),
	SELF_ADD_POWER_SUCCESS("command", "Assigned &power successfully."),
	SELF_ALREADY_HAS_POWER("command", "You already have &power assigned."),
	SELF_ASSIGN_UTILITY("command", "You can't assign utility powers to yourself."),
	SELF_FROM_CONSOLE("command", "Self commands can't be used from the console."),
	SELF_MISSING_POWER("command", "You don't have &power assigned."),
	SELF_POWER_DISABLED("command", "You now have &power disabled."),
	SELF_POWER_ENABLED("command", "You now have &power enabled."),
	SELF_POWERS_DISABLED("command", "You now have all powers disabled."),
	SELF_POWERS_ENABLED("command", "You now have all powers enabled."),
	SELF_REMOVE_NO_POWERS("command", "No powers are assigned."),
	SELF_REMOVE_POWER_SUCCESS("command", "Removed &power successfully."),
	SELF_TOO_MANY_POWERS("command", "You have too many powers assigned."),
	SELF_TOO_MANY_POWERS_TYPE("command", "You have too many powers of type &type assigned."),
	SET_OPTION_FAIL("command", "Attempt to set option &string to &value failed. Expected type: &class."),
	SET_OPTION_LOCKED("command", "Attempt to set option &string to &value failed. Option is locked."),
	SET_OPTION_ADD_SUCCESS("command", "Added &value to option &string successfully."),
	SET_OPTION_REMOVE_SUCCESS("command", "Removed &value from option &string successfully."),
	SET_OPTION_SUCCESS("command", "Option &string was set to &value successfully."),
	SET_STAT_SUCCESS("command", "Stat &string was set to &int successfully."),
	SELF_SUPPLY_SUCCESS("command", "Supplied yourself successfully."),
	SPECIFY_ITEM_OR_NULL("command", "You must specify an item or null."),
	UNKNOWN_COMMAND("command", "Unknown command: &string"),
	UNKNOWN_GROUP("command", "Group &string was not found or does not exist."),
	UNKNOWN_OPTION("command", "Unknown option: &string."),
	UNKNOWN_PLAYER("command", "Unknown player."),
	UNKNOWN_POWER("command", "Power was not found or is not loaded."),
	UNKNOWN_TYPE("command", "Unknown power type: &string."),
	UNKNOWN_REGION("command", "Region &string was not found or does not exist."),
	VALUE_WRONG_TYPE("command", "Value was not of the expected type: &string."),
	VIEW_VALUE_FAIL("command", "Unable to determine the value of this option."),

	AUTO_ASSIGN_CONFIG("config", "Whether to automatically assign random powers to new players who join the server for the first time."),
	AUTO_SAVE_CONFIG("config", "Whether to automatically save configs as values are changed."),
	AUTO_SAVE_COOLDOWN_CONFIG("config", "Amount of time in milliseconds before auto-save can be triggered again."),
	BYPASS_COOLDOWN_CONFIG("config", "Whether admins ignore cooldowns from power use."),
	BYPASS_PERMISSION_CONFIG("config", "Whether admins can use powers regardless of having the 's86powers.enable' permission."),
	BYPASS_PROTOCOLLIB_REQUIREMENT_CONFIG("config", "Whether to load powers regardless of whether the correct version of ProtocolLib is detected."),
	DAMAGE_PLAYERS_CONFIG("config", "Whether players can damage other players with powers."),
	ENABLE_PERMISSION_ASSIGNMENTS_CONFIG("config", "Whether players can have powers or groups assigned to them via permissions."),
	ENFORCE_POWER_CAP_CONFIG("config", "Whether non-admins should be limited in how many powers they may assign to themselves."),
	LOAD_INCOMPLETE_POWERS_CONFIG("config", "Whether incomplete powers should be loaded."),
	LOCALIZATION_CONFIG("config", "Language file that should be used for most messages."),
	POWER_CAP_PER_TYPE_CONFIG("config", "Maximum number of powers a non-admin may assign to themselves per given type."),
	POWER_CAP_TOTAL_CONFIG("config", "Maximum number of powers a non-admin may assign to themselves."),
	PREVENT_GRIEFING_CONFIG("config", "Whether fires, explosions, etc. from powers should cause no structural damage."),
	REMOVE_POWERS_ON_DEATH_CONFIG("config", "Whether powers should be removed from players when they die."),
	REPLACE_POWERS_OF_SAME_TYPE_CONFIG("config", "Whether newly assigned powers should automatically replace a random existing power of the same type when the cap has been reached."),
	SAVE_ON_DISABLE_CONFIG("config", "Whether to automatically save all configs when the plugin is disabled."),
	SHOW_COLORS_IN_CONSOLE_CONFIG("config", "Whether to show colors in the console. Note: Commands will show colors regardless."),
	SHOW_COMMAND_HEADER_CONFIG("config", "Whether to show the plugin header when commands are executed. If false, a line is shown."),
	SHOW_COMMAND_LINES_CONFIG("config", "Whether to show the header and footer lines when commands are executed."),
	SHOW_CONFIG_STATUS_CONFIG("config", "Whether to show when a config file succeeds or fails to save or load."),
	SHOW_COOLDOWN_ON_ITEM_CONFIG("config", "Whether to show a cooldown indicator on the item used for a given power."),
	SHOW_DEBUG_MESSAGES_CONFIG("config", "Whether to show debug messages in the console."),
	SHOW_HEARTS_ON_TAMED_CONFIG("config", "Whether to show a health gauge over tamed entities."),
	SHOW_INPUT_ERRORS_CONFIG("config", "Whether to show when user input results in an error."),
	SHOW_MESSAGES_IN_ACTION_BAR_CONFIG("config", "Whether to show messages in the action bar as opposed to the chat window."),
	SHOW_NEUTRALIZING_BEACON_CONFIG("config", "Whether to show neutralizer beacons currently affecting the player to them."),
	SHOW_PACKET_ERRORS_CONFIG("config", "Whether to show packet-related errors in the console."),
	USE_GUI_CONFIG("config", "Whether to use the graphic user interface when typing a lone /powers command."),
	USE_LOOT_TABLES_CONFIG("config", "Whether to insert books that grant powers into loot tables."),
	USE_METRICS_CONFIG("config", "Whether to use metrics."),
	VIEW_INCOMPLETE_STAT_REWARDS_CONFIG("config", "Whether to show the rewards for stats not completed by users."),
	
	BAD_PROTOCOLLIB_VERSION("console", "ProtocolLib v&double or higher is required for S86Powers to work properly! Current version: &string"),
	DEBUG_POWER("console", "&string is marked as a debug power and was not loaded."),
	FILE_CREATE_FAIL("console", "Unable to create &file."),
	INCOMPLETE_POWER("console", "&string is marked as incomplete and was not loaded."),
	INVALID_POWER_MANIFEST("console", "&string is missing a PowerManifest and was not loaded."),
	INVALID_POWER_NAME("console", "&string has no assigned name and was not loaded."),
	INVALID_POWER_TYPE("console", "&string has an invalid power type and was not loaded."),
	INVALID_SERVER_SOFTWARE("console", "&string will not work with this server software and was not loaded. Required software: &server"),
	INVALID_SERVER_VERSION("console", "&string will not work with this server version and was not loaded. Required version: &version"),
	LOAD_ATTEMPT("console", "Attempting to load &file..."),
	LOAD_FAIL("console", "Failed to load &file."),
	LOAD_SUCCESS("console", "Successfully loaded &file."),
	POWER_LOAD_BLOCKED("console", "&class is blocked and was not loaded."),
	POWER_LOAD_SUCCESS("console", "&power loaded successfully."),
	POWER_REQUIRES_PROTOCOLLIB("console", "&string requires ProtocolLib and was not loaded."),
	POWERS_LOAD_SUCCESS("console", "&int powers loaded successfully."),
	PROTOCOLLIB_NOT_DETECTED("console", "ProtocolLib was not detected. Some powers and features may fail to load or operate properly."),
	SAVE_ATTEMPT("console", "Attempting to save &file..."),
	SAVE_FAIL("console", "Failed to save &file."),
	SAVE_SUCCESS("console", "Successfully saved &file."),

	CONFIG_HELP_HELP("help", "Shows a list of applicable config commands."),
	CONFIG_INFO_HELP("help", "Shows info on a given config option."),
	CONFIG_LIST_HELP("help", "Shows a list of applicable config options as well as their current values."),
	CONFIG_RELOAD_HELP("help", "Reloads all config options from disk. Note: This does not retain unsaved changes."),
	CONFIG_SAVE_HELP("help", "Saves all config options to disk."),
	CONFIG_SET_HELP("help", "Sets the given config option to the specified value."),
	GROUP_ADD_HELP("help", "Adds the specified power to the group."),
	GROUP_ASSIGN_HELP("help", "Adds the specified player to the group."),
	GROUP_CREATE_HELP("help", "Creates a new group by the specified name."),
	GROUP_DELETE_HELP("help", "Deletes the group from the group database."),
	GROUP_HELP_HELP("help", "Shows a list of applicable group commands."),
	GROUP_INFO_HELP("help", "Shows info on a given group."),
	GROUP_KICK_HELP("help", "Kicks the specified player from the group."),
	GROUP_LIST_HELP("help", "Shows a list of all groups in the group database."),
	GROUP_REMOVE_HELP("help", "Removes the specified power from the group."),
	HELP_HELP("help", "Shows a list of all available commands, optionally for a specific topic."),
	PLAYER_ADD_HELP("help", "Adds the specified power to the player."),
	PLAYER_HELP_HELP("help", "Shows a list of applicable player commands."),
	PLAYER_INFO_HELP("help", "Shows info on the specified player."),
	PLAYER_LIST_HELP("help", "Shows a list of all players in the database."),
	PLAYER_OPTION_HELP("help", "Views or modifies personal power options for the player."),
	PLAYER_REMOVE_HELP("help", "Removes the specified power from the player."),
	PLAYER_STATS_HELP("help", "Shows the player's stats for all powers or the specified power."),
	PLAYER_SUPPLY_HELP("help", "Supplies player with items for all powers or the specified power."),
	PLAYER_TOGGLE_HELP("help", "Toggles all power use or the specified power for the player."),
	POWER_BLOCK_HELP("help", "Blocks the power from being loaded next time the plugin loads."),
	POWER_DISABLE_HELP("help", "Forces the specified power or all powers to disable."),
	POWER_ENABLE_HELP("help", "Forces the specified power or all powers to enable."),
	POWER_HELP_HELP("help", "Shows a list of applicable power commands."),
	POWER_INFO_HELP("help", "Shows info on a given power."),
	POWER_KILL_HELP("help", "Attempts to unload the power from memory."),
	POWER_LIST_HELP("help", "Shows a list of all loaded powers, optionally filtered by type."),
	POWER_LOCK_HELP("help", "Locks a power, preventing it from automatically enabling or disabling."),
	POWER_OPTION_HELP("help", "Shows the options of a given power. Also allows viewing detailed info of, as well as setting options."),
	POWER_RELOAD_HELP("help", "Reloads a power from disk. Note: This does not retain unsaved changes to the power's options."),
	POWER_SAVE_HELP("help", "Saves all power options to disk."),
	POWER_STATS_HELP("help", "Shows the stats of a given power. Also allows viewing detailed info of, as well as setting stats."),
	POWER_SUPPLY_HELP("help", "Shows the supplies of a given power. Can also be used to change supplies."),
	POWER_UNBLOCK_HELP("help", "Unblocks the power, allowing it to load next time the plugin loads."),
	POWER_UNLOCK_HELP("help", "Unlocks a power, allowing it to be enabled or disabled."),
	REGION_CREATE_HELP("help", "Creates a new region, either in the specified world or in the world inhabited by the command user."),
	REGION_DELETE_HELP("help", "Removes the specified region from the region database."),
	REGION_HELP_HELP("help", "Shows a list of applicable region commands."),
	REGION_INFO_HELP("help", "Shows info on the specified region."),
	REGION_LIST_HELP("help", "Shows a list of all regions."),
	REGION_RESIZE_HELP("help", "Changes the size of the specified region."),
	REGION_TOGGLE_HELP("help", "Toggles the neutral state of the specified region."),
	SELF_ADD_HELP("help", "Adds the specified power to the command user."),
	SELF_INFO_HELP("help", "Shows info on the command user."),
	SELF_OPTION_HELP("help", "Views or modifies personal power options for the command user."),
	SELF_REMOVE_HELP("help", "Removes the specified power from the command user."),
	SELF_STATS_HELP("help", "Shows the command user's stats for all powers or the specified power."),
	SELF_SUPPLY_HELP("help", "Supplies the command user for all powers or the specified one."),
	SELF_TOGGLE_HELP("help", "Toggles all power use or the specified power for the command user."),
	
	KILLED_BY_POWER("players", "&player was killed by &string's &power."),
	NEUTRALIZED_BY_BEACON("players", "Your powers have been neutralized by a nearby Neutralizer Beacon."),
	NEUTRALIZED_BY_POWER("players", "Your powers have been neutralized by &power for &cooldown."),
	NEUTRALIZED_BY_REGION("players", "You've entered a neutralized region."),
	POWER_ON_COOLDOWN("players", "&power is still on cooldown for &cooldown."),
	POWERS_RETURN("players", "Your powers return to you."),

	ADD_PLAYER("words", "Add Player"),
	ADD_POWER("words", "Add Power"),
	AUTHOR("words", "Author"),
	BACK("words", "Back"),
	CONCEPT("words", "Concept"),
	CONFIG("words", "Config"),
	DAY("words", "day"),
	DAYS("words", "days"),
	DEFAULT("words", "Default"),
	DELETE("words", "Delete"),
	DESCRIPTION("words", "Description"),
	DIMENSIONS("words", "Dimensions"),
	DISABLE("words", "Disable"),
	ENABLE("words", "Enable"),
	ERROR_CAPS("words", "ERROR"),
	GROUP("words", "Group"),
	GROUPS("words", "Groups"),
	HELP_CAPS("words", "HELP"),
	HOUR("words", "hour"),
	HOURS("words", "hours"),
	INFO("words", "Info"),
	INFO_CAPS("words", "INFO"),
	LESS_THAN_ONE_SECOND("words", "less than one second"),
	LIST("words", "List"),
	LIST_CAPS("words", "LIST"),
	MINUTE("words", "minute"),
	MINUTES("words", "minutes"),
	NONE("words", "None"),
	PAGE("words", "Page"),
	OPTIONS("words", "Options"),
	PLAYER("words", "Player"),
	PLAYERS("words", "Players"),
	POWER("words", "Power"),
	POWERS("words", "Powers"),
	REGION("words", "Region"),
	REGIONS("words", "Regions"),
	RELOAD("words", "Reload"),
	REMOVE_PLAYER("words", "Remove Player"),
	REMOVE_POWER("words", "Remove Power"),
	REWARD("words", "Reward"),
	SAVE("words", "Save"),
	SECOND("words", "second"),
	SECONDS("words", "seconds"),
	STATS("words", "Stats"),
	SUCCESS_CAPS("words", "SUCCESS"),
	SUPPLIES("words", "Supplies"),
	TYPE("words", "Type"),
	VALUE("words", "Value"),
	WORLD("words", "World");
	
	private final String defText, prefix;
	
	private LocaleString(String prefix, String defText) {
		this.defText = defText;
		this.prefix = prefix;
	}
	
	public String build(Object... objects) {
		String text = this.toString();
		for (int i = 0; i < objects.length; i ++) {
			if (objects[i] instanceof File
					&& text.contains("&file")) {
				File file = (File) objects[i];
				text = text.replaceAll("&file", file.getName());
			}
			else if (objects[i] instanceof Long
					&& text.contains("&cooldown")) {
				long cooldown = (long) objects[i];
				text = text.replaceAll("&cooldown", PowerTime.asLongString(cooldown));
			}
			else if (objects[i] instanceof PowerGroup
					&& text.contains("&group")) {
				PowerGroup group = (PowerGroup) objects[i];
				text = text.replaceAll("&group", group.getName());
			}
			else if (objects[i] instanceof Double
					&& text.contains("&double")) {
				double j = (double) objects[i];
				text = text.replaceAll("&double", Double.toString(j));
			}
			else if (objects[i] instanceof Integer
					&& text.contains("&int")) {
				int j = (int) objects[i];
				text = text.replaceAll("&int", Integer.toString(j));
			}
			else if (objects[i] instanceof PowerUser
					&& text.contains("&player")) {
				PowerUser user = (PowerUser) objects[i];
				text = text.replaceAll("&player", user.getName() != null ? user.getName() : "!NULL");
			}
			else if (objects[i] instanceof Power
					&& text.contains("&power")) {
				Power power = (Power) objects[i];
				text = text.replaceAll("&power", power.getType().getColor() + power.getName() + ChatColor.RESET);
			}
//			else if (objects[i] instanceof PotionEffect
//					&& text.contains("&effect")) {
//				PotionEffect effect = (PotionEffect) objects[i];
//				text = text.replaceAll("&effect", text); // TODO
//			}
			else if (objects[i] instanceof String
					&& text.contains("&string")) {
				String string = (String) objects[i];
				text = text.replaceAll("&string", string);
			}
			else if (objects[i] instanceof HelpTopic
					&& text.contains("&syntax")) {
				HelpTopic topic = (HelpTopic) objects[i];
				text = text.replaceAll("&syntax", ChatColor.AQUA + topic.getSyntax() + ChatColor.RESET);
			}
			else if (objects[i] instanceof Class<?>
					&& text.contains("&class")) {
				Class<?> clazz = (Class<?>) objects[i];
				text = text.replaceAll("&class", clazz.getSimpleName());
			}
			else if (objects[i] instanceof PowerType
					&& text.contains("&type")) {
				PowerType type = (PowerType) objects[i];
				text = text.replaceAll("&type", type.getColor() + type.getName() + ChatColor.RESET);
			}
			else if (objects[i] instanceof NeutralRegion
					&& text.contains("&region")) {
				NeutralRegion region = (NeutralRegion) objects[i];
				text = text.replaceAll("&region", region.getName());
			}
			else if (objects[i] instanceof MCVersion
					&& text.contains("&version")) {
				MCVersion version = (MCVersion) objects[i];
				text = text.replaceAll("&version", version.name().replace("_", "."));
			}
			else if (objects[i] instanceof MCServer
					&& text.contains("&server")) {
				MCServer server = (MCServer) objects[i];
				text = text.replaceAll("&server", server.toString());
			}
			else if (text.contains("&value")) {
				text = text.replaceAll("&value", objects[i] != null ? objects[i].toString() : "null");
			}
		}
		return text;
	}
	
	public String getDefaultText() {
		return this.defText;
	}
	
	public String getPath() {
		return this.prefix + "." + name().replaceAll("_", "-").toLowerCase();
	}
	
	public static String getString(String localeString) {
		try {
			return LocaleString.valueOf(localeString).toString();
		} catch(Exception e) {
			return "";
		}
	}
	
	@Override
	public String toString() {
		return LocaleLoader.LOCALIZATION_YAML.getString(this.getPath());
	}
	
}
