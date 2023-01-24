package me.sirrus86.s86powers.users;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

public class PowerGroup implements Comparable<PowerGroup> {

	private final Set<PowerUser> members = new HashSet<>();
	private final Set<Power> powers = new HashSet<>();

	private final File cFile;
	private YamlConfiguration config;
	private final String name;
	private final Permission perm;
	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	
	public PowerGroup(String name) {
		this.name = name;
		cFile = new File(plugin.getGroupDirectory(), name + ".yml");
		perm = new Permission("s86powers.group." + name, "Allows player to use powers assigned to group '" + name + "'.", PermissionDefault.FALSE);
		if (!plugin.getServer().getPluginManager().getPermissions().contains(perm)) {
			plugin.getServer().getPluginManager().addPermission(perm);
		}
	}
	
	public void addMember(PowerUser user) {
		members.add(user);
		if (!user.inGroup(this)) {
			user.addGroup(this);
		}
	}
	
	public void addPower(Power power) {
		powers.add(power);
	}
	
	@Override
	public int compareTo(PowerGroup group) {
		return getName().compareTo(group.getName());
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void disband() {
		List<PowerUser> members = Lists.newArrayList(getMembers());
		for (PowerUser member : members) {
			member.removeGroup(this);
		}
		cFile.delete();
	}
	
	public Set<PowerUser> getMembers() {
		return members;
	}
	
	public final String getName() {
		return name;
	}
	
	public Set<Power> getPowers() {
		return powers;
	}
	
	public final Permission getRequiredPermission() {
		return perm;
	}
	
	public boolean hasMember(PowerUser user) {
		return members.contains(user);
	}
	
	public boolean hasPower(Power power) {
		return powers.contains(power);
	}
	
	public void load() {
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.LOAD_ATTEMPT.build(cFile));
		}
		if (cFile != null) {
			config = YamlConfiguration.loadConfiguration(cFile);
			if (config.contains("players")) {
				for (String uName : config.getStringList("players")) {
					PowerUser user = S86Powers.getConfigManager().getUser(UUID.fromString(uName));
					if (user != null) {
						addMember(user);
					}
				}
			}
			if (config.contains("powers")) {
				for (String pwr : config.getStringList("powers")) {
					Power power = S86Powers.getConfigManager().getPower(pwr);
					if (power != null) {
						addPower(power);
					}
				}
			}
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.LOAD_SUCCESS.build(cFile));
			}
		}
		else {
			throw new NullPointerException();
		}
	}
	
	public void removeMember(PowerUser user) {
		members.remove(user);
		if (user.inGroup(this)) {
			user.removeGroup(this);
		}
		for (Power power : powers) {
			if (!user.hasPowerAssigned(power)) {
				power.disable(user);
			}
		}
	}
	
	public void removePower(Power power) {
		powers.remove(power);
		for (PowerUser user : members) {
			if (!user.hasPowerAssigned(power)) {
				power.disable(user);
			}
		}
	}
	
	public void save() {
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.SAVE_ATTEMPT.build(cFile));
		}
		if (cFile != null) {
			if (config == null) {
				config = YamlConfiguration.loadConfiguration(cFile);
			}
			config.set("players", null);
			if (!members.isEmpty()) {
				List<String> uList = new ArrayList<>();
				for (PowerUser user : members) {
					uList.add(user.getUUID().toString());
				}
				config.set("players", uList);
			}
			config.set("powers", null);
			if (!powers.isEmpty()) {
				List<String> pList = new ArrayList<>();
				for (Power power : powers) {
					pList.add(power.getTag());
				}
				config.set("powers", pList);
			}
			try {
				config.save(cFile);
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					plugin.getLogger().info(LocaleString.SAVE_SUCCESS.build(cFile));
				}
			} catch (IOException e) {
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					plugin.getLogger().severe(LocaleString.SAVE_FAIL.build(cFile));
				}
				e.printStackTrace();
			}
		}
		else {
			throw new NullPointerException();
		}
	}
	
}
