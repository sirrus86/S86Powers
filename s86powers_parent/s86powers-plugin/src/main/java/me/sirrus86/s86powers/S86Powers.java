package me.sirrus86.s86powers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import me.sirrus86.s86powers.command.PowerComExecutor;
import me.sirrus86.s86powers.command.PowerTabCompleter;
import me.sirrus86.s86powers.config.ConfigManager;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.listeners.BlockListener;
import me.sirrus86.s86powers.listeners.GUIListener;
import me.sirrus86.s86powers.listeners.PowerListener;
import me.sirrus86.s86powers.listeners.PlayerListener;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.PermissionHandler;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.utils.Metrics;
import me.sirrus86.s86powers.utils.PowerExporter;
import me.sirrus86.s86powers.utils.PowerLoader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author sirrus86
 * @version 5.2.10
 */
public final class S86Powers extends JavaPlugin {

	private static ConfigManager configManager;
	private static File groupDir, powerDir, userDir;
	public static YamlConfiguration LOCALIZATION_YAML;
	private static final Plugin protocolLib = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
	private static double pLibVer = -1.0D;
	
	@Override
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		configManager = new ConfigManager(this);
		configManager.loadPluginConfig();
		configManager.loadPowerConfig();
		configManager.loadNeutralRegions();
		try {
			loadLocalization();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (protocolLib == null) {
			log(Level.WARNING, ChatColor.YELLOW + LocaleString.PROTOCOLLIB_NOT_DETECTED.toString());
		}
		else if (MCVersion.CURRENT_VERSION.getRequiredProtocolLib() > getProtocolLibVersion()) {
			log(Level.WARNING, ChatColor.YELLOW + LocaleString.BAD_PROTOCOLLIB_VERSION.build(MCVersion.CURRENT_VERSION.getRequiredProtocolLib(),
					protocolLib.getDescription().getVersion()));
		}
		new PermissionHandler(this);
		new PowerExporter(this, getFile());
		new PowerLoader(getPowerDirectory());
		log(Level.INFO, LocaleString.POWERS_LOAD_SUCCESS.build(configManager.getPowers().size()));
		configManager.loadUsers();
		configManager.loadGroups();
		new BlockListener(this);
		new PowerListener(this);
		new PlayerListener(this);
		new GUIListener(this);
		initCommands();
		if (ConfigOption.Plugin.USE_METRICS) {
			doMetrics();
		}
	}
	
	@Override
	public void onDisable() {
		configManager.disablePowers();
		if (ConfigOption.Plugin.SAVE_ON_DISABLE) {
			configManager.saveAll();
		}
	}
	
