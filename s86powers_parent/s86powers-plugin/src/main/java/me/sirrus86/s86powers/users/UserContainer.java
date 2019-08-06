package me.sirrus86.s86powers.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.powers.internal.utility.NeutralizerBeacon.Beacon;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.tools.PowerTools;

public class UserContainer {

	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	private final PowerUser user;
	
	public UserContainer(final PowerUser user) {
		this.user = user;
	}
	
	public static UserContainer getContainer(PowerUser user) {
		return plugin.getConfigManager().getContainer(user);
	}
	
	public void addBeacon(Beacon beacon) {
		if (user.beacons.isEmpty()) {
			user.neutralize(LocaleString.NEUTRALIZED_BY_BEACON.toString());
		}
		user.beacons.add(beacon);
	}
	
	public void removeBeacon(Beacon beacon) {
		if (user.beacons.contains(beacon)) {
			user.beacons.remove(beacon);
			user.deneutralize(false);
		}
	}
	
	public Set<PowerGroup> getAssignedGroups() {
		return user.groups;
	}
	
	public void addGroup(PowerGroup group) {
		user.groups.add(group);
		if (!group.hasMember(user)) {
			group.addMember(user);
		}
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= user.saveTimer) {
			save();
		}
	}
	
	private void addGroupWithoutSaving(PowerGroup group) {
		user.groups.add(group);
		if (!group.hasMember(user)) {
			group.addMember(user);
		}
	}
	
	public void removeGroup(PowerGroup group) {
		user.groups.remove(group);
		if (group.hasMember(user)) {
			group.removeMember(user);
		}
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= user.saveTimer) {
			save();
		}
	}
	
	public void addPower(Power power) {
		addPower(power, true);
	}
	
	public void addPower(Power power, boolean enable) {
		if (power.getType() != PowerType.UTILITY) {
			if (!user.powers.containsKey(power)) {
				user.powers.put(power, enable);
			}
			PowerContainer.getContainer(power).addUser(user);
			PowerContainer.getContainer(power).enable(user);
			if (ConfigOption.Plugin.AUTO_SAVE
					&& System.currentTimeMillis() >= user.saveTimer) {
				save();
			}
		}
	}
	
	private void addPowerWithoutSaving(Power power, boolean enable) {
		if (!user.powers.containsKey(power)) {
			user.powers.put(power, enable);
		}
		PowerContainer.getContainer(power).addUser(user);
		PowerContainer.getContainer(power).enable(user);
	}
	
	public void removePower(Power power) {
		if (user.powers.containsKey(power)) {
			user.powers.remove(power);
		}
		PowerContainer.getContainer(power).removeUser(user);
		PowerContainer.getContainer(power).disable(user);
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= user.saveTimer) {
			save();
		}
	}
	
	public void addRegion(NeutralRegion region) {
		if (user.regions.isEmpty()) {
			user.neutralize(LocaleString.NEUTRALIZED_BY_REGION.toString());
		}
		user.regions.add(region);
	}
	
	public void removeRegion(NeutralRegion region) {
		if (user.regions.contains(region)) {
			user.regions.remove(region);
			user.deneutralize(false);
		}
	}
	
	public Set<Power> getAssignedPowers() {
		return user.powers.keySet();
	}
	
	public Set<Power> getAssignedPowersByType(PowerType type) {
		Set<Power> tmp = new HashSet<Power>();
		for (Power power : user.powers.keySet()) {
			if (power.getType() == type) {
				tmp.add(power);
			}
		}
		return tmp;
	}
	
	public Set<Beacon> getBeaconsInhabited() {
		return user.beacons;
	}
	
	public Set<Power> getGroupPowers() {
		Set<Power> tmp = new HashSet<Power>();
		for (PowerGroup group : getGroups()) {
			tmp.addAll(group.getPowers());
		}
		return tmp;
	}
	
	public Set<PowerGroup> getGroups() {
		Set<PowerGroup> tmp = new HashSet<PowerGroup>();
		tmp.addAll(getAssignedGroups());
		tmp.addAll(getPermissibleGroups());
		return tmp;
	}
	
	public Set<PowerGroup> getPermissibleGroups() {
		Set<PowerGroup> tmp = new HashSet<PowerGroup>();
		if (user.isOnline()
				&& ConfigOption.Plugin.ENABLE_PERMISSION_ASSIGNMENTS) {
			for (PowerGroup group : plugin.getConfigManager().getGroups()) {
				if (user.getPlayer().hasPermission(group.getRequiredPermission())) {
					tmp.add(group);
				}
			}
		}
		return tmp;
	}
	
	public Set<Power> getPermissiblePowers() {
		Set<Power> tmp = new HashSet<Power>();
		if (user.isOnline()
				&& ConfigOption.Plugin.ENABLE_PERMISSION_ASSIGNMENTS) {
			for (Power power : plugin.getConfigManager().getPowers()) {
				if (user.getPlayer().hasPermission(PowerContainer.getContainer(power).getUsePermission())) {
					tmp.add(power);
				}
			}
		}
		return tmp;
	}
	
	public Set<Power> getPowers() {
		return getPowers(false);
	}
	
	public Set<Power> getPowers(boolean includeUtility) {
		Set<Power> tmp = new HashSet<Power>();
		if (includeUtility) {
			tmp.addAll(plugin.getConfigManager().getPowersByType(PowerType.UTILITY));
		}
		tmp.addAll(getAssignedPowers());
		tmp.addAll(getGroupPowers());
		tmp.addAll(getPermissiblePowers());
		return tmp;
	}
	
	public Set<NeutralRegion> getRegionsInhabited() {
		return user.regions;
	}
	
	public boolean hasEnablePermission() {
		if (user.isOnline()) {
			return isAdmin() ? ConfigOption.Admin.BYPASS_PERMISSION : user.getPlayer().hasPermission(S86Permission.ENABLE);
		}
		return false;
	}
	
	public boolean hasPower(Power power) {
		return getPowers(true).contains(power);
	}
	
	public boolean hasPowerAssigned(Power power) {
		return user.powers.containsKey(power);
	}
	
	public boolean hasPowerEnabled(Power power) {
		return user.powers.containsKey(power) ? user.powers.get(power) : true;
	}
	
	public boolean hasPowersEnabled() {
		return user.enabled;
	}
	
	public boolean inGroup(PowerGroup group) {
		return user.groups.contains(group);
	}
	
	public boolean isAdmin() {
		return user.isOnline()
				&& user.getPlayer().hasPermission(S86Permission.ADMIN);
	}
	
	public boolean isNeutralized() {
		return !user.beacons.isEmpty()
				|| !user.regions.isEmpty()
				|| user.nTask > -1;
	}
	
	public void load() {
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.LOAD_ATTEMPT.build(user.cFile));
		}
		if (user.cFile != null) {
			user.config = YamlConfiguration.loadConfiguration(user.cFile);
			if (user.config.contains("powers")) {
				for (String pwr : user.config.getConfigurationSection("powers").getKeys(false)) {
					Power power = plugin.getConfigManager().getPower(pwr);
					if (power != null) {
						if (user.config.contains("powers." + pwr + ".active", false)) {
							addPowerWithoutSaving(power, user.config.getBoolean("powers." + pwr + ".active", false));
						}
						if (user.config.contains("powers." + pwr + ".stats")) {
							for (String statName : user.config.getConfigurationSection("powers." + pwr + ".stats").getKeys(false)) {
								PowerStat stat = PowerContainer.getContainer(power).getStat(statName);
								if (stat != null) {
									user.stats.put(stat, user.config.getInt("powers." + pwr + ".stats." + statName, 0));
								}
							}
						}
					}
				}
			}
			if (user.config.contains("groups")) {
				for (String grp : user.config.getStringList("groups")) {
					PowerGroup group = plugin.getConfigManager().getGroup(grp);
					if (group != null) {
						addGroupWithoutSaving(group);
					}
				}
			}
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.LOAD_SUCCESS.build(user.cFile));
			}
		}
		else {
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.LOAD_FAIL.build(user.cFile));
			}
			throw new NullPointerException();
		}
	}
	
	public void purge() {
		user.beacons = new HashSet<>();
		user.cooldowns = new HashMap<>();
		user.groups = new HashSet<>();
		user.powers = new HashMap<>();
		user.regions = new HashSet<>();
		user.stats = new HashMap<>();
	}

	public void save() {
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.SAVE_ATTEMPT.build(user.cFile));
		}
		if (user.cFile != null) {
			if (user.config == null) {
				user.config = YamlConfiguration.loadConfiguration(user.cFile);
			}
			user.config.set("groups", null);
			if (!getAssignedGroups().isEmpty()) {
				List<String> gList = new ArrayList<String>();
				for (PowerGroup group : getAssignedGroups()) {
					gList.add(group.getName());
				}
				user.config.set("groups", gList);
			}
			user.config.set("powers", null);
			if (!getAssignedPowers().isEmpty()) {
				for (Power power : getAssignedPowers()) {
					user.config.set("powers." + power.getClass().getSimpleName() + ".active", hasPowerEnabled(power));
				}
			}
			if (!user.stats.isEmpty()) {
				for (PowerStat stat : user.stats.keySet()) {
					user.config.set("powers." + stat.getPower().getClass().getSimpleName() + ".stats." + stat.getPath(), user.stats.get(stat));
				}
			}
			try {
				user.config.save(user.cFile);
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					plugin.getLogger().info(LocaleString.SAVE_SUCCESS.build(user.cFile));
				}
				user.saveTimer = System.currentTimeMillis() + ConfigOption.Plugin.AUTO_SAVE_COOLDOWN;
			} catch (IOException e) {
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					plugin.getLogger().info(LocaleString.SAVE_FAIL.build(user.cFile));
				}
				e.printStackTrace();
			}
		}
		else {
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.SAVE_FAIL.build(user.cFile));
			}
			throw new NullPointerException();
		}
	}
	
	public void setPowerEnabled(Power power, boolean newState) {
		if (user.powers.containsKey(power)) {
			user.powers.put(power, newState);
			if (!newState) {
				PowerContainer.getContainer(power).disable(user);
			}
		}
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= user.saveTimer) {
			save();
		}
	}
	
	public void setPowersEnabled(boolean newState) {
		user.enabled = newState;
	}
	
	public PowerUser getUser() {
		return this.user;
	}
	
	@SuppressWarnings("deprecation")
	public void supply(Power power) {
		if (user.isOnline()) {
			PowerContainer pCont = PowerContainer.getContainer(power);
			for (int i = 0; i < pCont.getSupplies().size(); i ++) {
				ItemStack item = pCont.getSupplies().get(i);
				for (ItemStack stack : user.getPlayer().getInventory().getContents()) {
					if (stack != null) {
						if (PowerTools.usesDurability(item)
								&& item.getType() == stack.getType()) {
							stack.setDurability((short) 0);
							return;
						}
						else if (item.getType() == stack.getType()
								&& item.getDurability() == stack.getDurability()) {
							if (stack.getAmount() < item.getAmount()) {
								stack.setAmount(item.getAmount());
							}
							return;
						}
					}
				}
				int j = user.getPlayer().getInventory().firstEmpty();
				if (j > -1) {
					user.getPlayer().getInventory().setItem(j, item);
				}
				else {
					user.getPlayer().getWorld().dropItem(user.getPlayer().getLocation(), item);
				}
			}
		}
	}
	
}
