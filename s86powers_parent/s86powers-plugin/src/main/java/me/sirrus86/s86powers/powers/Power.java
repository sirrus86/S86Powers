package me.sirrus86.s86powers.powers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.users.PowerUser;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Abstract class for a power class in S86 Powers. All power classes must extend this class in order to be loaded.
 */
public abstract class Power implements Comparable<Power>, Listener {
	
	private Map<PowerOption<?>, Object> options = new HashMap<>();
	private List<ItemStack> supplies = new ArrayList<>();
	private Map<PowerStat, Integer> stats = new HashMap<>();
	private Set<Integer> tasks = new HashSet<>();
	private Set<PowerUser> users = new HashSet<>();
	
	private File cFile, defLocFile, locFile;
	private YamlConfiguration config, defLocConfig, locConfig;
	private boolean enabled = true,
			locked = false;
	private final Permission aPerm, perm;
	@Deprecated
	protected boolean incomplete = false;
	private final PowerManifest manifest = getClass().getAnnotation(PowerManifest.class);
	private final static S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	private String description, name, tag = getClass().getSimpleName();
	
	/**
	 * Dedicated instance of {@link java.util.Random} used to create random values where needed.
	 */
	protected static final Random random = new Random();
	
	protected static final NamespacedKey collectorKey = new NamespacedKey(plugin, "powercollector.power-key");

	protected PowerOption<Long> cooldown;
	protected PowerOption<ItemStack> consumable, item;
	protected PowerOption<Boolean> wAxe, wItem, wSword;
	
	/**
	 * Events which should occur when the power becomes enabled or reloaded.
	 * <p>
	 * This is typically where Collections are initialized so that they may be reinitialized should this power be reset.
	 */
	protected void onEnable() {}
	
	/**
	 * Events which should occur should a given user add this power or join with it already assigned.
	 * These events also occur when the user is deneutralized.
	 * @param user - User whose powers will be enabled.
	 */
	protected void onEnable(PowerUser user) {}
	
	/**
	 * Events which should occur when the power becomes disabled, such as when killing the power or shutting down the server.
	 * <p>
	 * By default, this will run {@link Power#onDisable(PowerUser)} against every user of this power.
	 */
	protected void onDisable() {}
	
	/**
	 * Events which should occur should a given user's powers become disabled or neutralized.
	 * @param user - User whose powers have become disabled or neutralized.
	 */
	protected void onDisable(PowerUser user) {}
	
	/**
	 * Catalogs all configurable options for this power.
	 */
	protected abstract void config();

