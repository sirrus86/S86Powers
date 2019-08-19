package me.sirrus86.s86powers.powers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.users.PowerUser;

public class PowerAdapter {

	private final Power power;
	
	public PowerAdapter(final Power power) {
		this.power = power;
	}
	
	public static PowerAdapter getAdapter(Power power) {
		return S86Powers.getConfigManager().getAdapter(power);
	}
	
	public String getAuthor() {
		return power.getAuthor();
	}
	
	public String getConcept() {
		return power.getConcept();
	}
	
	public Power getPower() {
		return this.power;
	}
	
	public void addUser(PowerUser user) {
		power.addUser(user);
	}
	
	public Set<PowerUser> getUsers() {
		return power.getUsers();
	}
	
	public Material getIcon() {
		return power.getIcon();
	}
	
	public void enable() {
		power.enable();
	}
	
	public void enable(PowerUser user) {
		power.enable(user);
	}
	
	public void disable() {
		power.disable();
	}
	
	public void disable(PowerUser user) {
		power.disable(user);
	}
	
	public final Permission getAssignPermission() {
		return power.getAssignPermission();
	}
	
	public final Permission getUsePermission() {
		return power.getUsePermission();
	}
	
	public ItemStack getConsumable() {
		return power.getConsumable();
	}
	
	public ItemStack getRequiredItem() {
		return power.getRequiredItem();
	}
	
	public long getCooldown() {
		return power.getCooldown();
	}
	
	public PowerOption getOption(String path) {
		return power.getOption(path);
	}
	
	public Map<PowerOption, Object> getOptions() {
		return power.getOptions();
	}
	
	public Object getOptionValue(PowerOption option) {
		return power.getOptionValue(option);
	}
	
	public Object getFieldValue(String option) {
		return power.getFieldValue(option);
	}
	
	public PowerStat getStat(String name) {
		return power.getStat(name);
	}
	
	public Map<PowerStat, Integer> getStats() {
		return power.getStats();
	}
	
	public int getStatValue(PowerStat stat) {
		return power.getStatValue(stat);
	}
	
	public List<ItemStack> getSupplies() {
		return power.getSupplies();
	}
	
	public boolean hasSupply(final int index) {
		return power.hasSupply(index);
	}
	
	public void removeSupply(final int index) {
		power.removeSupply(index);;
	}
	
	public String getDescription() {
		return power.getDescription();
	}
	
	public String getTag() {
		return power.getTag();
	}
	
	public boolean isEnabled() {
		return power.isEnabled();
	}
	
	public boolean isLocked() {
		return power.isLocked();
	}
	
	public void refreshOptions() {
		power.refreshOptions();;
	}
	
	public void reload() {
		power.reload();;
	}
	
	public void removeUser(PowerUser user) {
		power.removeUser(user);
	}
	
	public boolean setEnabled(final boolean enable) {
		return power.setEnabled(enable);
	}
	
	public void setLocked(boolean lock) {
		power.setLocked(lock);
	}
	
	public void setOption(PowerOption option, Object value) {
		power.setOption(option, value);
	}
	
	public void setStatValue(PowerStat stat, int value) {
		power.setStatValue(stat, value);
	}
	
	public void setSupply(int index, ItemStack stack) {
		power.setSupply(index, stack);
	}
	
}