	private void doMetrics() {
		Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.AdvancedPie("used_powers", () -> {
			Map<String, Integer> valueMap = new HashMap<>();
			for (Power power : configManager.getPowers()) {
				if (power.getType() != PowerType.UTILITY) {
					valueMap.put(power.getName(), power.getUsers().size());
				}
			}
			return valueMap;
		}));
	}

	public static ConfigManager getConfigManager() {
		return configManager;
	}
	
	/**
	 * Gets the directory where group configs are stored, specifically {@code plugins\S86_Powers\Groups\}.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The group configs directory as a {@link File}
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public File getGroupDirectory() {
		if (groupDir == null) {
			groupDir = new File(getDataFolder(), "groups");
		}
		if (!groupDir.exists()) {
			groupDir.mkdirs();
		}
		return groupDir;
	}
	
	private static double getProtocolLibVersion() {
		if (pLibVer < 0.0D) {
			try {
				if (protocolLib != null) {
					pLibVer = Double.parseDouble(protocolLib.getDescription().getVersion().substring(0, 3));
				}
			} catch (Exception e) {
				pLibVer = 0.0D;
			}
		}
		return pLibVer;
	}
	
	/**
	 * Gets the directory where power classes and configs are stored, specifically {@code plugins\S86_Powers\Powers\}.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The power classes/configs directory as a {@link File}
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public File getPowerDirectory() {
		if (powerDir == null) {
			powerDir = new File(getDataFolder(), "powers");
		}
		if (!powerDir.exists()) {
			powerDir.mkdirs();
		}
		return powerDir;
	}
	
	public static Plugin getProtocolLib() {
		return protocolLib;
	}
	
	/**
	 * Gets the directory where user config files are stored, specifically {@code plugins\S86_Powers\Users\}.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The user configs directory as a {@link File}
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public File getUserDirectory() {
		if (userDir == null) {
			userDir = new File(getDataFolder(), "users");
		}
		if (!userDir.exists()) {
			userDir.mkdirs();
		}
		return userDir;
	}
	
	private void initCommands() {
		PowerComExecutor comExec = new PowerComExecutor();
		PowerTabCompleter pTC = new PowerTabCompleter();
		PluginCommand cmd = getCommand("powers");
		if (cmd != null) {
			cmd.setTabCompleter(pTC);
			cmd.setExecutor(comExec);
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void loadLocalization() throws IOException {
		File locDir = new File(getDataFolder(), "localization");
		if (!locDir.exists()) {
			locDir.mkdirs();
		}
		File locFile = new File(locDir, ConfigOption.Plugin.LOCALIZATION + ".yml");
		LOCALIZATION_YAML = YamlConfiguration.loadConfiguration(locFile);
		File defLocFile = new File(locDir, "enUS.yml");
		if (!defLocFile.exists()) {
			defLocFile.createNewFile();
		}
		YamlConfiguration defYaml = YamlConfiguration.loadConfiguration(defLocFile);
		List<String> header = List.of("To create your own localization file, make a copy of this file, name the copied file",
				"something with no spaces, then replace the text to the right of the colon with what",
				"should be read. To ensure a given line is readable by the plugin, leave any single quotes",
				"as-is. If any given line is deleted or unreadable, the plugin will use the default enUS",
				"line when needed.",
				"",
				"Words following an ampersand '&' are replaced by the plugin with the below data:",
				"&class - Gets the name of the specified class",
				"&cooldown - Renders the number as a long string, e.g. one minute five seconds",
				"&file - Gets the name of the specified file",
				"&group - Gets the name of the specified group",
				"&int - Gets a number supplied by the plugin",
				"&player - Gets the name of the specified player",
				"&power - Gets the name of the specified power",
				"&string - Is replaced by an expected string",
				"&syntax - Gets the help syntax for a specified command",
				"&type - Gets the name of the specified power type",
				"&value - Gets the expected value and outputs as string",
				"",
				"When changing a message, be sure to keep the above words in the message.",
				"Removing a special word from a message leaves it with no way to render unique info.",
				"Adding special words that weren't originally in the message has no effect.",
				"",
				"Once the new file is created, run the following command in-game or from the console:",
				"/p config set plugin.localization newfile",
				"Replacing newfile with the file you created, minus the .yml extension.",
				"You can alternatively edit the config.yml file directly while the server is inactive.",
				"",
				"Note: Editing this file (enUS.yml) is pointless as it is overwritten every time the",
				"server restarts.");
		if (MCVersion.isLessThan(MCVersion.v1_18_1)) {
			defYaml.options().setHeader(header);
		}
		else {
			//noinspection deprecation
			defYaml.options().header(String.join("\n", header));
		}
		for (LocaleString string : LocaleString.values()) {
			defYaml.set(string.getPath(), string.getDefaultText());
		}
		defYaml.save(defLocFile);
		LOCALIZATION_YAML.setDefaults(defYaml);
	}
	
	public static void log(Level level, String string) {
		if (ConfigOption.Plugin.SHOW_COLORS_IN_CONSOLE) {
			Bukkit.getServer().getConsoleSender().sendMessage("[S86Powers] " + string);
		}
		else {
			Bukkit.getLogger().log(level, ChatColor.stripColor(string));
		}
	}
	
	@Deprecated
	public static void showDebug(String message) {
		if (ConfigOption.Plugin.SHOW_DEBUG_MESSAGES) {
			log(Level.WARNING, message);
		}
	}
	
}