	public Power() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		File locFolder = new File(plugin.getPowerDirectory(), "localization");
		if (!locFolder.exists()) {
			locFolder.mkdirs();
		}
		cFile = new File(plugin.getPowerDirectory(), tag + ".yml");
		defLocFile = new File(locFolder, tag + "-enUS.yml");
		File[] files = new File[] {cFile, defLocFile};
		for (File file : files) {
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					plugin.getLogger().severe(LocaleString.FILE_CREATE_FAIL.build(file));
					e.printStackTrace();
				}
			}
		}
		config = YamlConfiguration.loadConfiguration(cFile);
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.LOAD_SUCCESS.build(cFile));
		}
		defLocConfig = YamlConfiguration.loadConfiguration(defLocFile);
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.LOAD_SUCCESS.build(defLocFile));
		}
		locFile = new File(locFolder, tag + "-" + ConfigOption.Plugin.LOCALIZATION + ".yml");
		locConfig = locFile.exists() ? YamlConfiguration.loadConfiguration(locFile) : defLocConfig;
		aPerm = new Permission("s86powers.assign." + tag, "Allows player to assign " + getName() + ".", PermissionDefault.TRUE);
		perm = new Permission("s86powers.power." + tag, "Allows player to use  " + getName() + " regardless if assigned or not.", PermissionDefault.FALSE);
		if (!plugin.getServer().getPluginManager().getPermissions().contains(aPerm)) {
			plugin.getServer().getPluginManager().addPermission(aPerm);
		}
		if (!plugin.getServer().getPluginManager().getPermissions().contains(perm)) {
			plugin.getServer().getPluginManager().addPermission(perm);
		}
		// Force localization file to populate
		getTag();
		getName();
		getDescription();
	}

	public void addUser(PowerUser user) {
		users.add(user);
	}
	
	/**
	 * Shortcut method to call an event from within a power class.
	 * @param event - Event to be called
	 */
	protected void callEvent(Event event) {
		plugin.getServer().getPluginManager().callEvent(event);
	}
	
	/**
	 * Cancels a running task with the specified task ID.
	 * @param taskId - The task ID of the running task
	 */
	protected void cancelTask(int taskId) {
		if (plugin.getServer().getScheduler().isCurrentlyRunning(taskId)
				|| plugin.getServer().getScheduler().isQueued(taskId)) {
			plugin.getServer().getScheduler().cancelTask(taskId);
		}
	}
	
	/**
	 * Determines whether any {@link ItemStack} which represents an axe can be used to activate this power.
	 * @return {@link boolean} value of the field {@code wAxe}
	 */
	public boolean canUseAnyAxe() {
		return hasOption(wAxe) ? getOption(wAxe) : false;
	}
	
	/**
	 * Determines whether any {@link ItemStack} which represents the item specified by the field {@code item} can be used to activate this power.
	 * @return {@link boolean} value of the field {@code wItem}
	 */
	public boolean canUseSpecificItem() {
		return hasOption(wItem) ? getOption(wItem) : true;
	}
	
	/**
	 * Determines whether any {@link ItemStack} which represents a sword can be used to activate this power.
	 * @return {@link boolean} value of the field {@code wSword}
	 */
	public boolean canUseAnySword() {
		return hasOption(wSword) ? getOption(wSword) : false;
	}
	
	@Override
	public int compareTo(Power p) {
		String o1Str = getName(),
				o2Str = p.getName();
		List<String> tmp = Arrays.asList(o1Str, o2Str);
		Collections.sort(tmp);
		if (tmp.get(0).equalsIgnoreCase(getName())) return -1;
		else return 1;
	}
	
	/**
	 * Shortcut method to create a {@link NamespacedKey} for use in persistent tags within the power class.
	 * @param key - The name of the key to create
	 * @return The created {@link NamespacedKey}
	 */
	protected NamespacedKey createNamespacedKey(String key) {
		return new NamespacedKey(plugin, tag.toLowerCase() + "." + key.toLowerCase());
	}
	
	public void disable() {
		for (PowerUser user : getUsers()) {
			disable(user);
		}
		onDisable();
		if (Arrays.asList(getClass().getInterfaces()).contains(Listener.class)) {
			HandlerList.unregisterAll(this);
		}
		for (int i : tasks) {
			if (plugin.getServer().getScheduler().isCurrentlyRunning(i)
					|| plugin.getServer().getScheduler().isQueued(i)) {
				plugin.getServer().getScheduler().cancelTask(i);
			}
		}
		tasks.clear();
		enabled = false;
	}
	
	public void disable(PowerUser user) {
		onDisable(user);
	}
	
	public void enable() {
		if (Arrays.asList(getClass().getInterfaces()).contains(Listener.class)) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
		reload();
		enabled = true;
		for (PowerUser user : getUsers()) {
			enable(user);
		}
	}
	
	public void enable(PowerUser user) {
		onEnable(user);
	}
	
	public final Permission getAssignPermission() {
		return aPerm;
	}
	
	public String getAuthor() {
		return manifest.author();
	}
	
	public String getConcept() {
		return manifest.concept();
	}
	
	protected ItemStack getConsumable() {
		return hasOption(consumable) ? getOption(consumable) : null;
	}
	
	protected long getCooldown() {
		return hasOption(cooldown) ? getOption(cooldown) : 0L;
	}

	/**
	 * Gets the config file used by this power to hold info on options and stats.
	 * This file can also be used to hold persistent information for the power.
	 * @return This power's config file
	 */
	public final YamlConfiguration getConfig() {
		return config;
	}
	
	public final String getDescription() {
		if (description == null) {
			description = locale("manifest.description", manifest.description());
		}
		return description;
	}
	
	public Object getFieldValue(String option) {
		Field field = null;
		Object object = null;
		try {
			field = getClass().getDeclaredField(option);
			field.setAccessible(true);
			object = field.get(this);
		} catch (NoSuchFieldException e) {
			try {
				field = getClass().getSuperclass().getDeclaredField(option);
				field.setAccessible(true);
				object = field.get(this);
			} catch (Exception e1) { 
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		return object;
	}
	
	public Material getIcon() {
		return manifest.icon();
	}

	/**
	 * Gets the current instance of this power class. Useful when the power class must be
	 * referenced from within a nested class or method (i.e. {@link Runnable}).
	 * @return This power class
	 */
	protected Power getInstance() {
		return this;
	}

	/**
	 * Gets the proper name of this power, as it appears in the {@link PowerManifest} annotation.
	 * @return This power's name
	 */
	public final String getName() {
		if (name == null) {
			name = locale("manifest.name", manifest.name());
		}
		return name;
	}
	
	@SuppressWarnings("unchecked")
	public <O> O getOption(PowerOption<O> option) {
		return (O) options.get(option);
	}
	
	public PowerOption<?> getOptionByName(String path) {
		for (PowerOption<?> option : options.keySet()) {
			if (option.getPath().equalsIgnoreCase(path)) {
				return option;
			}
		}
		return null;
	}
	
	public PowerOption<?> getOptionByField(String field) {
		return (PowerOption<?>) getFieldValue(field);
	}
	
	public Map<PowerOption<?>, Object> getOptions() {
		return options;
	}
	
	public static NamespacedKey getCollectorKey() {
		return collectorKey;
	}
	
	public ItemStack getRequiredItem() {
		return hasOption(item) ? getOption(item) : null;
	}
	
	public PowerStat getStat(String name) {
		for (PowerStat stat : stats.keySet()) {
			if (stat.getPath().equalsIgnoreCase(name)) {
				return stat;
			}
		}
		return null;
	}
	
	public Map<PowerStat, Integer> getStats() {
		return stats;
	}
	
	public int getStatValue(PowerStat stat) {
		return stats.containsKey(stat) ? stats.get(stat) : 0;
	}
	
	public List<ItemStack> getSupplies() {
		return supplies;
	}
	
	public final String getTag() {
		if (tag == null) {
			tag = locale("manifest.tag", getClass().getSimpleName());
		}
		return tag;
	}

	public final PowerType getType() {
		return manifest.type();
	}
	
	 public final Permission getUsePermission() {
		return perm;
	}

	/**
	 * Gets the {@link PowerUser} based on an OfflinePlayer instance.
	 * If a {@link PowerUser} doesn't exist, one is created with the player's UUID.
	 * @param player - The player instance
	 * @return The PowerUser instance for this player
	 */
	protected final PowerUser getUser(OfflinePlayer player) {
		return S86Powers.getConfigManager().getUser(player.getUniqueId());
	}

	/**
	 * Gets the {@link PowerUser} based on a player name.
	 * If a {@link PowerUser} doesn't exist, the plugin will attempt to determine the
	 * correct UUID based on the name and use that instead.
	 * <p>
	 * Note that using player instances or UUIDs is preferable in almost all cases.
	 * @param name - The player's name
	 * @return The PowerUser instance for this player
	 */
	protected final PowerUser getUser(String name) {
		return S86Powers.getConfigManager().getUser(name);
	}

	/**
	 * Gets the {@link PowerUser} based on a UUID.
	 * If a {@link PowerUser} doesn't exist, one is created with the specified UUID.
	 * @param uuid - The UUID of the player
	 * @return The PowerUser instance for this player
	 */
	protected final PowerUser getUser(UUID uuid) {
		return S86Powers.getConfigManager().getUser(uuid);
	}

	public Set<PowerUser> getUsers() {
		updateUsers();
		return users;
	}
	
	private boolean hasOption(PowerOption<?> option) {
		return options.containsKey(option);
	}
	
	boolean hasSupply(final int index) {
		return index >= 0 && index < supplies.size();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	protected boolean isTaskLive(int taskId) {
		return plugin.getServer().getScheduler().isCurrentlyRunning(taskId)
				|| plugin.getServer().getScheduler().isQueued(taskId);
	}
	
	protected String locale(String path, String defValue) {
		if (!defLocConfig.contains(path)
				|| !defLocConfig.getString(path).equals(defValue)) {
			defLocConfig.set(path, defValue);
			try {
				defLocConfig.save(defLocFile);
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					S86Powers.log(Level.INFO, LocaleString.SAVE_SUCCESS.build(defLocFile));
				}
			} catch (IOException e) {
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					S86Powers.log(Level.SEVERE, LocaleString.SAVE_FAIL.build(defLocFile));
				}
				e.printStackTrace();
			}
		}
		return locConfig.getString(path, defLocConfig.getString(path));
	}

	/**
	 * Tells the plugin to monitor this explosive. If grief protection is turned on,
	 * it will ensure no collateral damage occurs.
	 * @param explosive - The explosive to monitor
	 */
	protected void monitorExplosive(Explosive explosive) {
		plugin.getBlockListener().addExplosive(explosive);
	}

	/**
	 * Creates a configurable option.
	 * <p>
	 * Fields can be directly assigned a value of this method, as it should always return
	 * a value that is an instance of the default value.
	 * @param <O>
	 * @param <O> - The class of the default value
	 * @param path - The path used to reference this option in this power class' config file
	 * @param defValue - The default value of this option
	 * @param desc - A description of what this option does, useful for admins who wish to
	 * configure this power
	 * @return The configured value of this option
	 */
	protected <O> PowerOption<O> option(final String path, final O defValue, final String desc) {
		return option(path, defValue, desc, false);
	}
	
	@SuppressWarnings("unchecked")
	protected <O> PowerOption<O> option(final String path, final O defValue, final String desc, final boolean locked) {
		PowerOption<O> option = (PowerOption<O>) getOptionByName(path);
		if (option == null) {
			option = new PowerOption<O>(this, path, defValue, locale("options." + path + ".description", desc), locked);
		}
		if (!options.containsKey(option)
				&& !locked) {
			if (!config.contains("options." + option.getPath())) {
				config.set("options." + option.getPath(), defValue);
				saveConfig();
			}
			options.put(option, defValue instanceof Long ? config.getLong("options." + option.getPath(), (Long) defValue) :
					defValue instanceof Float ? (float) config.getDouble("options." + option.getPath(), (Float) defValue) :
					config.get("options." + option.getPath(), defValue));
		}
		return option;
	}
	
	public void refreshOptions() {
		config();
	}
	
	/**
	 * Shortcut method to Register the events of the specified Listener.
	 * Useful for nested classes with their own events.
	 * @param listener - The Listener class to register
	 */
	public void registerEvents(Listener listener) {
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
	}
	
	public void reload() {
		onEnable();
		config();
	}
	
	public void removeUser(PowerUser user) {
		onDisable(user);
		users.remove(user);
	}
	
	public void removeSupply(final int index) {
		supplies.remove(index);
	}
	
	/**
	 * Shortcut method to create a task from a {@link Runnable}.
	 * Tasks created this way are also stored, so they can be properly
	 * stopped should the power need to be disabled.
	 * @param runnable - The Runnable to create the task
	 * @return The resulting {@link BukkitTask}
	 */
	public BukkitTask runTask(Runnable runnable) {
		BukkitTask task = plugin.getServer().getScheduler().runTask(plugin, runnable);
		tasks.add(task.getTaskId());
		return task;
	}
	
	/**
	 * Shortcut method to create a delayed task from a {@link Runnable}.
	 * Tasks created this way are also stored, so they can be properly
	 * stopped should the power need to be disabled.
	 * @param runnable - The Runnable to create the task
	 * @param delay - Delay in game ticks before running this task
	 * @return The resulting {@link BukkitTask}
	 */
	public BukkitTask runTaskLater(Runnable runnable, long delay) {
		BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
		tasks.add(task.getTaskId());
		return task;
	}
	
	/**
	 * Shortcut method to create a repeating task from a {@link Runnable}.
	 * Tasks created this way are also stored, so they can be properly
	 * stopped should the power need to be disabled.
	 * @param runnable - The Runnable to create the task
	 * @param delay - Delay in game ticks before running this task
	 * @param period - Delay between following iterations of this task
	 * @return The resulting {@link BukkitTask}
	 */
	public BukkitTask runTaskTimer(Runnable runnable, long delay, long period) {
		BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
		tasks.add(task.getTaskId());
		return task;
	}

	public void saveConfig() {
		for (PowerOption<?> option : options.keySet()) {
			config.createSection("options." + option.getPath());
			config.set("options." + option.getPath(), options.get(option));
		}
		for (PowerStat stat : stats.keySet()) {
			config.createSection("stats." + stat.getPath());
			config.set("stats." + stat.getPath(), stats.get(stat));
		}
		for (int i = 0; i < supplies.size(); i ++) {
			config.createSection("supplies.item" + i);
			config.set("supplies.item" + i, supplies.get(i));
		}
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.SAVE_ATTEMPT.build(cFile));
		}
		try {
			config.save(cFile);
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.SAVE_SUCCESS.build(cFile));
			}
		} catch (IOException e) {
			plugin.getLogger().severe(LocaleString.SAVE_FAIL.build(cFile));
			e.printStackTrace();
		}
	}
	
	public boolean setEnabled(final boolean enable) {
		if (!locked) {
			if (enable) {
				enable();
				return true;
			}
			else {
				disable();
				return true;
			}
		}
		return false;
	}
	
	public void setLocked(boolean lock) {
		locked = lock;
	}
	
	public void setOption(PowerOption<?> option, Object value) {
		options.put(option, value);
		if (ConfigOption.Plugin.AUTO_SAVE) {
			saveConfig();
		}
		refreshOptions();
	}
	
	public void setStatValue(PowerStat stat, int value) {
		if (stats.containsKey(stat)) {
			stats.put(stat, value);
		}
	}
	
	public void setSupply(int index, ItemStack stack) {
		if (index >= supplies.size()) {
			supplies.add(stack);
		}
		else {
			supplies.set(index, stack);
		}
		if (ConfigOption.Plugin.AUTO_SAVE) {
			saveConfig();
		}
		refreshOptions();
	}
	
	/**
	 * Shortcut method to create debug messages that will show up in the console.
	 * Messages are prefixed with the power's class name, for easier identification.
	 * @param message - Message to be displayed
	 * @deprecated Solely for visibility within an IDE
	 */
	public void showDebug(String message) {
		S86Powers.showDebug(tag + " > " + message);
	}
	
	/**
	 * Creates a configurable stat for this power.
	 * @param path - The path used to reference this stat in this power class' config file
	 * @param defValue - The default value of this option
	 * @param desc - A description of what this stat is tracking, sent to the player any time they make progress
	 * @param reward - The message a player gets when they complete this stat, to explain what their reward is
	 * @return The configured {@link PowerStat} instance of this stat
	 */
	protected PowerStat stat(String path, int defValue, String description, String reward) {
		PowerStat stat = getStat(path);
		if (stat == null) {
			stat = new PowerStat(this, path, defValue, locale("stats." + path + ".description", description),
					locale("stats." + path + ".reward", reward));
		}
		if (!stats.containsKey(stat)) {
			if (!config.contains("stats." + stat.getPath())) {
				config.set("stats." + stat.getPath(), defValue);
			}
			stats.put(stat, config.getInt("stats." + stat.getPath(), defValue));
		}
		return stat;
	}

	/**
	 * Used to set which items should be supplied to a player who has this power
	 * when using a supply command.
	 * @param stacks - Items to set as supplies
	 */
	// TODO Pull from user's options
	protected void supplies(ItemStack... stacks) {
		if (supplies.isEmpty()) {
			supplies.addAll(Lists.newArrayList(stacks));
		}
	}
	
	/**
	 * Shortcut method to unregister all events within the specified Listener.
	 * Useful in nested classes which may have inactive instances.
	 * @param listener - The Listener to unregister events from
	 */
	public void unregisterEvents(Listener listener) {
		HandlerList.unregisterAll(listener);
	}

	private void updateUsers() {
		for (PowerUser user : Sets.newHashSet(users)) {
			if (!user.hasPower(this)) {
				users.remove(user);
			}
		}
		for (PowerUser user : S86Powers.getConfigManager().getUserList()) {
			if (user.hasPower(this)
					&& !users.contains(user)) {
				users.add(user);
			}
		}
	}
	
	@EventHandler
	private void onLoad(ServerLoadEvent event) {
		for (PowerUser user : getUsers()) {
			onEnable(user);
		}
	}

}
