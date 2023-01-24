package me.sirrus86.s86powers.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.UUIDFetcher;

public class ConfigManager {

	private final static String BLOCKED_POWERS = "blocked-powers";
	
	private final Set<String> blocked = new HashSet<>();
	private final Set<PowerGroup> groups = new HashSet<>();
	private final Map<String, NeutralRegion> regions = new HashMap<>();
	private final Map<String, Object> options = new HashMap<>();
	private final Set<Power> powers = new HashSet<>();
	private final Map<UUID, PowerUser> users = new HashMap<>();
	private final YamlConfiguration rgnConfig, plgConfig, pwrConfig;
	private final File plgFile;
	private final File pwrFile;
	private final S86Powers plugin;
	
	public ConfigManager(S86Powers plugin) {
		this.plugin = plugin;
		File rgnFile = new File(plugin.getDataFolder(), "regions.yml");
		plgFile = new File(plugin.getDataFolder(), "config.yml");
		pwrFile = new File(plugin.getDataFolder(), "powers.yml");
		createFiles(rgnFile, plgFile, pwrFile);
		rgnConfig = YamlConfiguration.loadConfiguration(rgnFile);
		plgConfig = YamlConfiguration.loadConfiguration(plgFile);
		pwrConfig = YamlConfiguration.loadConfiguration(pwrFile);
		ConfigurationSerialization.registerClass(NeutralRegion.class);
	}
	
	public void addGroup(PowerGroup group) {
		groups.add(group);
	}
	
	public boolean addPower(Power power) {
		return powers.add(power);
	}
	
	public void addRegion(NeutralRegion region) {
		regions.put(region.getName(), region);
	}
	
