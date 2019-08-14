package me.sirrus86.s86powers.powers;

import java.io.File;
import java.io.IOException;
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

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.UserContainer;

import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Explosive;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Lists;

/**
 * Abstract class for a power class in S86 Powers. All power classes must extend this class in order to be loaded.
 */
public abstract class Power implements Comparable<Power>, Listener {
	
	Map<PowerOption, Object> options = new HashMap<>();
	List<ItemStack> supplies = new ArrayList<>();
	Map<PowerStat, Integer> stats = new HashMap<>();
	Set<Integer> tasks = new HashSet<>();
	Set<PowerUser> users = new HashSet<>();
	
	final File cFile;
	final YamlConfiguration config;
	boolean enabled = true,
			locked = false;
	final Permission aPerm, perm;
	@Deprecated
	protected boolean incomplete = false;
	private final PowerManifest manifest = getClass().getAnnotation(PowerManifest.class);
	private final S86Powers plugin;
	private String tag = getClass().getSimpleName();
	
	/**
	 * Dedicated instance of {@link java.util.Random} used to create random values where needed.
	 */
	protected static final Random random = new Random();
	
	/**
	 * Dedicated field to represent the cooldown of a power.
	 */
	protected long cooldown;
	
	/**
	 * Dedicated field to represent the consumable item of a power, if it is different from the use item.
	 */
	protected ItemStack consumable;
	
	/**
	 * Dedicated field to represent the use item of a power.
	 */
	protected ItemStack item;
	
	/**
	 * {@link boolean} value representing whether any {@link ItemStack} which is an axe can be used to activate this power.
	 */
	protected boolean wAxe = false;
	
	/**
	 * {@link boolean} value representing whether the specified {@link ItemStack} (set my using the field {@code item}) can be used to activate this power.
	 */
	protected boolean wItem = true;
	
	/**
	 * {@link boolean} value representing whether any {@link ItemStack} which is a sword can be used to activate this power.
	 */
	protected boolean wSword = false;
	
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
	protected abstract void options();

	public Power() {
		plugin = JavaPlugin.getPlugin(S86Powers.class);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		cFile = new File(plugin.getPowerDirectory(), tag + ".yml");
		if (!cFile.exists()) {
			try {
				cFile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().severe(LocaleString.FILE_CREATE_FAIL.build(cFile));
				e.printStackTrace();
			}
		}
		config = YamlConfiguration.loadConfiguration(cFile);
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.LOAD_SUCCESS.build(cFile));
		}
		aPerm = new Permission("s86powers.assign." + tag, "Allows player to assign " + getName() + ".", PermissionDefault.TRUE);
		perm = new Permission("s86powers.power." + tag, "Allows player to use  " + getName() + " regardless if assigned or not.", PermissionDefault.FALSE);
		if (!plugin.getServer().getPluginManager().getPermissions().contains(aPerm)) {
			plugin.getServer().getPluginManager().addPermission(aPerm);
		}
		if (!plugin.getServer().getPluginManager().getPermissions().contains(perm)) {
			plugin.getServer().getPluginManager().addPermission(perm);
		}
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
		return wAxe;
	}
	
	/**
	 * Determines whether any {@link ItemStack} which represents the item specified by the field {@code item} can be used to activate this power.
	 * @return {@link boolean} value of the field {@code wItem}
	 */
	public boolean canUseSpecificItem() {
		return wItem;
	}
	
	/**
	 * Determines whether any {@link ItemStack} which represents a sword can be used to activate this power.
	 * @return {@link boolean} value of the field {@code wSword}
	 */
	public boolean canUseAnySword() {
		return wSword;
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
		return new NamespacedKey(plugin, tag.toLowerCase() + "." + key);
	}

	/**
	 * Gets the config file used by this power to hold info on options and stats.
	 * This file can also be used to hold persistent information for the power.
	 * @return This power's config file
	 */
	public final YamlConfiguration getConfig() {
		return config;
	}

	/**
	 * Gets the current instance of this power class. Useful when the power class must be
	 * referenced from within a nested class or method (i.e. {@link Runnable}).
	 * @return This power class
	 */
	protected Power getInstance() {
		return this;
	}
	
	PowerManifest getManifest() {
		return manifest;
	}

	/**
	 * Gets the proper name of this power, as it appears in the {@link PowerManifest} annotation.
	 * @return This power's name
	 */
	public final String getName() {
		return manifest.name();
	}

	private PowerOption getOption(String path) {
		for (PowerOption option : options.keySet()) {
			if (option.getPath().equalsIgnoreCase(path)) {
				return option;
			}
		}
		return null;
	}
	
	private PowerStat getStat(String name) {
		for (PowerStat stat : stats.keySet()) {
			if (stat.getPath().equalsIgnoreCase(name)) {
				return stat;
			}
		}
		return null;
	}
	
	public int getStatValue(PowerStat stat) {
		return stats.containsKey(stat) ? stats.get(stat) : 0;
	}

	public final PowerType getType() {
		return manifest.type();
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
	
	protected boolean isTaskLive(int taskId) {
		return plugin.getServer().getScheduler().isCurrentlyRunning(taskId)
				|| plugin.getServer().getScheduler().isQueued(taskId);
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
	 * @param <O> - The class of the default value
	 * @param path - The path used to reference this option in this power class' config file
	 * @param defValue - The default value of this option
	 * @param desc - A description of what this option does, useful for admins who wish to
	 * configure this power
	 * @return The configured value of this option
	 */
	@SuppressWarnings("unchecked")
	protected <O> O option(final String path, final O defValue, final String desc) {
		PowerOption option = getOption(path);
		if (option == null) {
			option = new PowerOption(this, path, defValue, desc);
		}
		if (!options.containsKey(option)) {
			if (!config.contains("options." + option.getPath())) {
				config.set("options." + option.getPath(), defValue);
				saveConfig();
			}
			options.put(option, defValue instanceof Long ? config.getLong("options." + option.getPath(), (Long) defValue) : config.get("options." + option.getPath(), defValue));
		}
		return (O) options.get(option);
	}
	
	/**
	 * Shortcut method to Register the events of the specified Listener.
	 * Useful for nested classes with their own events.
	 * @param listener - The Listener class to register
	 */
	public void registerEvents(Listener listener) {
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
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
		for (PowerOption option : options.keySet()) {
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
	
	/**
	 * Shortcut method to create debug messages that will show up in the console.
	 * Messages are prefixed with the power's class name, for easier identification.
	 * @param message - Message to be displayed
	 * @deprecated Solely for visibility within an IDE
	 */
	protected void showDebug(String message) {
		plugin.showDebug(tag + " > " + message);
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
			stat = new PowerStat(this, path, defValue, description, reward);
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
		for (PowerUser user : users) {
			if (!UserContainer.getContainer(user).hasPower(this)) {
				users.remove(user);
			}
		}
		for (PowerUser user : S86Powers.getConfigManager().getUserList()) {
			if (UserContainer.getContainer(user).hasPower(this)
					&& !users.contains(user)) {
				users.add(user);
			}
		}
	}

}
