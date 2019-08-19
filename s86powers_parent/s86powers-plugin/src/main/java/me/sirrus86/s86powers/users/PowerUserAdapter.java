package me.sirrus86.s86powers.users;

import java.util.Set;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.powers.internal.utility.NeutralizerBeacon.Beacon;
import me.sirrus86.s86powers.regions.NeutralRegion;

public class PowerUserAdapter {

	private final PowerUser user;
	
	public PowerUserAdapter(final PowerUser user) {
		this.user = user;
	}
	
	public static PowerUserAdapter getAdapter(PowerUser user) {
		return S86Powers.getConfigManager().getAdapter(user);
	}
	
	public void addBeacon(Beacon beacon) {
		user.addBeacon(beacon);
	}
	
	public Set<PowerGroup> getAssignedGroups() {
		return user.getAssignedGroups();
	}
	
	public void addGroup(PowerGroup group) {
		user.addGroup(group);
	}
	
	public void removeGroup(PowerGroup group) {
		user.removeGroup(group);
	}
	
	public void addPower(Power power) {
		user.addPower(power, true);
	}
	
	public void addPower(Power power, boolean enable) {
		user.addPower(power, enable);
	}
	
	public void removePower(Power power) {
		user.removePower(power);
	}
	
	public void addRegion(NeutralRegion region) {
		user.addRegion(region);
	}
	
	public void removeRegion(NeutralRegion region) {
		user.removeRegion(region);
	}
	
	public Set<Power> getAssignedPowers() {
		return user.getAssignedPowers();
	}
	
	public Set<Power> getAssignedPowersByType(PowerType type) {
		return user.getAssignedPowersByType(type);
	}
	
	public Set<Beacon> getBeaconsInhabited() {
		return user.getBeaconsInhabited();
	}
	
	public Set<Power> getGroupPowers() {
		return user.getGroupPowers();
	}
	
	public Set<PowerGroup> getGroups() {
		return user.getGroups();
	}
	
	public Set<PowerGroup> getPermissibleGroups() {
		return user.getPermissibleGroups();
	}
	
	public Set<Power> getPermissiblePowers() {
		return user.getPermissiblePowers();
	}
	
	public Set<Power> getPowers() {
		return user.getPowers();
	}
	
	public Set<Power> getPowers(boolean includeUtility) {
		return user.getPowers(includeUtility);
	}
	
	public Set<NeutralRegion> getRegionsInhabited() {
		return user.getRegionsInhabited();
	}
	
	public boolean hasEnablePermission() {
		return user.hasEnablePermission();
	}
	
	public boolean hasPower(Power power) {
		return user.hasPower(power);
	}
	
	public boolean hasPower(String name) {
		return user.hasPower(name);
	}
	
	public boolean hasPowerAssigned(Power power) {
		return user.hasPowerAssigned(power);
	}
	
	public boolean hasPowerEnabled(Power power) {
		return user.hasPowerEnabled(power);
	}
	
	public boolean hasPowersEnabled() {
		return user.hasPowersEnabled();
	}
	
	public boolean inGroup(PowerGroup group) {
		return user.inGroup(group);
	}
	
	public boolean isAdmin() {
		return user.isAdmin();
	}
	
	public boolean isNeutralized() {
		return user.isNeutralized();
	}
	
	public void load() {
		user.load();
	}
	
	public void purge() {
		user.purge();
	}
	
	public void removeBeacon(Beacon beacon) {
		user.removeBeacon(beacon);
	}

	public void save() {
		user.save();
	}
	
	public void setPowerEnabled(Power power, boolean newState) {
		user.setPowerEnabled(power, newState);
	}
	
	public void setPowersEnabled(boolean newState) {
		user.setPowersEnabled(newState);;
	}
	
	public PowerUser getUser() {
		return this.user;
	}
	
	public void supply(Power power) {
		user.supply(power);
	}
	
}
