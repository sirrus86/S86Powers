package me.sirrus86.s86powers.powers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

public class PowerContainer {

	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	private final Power power;
	
	public PowerContainer(final Power power) {
		this.power = power;
	}
	
	public static PowerContainer getContainer(Power power) {
		return S86Powers.getConfigManager().getContainer(power);
	}
	
	public String getAuthor() {
		return power.getManifest().author();
	}
	
	public String getConcept() {
		return power.getManifest().concept();
	}
	
	public Power getPower() {
		return this.power;
	}
	
	public void addUser(PowerUser user) {
		power.users.add(user);
	}
	
	public Set<PowerUser> getUsers() {
		return power.users;
	}
	
	public Material getIcon() {
		return power.getManifest().icon();
	}
	
	public void enable() {
		if (Arrays.asList(power.getClass().getInterfaces()).contains(Listener.class)) {
			plugin.getServer().getPluginManager().registerEvents(power, plugin);
		}
		reload();
		power.enabled = true;
	}
	
	public void enable(PowerUser user) {
		power.onEnable(user);
	}
	
	public void disable() {
		for (PowerUser user : power.getUsers()) {
			disable(user);
		}
		power.onDisable();
		if (Arrays.asList(power.getClass().getInterfaces()).contains(Listener.class)) {
			HandlerList.unregisterAll(power);
		}
		for (int i : power.tasks) {
			if (plugin.getServer().getScheduler().isCurrentlyRunning(i)
					|| plugin.getServer().getScheduler().isQueued(i)) {
				plugin.getServer().getScheduler().cancelTask(i);
			}
		}
		power.tasks.clear();
		power.enabled = false;
	}
	
	public void disable(PowerUser user) {
		power.onDisable(user);
	}
	
	public final Permission getAssignPermission() {
		return power.aPerm;
	}
	
	public final Permission getUsePermission() {
		return power.perm;
	}
	
	public ItemStack getConsumable() {
		return power.consumable;
	}
	
	public ItemStack getRequiredItem() {
		return power.item;
	}
	
	public long getCooldown() {
		return power.cooldown;
	}
	
	public PowerOption getOption(String path) {
		for (PowerOption option : power.options.keySet()) {
			if (option.getPath().equalsIgnoreCase(path)) {
				return option;
			}
		}
		return null;
	}
	
	public Map<PowerOption, Object> getOptions() {
		return power.options;
	}
	
	public Object getOptionValue(PowerOption option) {
		return power.options.containsKey(option) ? power.options.get(option) : null;
	}
	
	public Object getFieldValue(String option) {
		Field field = null;
		Object object = null;
		try {
			field = power.getClass().getDeclaredField(option);
			field.setAccessible(true);
			object = field.get(power);
		} catch (NoSuchFieldException e) {
			try {
				field = power.getClass().getSuperclass().getDeclaredField(option);
				field.setAccessible(true);
				object = field.get(power);
			} catch (NoSuchFieldException e1) { 
				return null;
			} catch(IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public PowerStat getStat(String name) {
		for (PowerStat stat : power.stats.keySet()) {
			if (stat.getPath().equalsIgnoreCase(name)) {
				return stat;
			}
		}
		return null;
	}
	
	public Map<PowerStat, Integer> getStats() {
		return power.stats;
	}
	
	public int getStatValue(PowerStat stat) {
		return power.getStatValue(stat);
	}
	
	public List<ItemStack> getSupplies() {
		return power.supplies;
	}
	
	boolean hasSupply(final int index) {
		return index >= 0 && index < power.supplies.size();
	}
	
	public void removeSupply(final int index) {
		power.supplies.remove(index);
	}
	
	public String getDescription() {
		return power.getManifest().description();
	}
	
	public String getFilteredText(String text) {
		String tmp = text;
		while(tmp.indexOf("[") != -1 && tmp.indexOf("]") != -1) {
			int i = tmp.indexOf("["),
					j = tmp.indexOf("]");
			String tag = tmp.substring(i, j + 1);
			String field = tmp.substring(i + 1, j);
			if (field.startsWith("act:")) {
				ItemStack item = (ItemStack) getFieldValue(field.substring(4));
				if (item != null) {
					tmp = tmp.replace(tag, PowerTools.getActionString(item));
				}
			}
			else {
				Object object = getFieldValue(field);
				if (object != null) {
					if (object instanceof Boolean) {
						String endTag = "[/" + field + "]";
						if (!Boolean.parseBoolean(object.toString())) {
							tmp = tmp.substring(0, tmp.indexOf(tag)) + tmp.substring(tmp.indexOf(endTag) + endTag.length());
						}
						else {
							tmp = tmp.replace(tag, "").replace(endTag, "");
						}
					}
					else if (object instanceof ItemStack) {
						tmp = tmp.replace(tag, PowerTools.getItemName((ItemStack)object));
					}
					else if (object instanceof Long) {
						tmp = tmp.replace(tag, PowerTime.asLongString((Long)object));
					}
					else {
						tmp = tmp.replace(tag, object.toString());
					}
				}
			}
		}
		char[] chars = tmp.toCharArray();
		Character.toUpperCase(chars[0]);
		for (int i = 2; i < chars.length; i ++) {
			if (chars[i - 2] == '.') Character.toUpperCase(chars[i]);
		}
		tmp = new String(chars);
		return tmp;
	}
	
	public String getTag() {
		return power.getClass().getSimpleName();
	}
	
	public boolean isEnabled() {
		return power.enabled;
	}
	
	public boolean isLocked() {
		return power.locked;
	}
	
	public void refreshOptions() {
		power.options();
	}
	
	public void reload() {
		power.onEnable();
		power.options();
	}
	
	public void removeUser(PowerUser user) {
		power.onDisable(user);
		power.users.remove(user);
	}
	
	public boolean setEnabled(final boolean enable) {
		if (!power.locked) {
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
		power.locked = lock;
	}
	
	public void setOption(PowerOption option, Object value) {
		power.options.put(option, value);
		if (ConfigOption.Plugin.AUTO_SAVE) {
			power.saveConfig();
		}
		refreshOptions();
	}
	
	public void setStatValue(PowerStat stat, int value) {
		if (power.stats.containsKey(stat)) {
			power.stats.put(stat, value);
		}
	}
	
	public void setSupply(int index, ItemStack stack) {
		if (index >= power.supplies.size()) {
			power.supplies.add(stack);
		}
		else {
			power.supplies.set(index, stack);
		}
		if (ConfigOption.Plugin.AUTO_SAVE) {
			power.saveConfig();
		}
		refreshOptions();
	}
	
}
