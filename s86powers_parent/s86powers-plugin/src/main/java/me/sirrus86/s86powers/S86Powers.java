package me.sirrus86.s86powers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import me.sirrus86.s86powers.command.PowerComExecutor;
import me.sirrus86.s86powers.command.PowerTabCompleter;
import me.sirrus86.s86powers.config.ConfigManager;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.listeners.BlockListener;
import me.sirrus86.s86powers.listeners.GUIListener;
import me.sirrus86.s86powers.listeners.PowerListener;
import me.sirrus86.s86powers.listeners.PlayerListener;
import me.sirrus86.s86powers.localization.LocaleLoader;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.PermissionHandler;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.utils.Metrics;
import me.sirrus86.s86powers.utils.PowerExporter;
import me.sirrus86.s86powers.utils.PowerLoader;

import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author sirrus86
 * @version 5.0.0
 */
public final class S86Powers extends JavaPlugin {

	private BlockListener bList;
	private PowerComExecutor comExec;
	private ConfigManager configManager;
	private File groupDir, powerDir, userDir;
	private PowerTabCompleter pTC;
	
	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		configManager = new ConfigManager(this);
		configManager.loadPluginConfig();
		configManager.loadPowerConfig();
		configManager.loadNeutralRegions();;
		try {
			new LocaleLoader();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PermissionHandler(this);
		new PowerExporter(this, getFile());
		new PowerLoader(this, getPowerDirectory());
		log(Level.INFO, LocaleString.POWERS_LOAD_SUCCESS.build(configManager.getPowers().size()));
		configManager.loadUsers();
		configManager.loadGroups();
		bList = new BlockListener(this);
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
		metrics.addCustomChart(new Metrics.AdvancedPie("used_powers", new Callable<Map<String, Integer>>() {

			@Override
			public Map<String, Integer> call() throws Exception {
				Map<String, Integer> valueMap = new HashMap<>();
				for (Power power : configManager.getPowers()) {
					if (power.getType() != PowerType.UTILITY) {
						PowerContainer pCont = PowerContainer.getContainer(power);
						valueMap.put(power.getName(), pCont.getUsers().size());
					}
				}
				return valueMap;
			}
			
		}));
	}
	
	/**
	 * Accesses the {@code BlockListener} class used by S86 Powers.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The current instance of the {@code BlockListener} class
	 */
	public final BlockListener getBlockListener() {
		return bList;
	}
	
	// TODO
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	/**
	 * Gets the directory where group configs are stored, specifically {@code plugins\S86_Powers\Groups\}.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The group configs directory as a {@link File}
	 */
	public final File getGroupDirectory() {
		if (groupDir == null) {
			groupDir = new File(getDataFolder(), "groups");
		}
		if (!groupDir.exists()) {
			groupDir.mkdirs();
		}
		return groupDir;
	}
	
	/**
	 * Gets the directory where power classes and configs are stored, specifically {@code plugins\S86_Powers\Powers\}.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The power classes/configs directory as a {@link File}
	 */
	public final File getPowerDirectory() {
		if (powerDir == null) {
			powerDir = new File(getDataFolder(), "powers");
		}
		if (!powerDir.exists()) {
			powerDir.mkdirs();
		}
		return powerDir;
	}
	
	/**
	 * Gets the directory where user config files are stored, specifically {@code plugins\S86_Powers\Users\}.
	 * <p>
	 * This is an internal method and shouldn't need to be used from within a power class.
	 * @return The user configs directory as a {@link File}
	 */
	public final File getUserDirectory() {
		if (userDir == null) {
			userDir = new File(getDataFolder(), "users");
		}
		if (!userDir.exists()) {
			userDir.mkdirs();
		}
		return userDir;
	}
	
	private void initCommands() {
		comExec = new PowerComExecutor();
		pTC = new PowerTabCompleter();
		PluginCommand cmd = getCommand("powers");
		cmd.setTabCompleter(pTC);
		cmd.setExecutor(comExec);
	}
	
	public void log(Level level, String string) {
		if (ConfigOption.Plugin.SHOW_COLORS_IN_CONSOLE) {
			getServer().getConsoleSender().sendMessage("[S86Powers] " + string);
		}
		else {
			getLogger().log(level, ChatColor.stripColor(string));
		}
	}
	
	@Deprecated
	public void showDebug(String message) {
		if (ConfigOption.Plugin.SHOW_DEBUG_MESSAGES) {
			log(Level.INFO, message);
		}
	}
	
}