	public boolean blockPower(Power power) {
		power.disable();
		return blocked.add(power.getClass().getSimpleName());
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void createFiles(File... files) {
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
	}
	
	public void disablePowers() {
		for (Power power : powers) {
			power.disable();
		}
	}
	
	public YamlConfiguration getConfig() {
		return plgConfig;
	}
	
	public Map<String, Object> getConfigOptions() {
		return options;
	}
	
	public Object getConfigValue(String option) {
		return options.get(option);
	}
	
	public PowerGroup getGroup(String name) {
		for (PowerGroup group : groups) {
			if (group.getName().equalsIgnoreCase(name)) {
				return group;
			}
		}
		return null;
	}
	
	public Set<PowerGroup> getGroups() {
		return groups;
	}
	
	public Power getPower(String name) {
		for (Power power : powers) {
			if (power.getTag().equalsIgnoreCase(name)
					|| power.getName().replace("_", " ").equalsIgnoreCase(name)) {
				return power;
			}
		}
		return null;
	}
	
	public Set<Power> getPowers() {
		return powers;
	}
	
	public Set<Power> getPowersByType(PowerType type) {
		Set<Power> tmp = new HashSet<>();
		for (Power power : powers) {
			if (power.getType() == type) {
				tmp.add(power);
			}
		}
		return tmp;
	}
	
	public NeutralRegion getRegion(String name) {
		return regions.get(name);
	}
	
	public Collection<NeutralRegion> getRegions() {
		return regions.values();
	}
	
	public PowerUser getUser(final String name) {
		for (PowerUser user : users.values()) {
			if (user.getName() != null
					&& user.getName().equalsIgnoreCase(name)) {
				return user;
			}
		}
		try {
			return getUser(getUUID(name));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public PowerUser getUser(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid);
		}
		else if (uuid != null){
			PowerUser user = new PowerUser(uuid);
			users.put(uuid, user);
			user.load();
			return user;
		}
		else {
			return null;
		}
	}
	
	public Collection<PowerUser> getUserList() {
		return users.values();
	}
	
	private UUID getUUID(String name) throws Exception {
		return new UUIDFetcher(Collections.singletonList(name)).call().get(name);
	}
	
	public boolean hasGroup(String name) {
		for (PowerGroup group : groups) {
			if (group.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasUser(UUID uuid) {
		return users.containsKey(uuid);
	}
	
	public boolean isBlocked(String name) {
		return blocked.contains(name);
	}
	
	public void loadGroups() {
		String[] fileList = plugin.getGroupDirectory().list();
		if (fileList != null) {
			for (String file : fileList) {
				if (file.endsWith(".yml")) {
					String gName = file.substring(0, file.indexOf(".yml"));
					PowerGroup group = hasGroup(gName) ? getGroup(gName) : new PowerGroup(gName);
					group.load();
					groups.add(group);
				}
			}
		}
	}
	
	public void loadNeutralRegions() {
		for (String name : rgnConfig.getKeys(false)) {
			NeutralRegion region = rgnConfig.getSerializable(name, NeutralRegion.class);
			regions.put(name, region);
		}
	}
	
	public void loadPluginConfig() {
		for (Class<?> clazz : ConfigOption.class.getClasses()) {
			for (Field field : clazz.getFields()) {
				String path = clazz.getSimpleName().toLowerCase() + "." + field.getName().replace("_", "-").toLowerCase();
				try {
					if (!options.containsKey(path)) {
						options.put(path, field.get(null));
					}
					if (!plgConfig.contains(path)) {
						plgConfig.set(path, field.get(null));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void loadPowerConfig() {
		if (!pwrConfig.contains(BLOCKED_POWERS)) {
			pwrConfig.createSection(BLOCKED_POWERS);
			pwrConfig.set(BLOCKED_POWERS, new ArrayList<String>());
		}
		blocked.addAll(pwrConfig.getStringList(BLOCKED_POWERS));
	}
	
	public void loadUsers() {
		for (OfflinePlayer player : plugin.getServer().getOfflinePlayers()) {
			PowerUser user;
			if (!users.containsKey(player.getUniqueId())) {
				user = new PowerUser(player.getUniqueId());
				users.put(player.getUniqueId(), user);
			}
			else {
				user = users.get(player.getUniqueId());
			}
			user.load();
		}
	}
	
	public void removeGroup(PowerGroup group) {
		group.disband();
		groups.remove(group);
	}
	
	public void removePower(Power power) {
		powers.remove(power);
	}
	
	public void removeRegion(NeutralRegion region) {
		regions.remove(region.getName());
		region.deactivate();
	}
	
	private void save(YamlConfiguration config, File file) {
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.SAVE_ATTEMPT.build(file));
		}
		try {
			config.save(file);
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.SAVE_SUCCESS.build(file));
			}
		} catch (IOException e) {
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().severe(LocaleString.SAVE_SUCCESS.build(file));
			}
			e.printStackTrace();
		}
	}
	
	public void saveAll() {
		for (PowerGroup group : groups) {
			group.save();
		}
		for (PowerUser user : users.values()) {
			user.save();
		}
		savePluginConfig();
		savePowerConfig();
	}
	
	public void savePluginConfig() {
		for (String option : options.keySet()) {
			plgConfig.set(option, options.get(option));
		}
		save(plgConfig, plgFile);
	}
	
	public void savePowerConfig() {
		pwrConfig.set(BLOCKED_POWERS, Lists.newArrayList(blocked));
		save(pwrConfig, pwrFile);
	}
	
	public boolean setConfigValue(String option, Object value) {
		if (options.containsKey(option)) {
			Class<?> clazz = options.get(option).getClass();
			Object obj = validate(clazz, value);
			if (obj != null) {
				options.put(option, obj);
				if (ConfigOption.Plugin.AUTO_SAVE) {
					savePluginConfig();
				}
				return true;
			}
			else {
				if (ConfigOption.Plugin.SHOW_INPUT_ERRORS) {
					plugin.getLogger().warning(LocaleString.SET_OPTION_FAIL.build(option, value, clazz));
				}
			}
		}
		return false;
	}
	
	public boolean unblockPower(String name) {
		return blocked.remove(name);
	}
	
	private Object validate(Class<?> clazz, Object value) {
		String test = value.toString();
		if ((clazz == boolean.class || clazz == Boolean.class)
				&& (test.equalsIgnoreCase("false") || test.equalsIgnoreCase("true"))) {
			return Boolean.parseBoolean(test);
		}
		else if (clazz == int.class || clazz == Integer.class) {
			try {
				return Integer.parseInt(test);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (clazz == String.class) {
			return test;
		}
		return null;
	}
	
}
