package me.sirrus86.s86powers.users;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.events.PowerDamageEvent;
import me.sirrus86.s86powers.events.UserMaxedStatEvent;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerFire;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.powers.internal.utility.NeutralizerBeacon.Beacon;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.utils.PowerTime;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

/**
 * Class for any potential user of any power.
 * <p>
 * S86 Powers ultimately considers every player a PowerUser, regardless of whether they have or use powers.
 */
public final class PowerUser implements Comparable<PowerUser> {

	private Set<Beacon> beacons = new HashSet<>();
	private Map<Power, Long> cooldowns = new HashMap<>();
	private Set<PowerGroup> groups = new HashSet<>();
	private Map<PowerOption<?>, Object> options = new HashMap<>();
	private Map<Power, Boolean> powers = new HashMap<>();
	private Set<NeutralRegion> regions = new HashSet<>();
	private Map<PowerStat, Integer> stats = new HashMap<>();
	
	private final File cFile;
	private YamlConfiguration config;
	private boolean enabled = true;
	private String name;
	private int nTask = -1;
	private long nTimer = 0L;
	private OfflinePlayer oPlayer;
	private long saveTimer = 0L;
	private final UUID uuid;
	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	
	/**
	 * Creates a new instance of a PowerUser with the supplied {@link UUID}.
	 * <p>
	 * New PowerUsers should not need to be created from within power classes. To get the PowerUser instance of a player, use {@link Power#getUser(OfflinePlayer)}.
	 * @param uuid - {@link UUID} of the player
	 */
	public PowerUser(UUID uuid) {
		this.uuid = uuid;
		this.name = getOfflinePlayer().getName();
		String filename = this.uuid.toString();
		if (!plugin.getServer().getOnlineMode()
				|| ConfigOption.Users.SAVE_FILES_BY_NAME) {
			filename = this.name;
		}
		this.cFile = new File(plugin.getUserDirectory(), filename + ".yml");
		if (!cFile.exists()) {
			try {
				cFile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().severe(LocaleString.FILE_CREATE_FAIL.build(cFile));
				e.printStackTrace();
			}
		}
	}
	
	public void addBeacon(Beacon beacon) {
		if (beacons.isEmpty()) {
			neutralize(LocaleString.NEUTRALIZED_BY_BEACON.toString());
		}
		beacons.add(beacon);
		if (ConfigOption.Powers.SHOW_NEUTRALIZING_BEACON) {
			PowerTools.addSpectralBlock(getPlayer(), beacon.getBlock(), ChatColor.BLUE);
		}
	}
	
	void addGroup(PowerGroup group) {
		groups.add(group);
		if (!group.hasMember(this)) {
			group.addMember(this);
		}
		autosave();
	}
	
	private void addGroupWithoutSaving(PowerGroup group) {
		groups.add(group);
		if (!group.hasMember(this)) {
			group.addMember(this);
		}
	}
	
	public void addItems(ItemStack... items) {
		if (isOnline()) {
			Map<Integer, ItemStack> added = getPlayer().getInventory().addItem(items);
			if (!added.isEmpty()) {
				for (ItemStack stack : added.values()) {
					getPlayer().getWorld().dropItemNaturally(getPlayer().getLocation(), stack);
				}
			}
		}
	}
	
	/**
	 * Adds a potion effect discretely to the user.
	 * <p>
	 * Note: If the player is offline this will silently fail.
	 * @param effect - {@link PotionEffect} to apply to this user
	 */
	public boolean addPotionEffect(PotionEffect effect) {
		if (isOnline()) {
			return getPlayer().addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier(), false, false, false));
		}
		return false;
	}
	
	/**
	 * Adds several potion effects to the user.
	 * <p>
	 * Note: If the player is offline this will silently fail.
	 * @param effects - {@link PotionEffect}s to apply to this user
	 */
	public void addPotionEffects(Collection<PotionEffect> effects) {
		if (isOnline()
				&& effects != null
				&& !effects.isEmpty()) {
			getPlayer().addPotionEffects(effects);
		}
	}
	
	public void addPower(Power power) {
		addPower(power, true);
	}
	
	public void addPower(Power power, boolean enable) {
		if (power != null
				&& !powers.containsKey(power)) {
			powers.put(power, enable);
		}
	}
	
	private void addPowerWithoutSaving(Power power, boolean enable) {
		if (!powers.containsKey(power)) {
			powers.put(power, enable);
		}
		power.addUser(this);
		power.enable(this);
	}
	
	public void addRegion(NeutralRegion region) {
		if (regions.isEmpty()) {
			neutralize(LocaleString.NEUTRALIZED_BY_REGION.toString());
		}
		regions.add(region);
	}
	
	/**
	 * Determines whether the specified power can be used by this user at this time.
	 * <p>
	 * This will automatically check whether the player is online, whether they have the power assigned in any capacity (directly assigned, permission, etc), and that they're not neutralized.
	 * <p>
	 * Powers with {@link PowerType#UTILITY} will always return true.
	 * @param power - {@link Power} to check
	 * @return <b>true</b> if player is online, power is assigned, power is enabled, and user is not neutralized
	 */
	public boolean allowPower(Power power) {
		if (power.getType() == PowerType.UTILITY) {
			return true;
		}
		else if (this.getPlayer() != null
				&& this.getPlayer().isOnline()
				&& this.hasPowersEnabled()
				&& !this.isNeutralized()) {
			return (this.hasPower(power) && this.hasPowerEnabled(power))
					|| this.getPlayer().hasPermission(power.getUsePermission());
		}
		else {
			return false;
		}
	}
	
	private void autosave() {
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= saveTimer) {
			save();
		}
	}
	
	/**
	 * Attributes an {@link EntityDamageByEntityEvent} to the specified power.
	 * <p>
	 * Use this method during the event to attribute it as damage caused by the specified power.
	 * @param power - {@link Power} that caused this damage
	 * @param event - {@link EntityDamageByEntityEvent} where this damage already took place
	 */
	public void causeDamage(Power power, EntityDamageByEntityEvent event) {
		causeDamage(power, event, Double.MAX_VALUE);
	}
	
	/**
	 * Attributes an {@link EntityDamageByEntityEvent} to the specified power.
	 * <p>
	 * Use this method during the event to attribute it as damage caused by the specified power.
	 * @param power - {@link Power} that caused this damage
	 * @param event - {@link EntityDamageByEntityEvent} where this damage already took place
	 * @param cap - Maximum amount of damage that can occur in this instance
	 */
	public void causeDamage(Power power, EntityDamageByEntityEvent event, double cap) {
		plugin.getServer().getPluginManager().callEvent(new PowerDamageEvent(power, event, cap));
	}
	
	/**
	 * Causes damage to a target that is attributable to this power.
	 * <p>
	 * Use this method if damage doesn't naturally result from this power's mechanics (e.g. it doesn't damage the target via explosions, fire, arrows, etc).
	 * @param power - {@link Power} that caused this damage
	 * @param target - Entity to be harmed
	 * @param cause - {@link DamageCause} to attribute this damage to
	 * @param damage - Amount of damage to inflict
	 */
	public void causeDamage(Power power, Damageable target, DamageCause cause, double damage) {
		causeDamage(power, target, cause, damage, Double.MAX_VALUE);
	}
	
	/**
	 * Causes damage to a target that is attributable to this power.
	 * <p>
	 * Use this method if damage doesn't naturally result from this power's mechanics (e.g. it doesn't damage the target via explosions, fire, arrows, etc).
	 * @param power - {@link Power} that caused this damage
	 * @param target - Entity to be harmed
	 * @param cause - {@link DamageCause} to attribute this damage to
	 * @param damage - Amount of damage to inflict
	 * @param cap - Maximum amount of damage that can occur in this instance
	 */
	public void causeDamage(Power power, Damageable target, DamageCause cause, double damage, double cap) {
		plugin.getServer().getPluginManager().callEvent(new PowerDamageEvent(power, this, target, cause, damage, cap));
	}
	
	/**
	 * Ignites a target, then tracks the fire and attributes its damage to the specified power.
	 * <p>
	 * Use this method if you want to ignite a target on fire, then attribute the specified power as the cause should they burn to death.
	 * @param power - {@link Power} that caused this ignition
	 * @param target - Entity to be ignited
	 * @param duration - Time in game ticks for the fire to last
	 */
	public void causeIgnite(Power power, Entity target, int duration) {
		target.setFireTicks(duration);
		if (target instanceof LivingEntity) {
			plugin.getBlockListener().addIgnite((LivingEntity) target, new PowerFire(power, this));
		}
	}
	
	/**
	 * Removes any progress the user made with the specified {@link PowerStat}.
	 * @param stat - {@link PowerStat} to remove progress of
	 */
	public void clearStat(PowerStat stat) {
		if (stats.containsKey(stat)) {
			stats.remove(stat);
		}
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= saveTimer) {
			this.save();
		}
	}

	@Override
	public int compareTo(PowerUser user) {
		String o1Str = getName(),
				o2Str = user.getName();
		if (o1Str != null
				&& o2Str != null) {
			List<String> tmp = Arrays.asList(o1Str, o2Str);
			Collections.sort(tmp);
			return tmp.get(0).equalsIgnoreCase(getName()) ? -1 : 1;
		}
		else {
			return -1;
		}
	}
	
	void deneutralize(boolean force) {
		if (!this.isNeutralized()
				|| force) {
			if (isOnline()) {
				sendMessage(ChatColor.GREEN + LocaleString.POWERS_RETURN.toString());
			}
			if (nTask != -1
					&& plugin.getServer().getScheduler().isQueued(nTask)) {
				plugin.getServer().getScheduler().cancelTask(nTask);
				nTask = -1;
			}
			beacons.clear();
			regions.clear();
		}
	}
	
	void neutralize(String message) {
		if (!this.isNeutralized()) {
			this.getPlayer().getWorld().playEffect(this.getPlayer().getEyeLocation(), Effect.STEP_SOUND, Material.BLUE_STAINED_GLASS);
			this.sendMessage(ChatColor.BLUE + message);
			for (Power power : powers.keySet()) {
				power.disable(this);
			}
			if (S86Powers.getProtocolLib() != null) {
				PowerTools.removeDisguise(getPlayer());
				PowerTools.removeGhost(getPlayer());
			}
		}
	}
	
	public Set<Power> getAllUsablePowers() {
		return Sets.union(getPowers(true), getPermissiblePowers());
	}
	
	public Set<PowerGroup> getAssignedGroups() {
		return groups;
	}
	
	public Set<Power> getAssignedPowers() {
		return powers.keySet();
	}
	
	public Set<Power> getAssignedPowersByType(PowerType type) {
		Set<Power> tmp = new HashSet<Power>();
		for (Power power : powers.keySet()) {
			if (power.getType() == type) {
				tmp.add(power);
			}
		}
		return tmp;
	}
	
	Set<Beacon> getBeaconsInhabited() {
		return beacons;
	}
	
	/**
	 * Gets the remaining amount of time for a specified power's cooldown.
	 * @param power - The power of which to get the remaining cooldown for
	 * @return Amount of time, in milliseconds, remaining for this cooldown
	 */
	public long getCooldown(Power power) {
		if (cooldowns.containsKey(power)
				&& cooldowns.get(power) > System.currentTimeMillis()) {
			return cooldowns.get(power) - System.currentTimeMillis();
		}
		return 0L;
	}
	
	public ItemStack getEquipment(EquipmentSlot slot) {
		return PowerTools.getEquipment(getPlayer(), slot);
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
	
	/**
	 * Gets the name of the user's player. This will show the name the player used when most recently active on this server.
	 */
	public String getName() {
		if (getOfflinePlayer().getName() != null) {
			name = getOfflinePlayer().getName();
		}
		return name;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		if (oPlayer == null
				|| oPlayer.getUniqueId() != uuid) {
			oPlayer = plugin.getServer().getOfflinePlayer(uuid);
		}
		return oPlayer;
	}
	
	@SuppressWarnings("unchecked")
	public <O> O getOption(PowerOption<O> option) {
		assert option != null;
		Power power = option.getPower();
		return options.containsKey(option) ? (O) options.get(option) : (O) power.getOption(option);
	}
	
	public Map<PowerOption<?>, Object> getOptions() {
		return options;
	}
	
	public Object getOptionValue(PowerOption<?> option) {
		return options.containsKey(option) ? options.get(option) : null;
	}
	
	Set<PowerGroup> getPermissibleGroups() {
		Set<PowerGroup> tmp = new HashSet<PowerGroup>();
		if (isOnline()
				&& ConfigOption.Plugin.ENABLE_PERMISSION_ASSIGNMENTS) {
			for (PowerGroup group : S86Powers.getConfigManager().getGroups()) {
				if (getPlayer().hasPermission(group.getRequiredPermission())) {
					tmp.add(group);
				}
			}
		}
		return tmp;
	}
	
	public Set<Power> getPermissiblePowers() {
		Set<Power> tmp = new HashSet<Power>();
		if (isOnline()
				&& ConfigOption.Plugin.ENABLE_PERMISSION_ASSIGNMENTS) {
			for (Power power : S86Powers.getConfigManager().getPowers()) {
				if (getPlayer().hasPermission(power.getUsePermission())) {
					tmp.add(power);
				}
			}
		}
		return tmp;
	}
	
	public Player getPlayer() {
		return getOfflinePlayer().getPlayer();
	}
	
	public Set<Power> getPowers() {
		return getPowers(false);
	}
	
	public Set<Power> getPowers(boolean includeUtility) {
		Set<Power> tmp = new HashSet<Power>();
		if (includeUtility) {
			tmp.addAll(S86Powers.getConfigManager().getPowersByType(PowerType.UTILITY));
		}
		tmp.addAll(getAssignedPowers());
		tmp.addAll(getGroupPowers());
		tmp.addAll(getPermissiblePowers());
		return tmp;
	}
	
	Set<NeutralRegion> getRegionsInhabited() {
		return regions;
	}
	
	public int getStatCount(PowerStat stat) {
		return stats.containsKey(stat) ? stats.get(stat) : 0;
	}
	
	/**
	 * Gets the entity in the user's line of sight.
	 * This will always return the first entity hit (other than the player itself),
	 * or null if none are hit within the specified range.
	 * @param range - Range in blocks to check
	 * @return The target entity, or null if there is none
	 */
	public Entity getTargetEntity(double range) {
		return getTargetEntity(Entity.class, range);
	}
	
	/**
	 * Gets the entity in the user's line of sight, only if it is an instance of the specified class.
	 * This will always return the first entity hit (other than the player itself),
	 * or null if none are hit within the specified range.
	 * @param clazz - Class to check for
	 * @param range - Range in blocks to check
	 * @return The target entity, or null if there is none
	 */
	public <E extends Entity> E getTargetEntity(Class<E> clazz, double range) {
		if (isOnline()) {
			Predicate<Entity> pred = entity -> {
				return entity != getPlayer()
						&& clazz.isInstance(entity);
			};
			return PowerTools.getTargetEntity(clazz, getPlayer().getEyeLocation(), getPlayer().getEyeLocation().getDirection(), range, pred);
		}
		return null;
	}
	
	public Location getTargetLocation(double range) {
		if (isOnline()) {
			Vector dir = getPlayer().getLocation().getDirection();
			for (Location loc = getPlayer().getEyeLocation().clone(); getPlayer().getEyeLocation().distanceSquared(loc) <= range * range; loc.add(dir)) {
				if (loc.getBlock().getType().isOccluding()) {
					return loc;
				}
			}
		}
		return null;
	}
	
	public final UUID getUUID() {
		return uuid;
	}
	
	boolean hasEnablePermission() {
		if (isOnline()) {
			return isAdmin() ? ConfigOption.Admin.BYPASS_PERMISSION : getPlayer().hasPermission(S86Permission.ENABLE);
		}
		return false;
	}
	
	public boolean hasPower(Power power) {
		return getPowers(true).contains(power);
	}
	
	public boolean hasPower(String name) {
		for (Power power : getPowers(true)) {
			if (power.getClass().getSimpleName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPowerAssigned(Power power) {
		return powers.containsKey(power);
	}
	
	public boolean hasPowerEnabled(Power power) {
		return powers.containsKey(power) ? powers.get(power) : (isOnline() && getPlayer().hasPermission(power.getAssignPermission()));
	}
	
	public boolean hasPowersEnabled() {
		return enabled;
	}
	
	public boolean hasStatMaxed(PowerStat stat) {
		return stats.containsKey(stat)
				&& stats.get(stat) >= stat.getPower().getStatValue(stat);
	}
	
	public void heal(double amt) {
		if (isOnline()) {
			amt = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - getPlayer().getHealth() < amt ? getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - getPlayer().getHealth() : amt;
			getPlayer().setHealth(getPlayer().getHealth() + amt);
		}
	}
	
	public void increaseStat(PowerStat stat, int amount) {
		Power power = stat.getPower();
		if (!stats.containsKey(stat)) {
			stats.put(stat, 0);
		}
		if (stats.get(stat) < power.getStatValue(stat)
				|| amount < 0) {
			int newStat = stats.get(stat) + amount;
			stats.put(stat, newStat > power.getStatValue(stat) ? power.getStatValue(stat) : newStat);
			if (isOnline()) {
				sendMessage(power.getType().getColor() + power.getName() + ChatColor.RESET + " > " + ChatColor.YELLOW + stat.getDescription() + ChatColor.RESET + ": " + (stats.get(stat) < power.getStatValue(stat) + 1 ? stats.get(stat) : power.getStatValue(stat)) + "/" + power.getStatValue(stat));
				if (hasStatMaxed(stat)) {
					UserMaxedStatEvent event = new UserMaxedStatEvent(this, stat);
					plugin.getServer().getPluginManager().callEvent(event);
					sendMessage(power.getType().getColor() + power.getName() + ChatColor.RESET + " > " + ChatColor.YELLOW +
							PowerTools.getFilteredText(power, stat.getReward()));
				}
			}
			if (ConfigOption.Plugin.AUTO_SAVE
					&& System.currentTimeMillis() >= saveTimer) {
				this.save();
				saveTimer = System.currentTimeMillis() + ConfigOption.Plugin.AUTO_SAVE_COOLDOWN;
			}
		}
	}
	
	boolean inGroup(PowerGroup group) {
		return groups.contains(group);
	}
	
	public boolean isAdmin() {
		return isOnline()
				&& getPlayer().hasPermission(S86Permission.ADMIN);
	}
	
	public boolean isHoldingItem(ItemStack item) {
		return (isOnline() && getPlayer().getInventory().getItemInMainHand() != null && getPlayer().getInventory().getItemInMainHand().getType() == item.getType())
				|| (isOnline() && getPlayer().getInventory().getItemInOffHand() != null && getPlayer().getInventory().getItemInOffHand().getType() == item.getType());
	}
	
	public boolean isOnline() {
		return getOfflinePlayer().isOnline();
	}
	
	public boolean isNeutralized() {
		return !beacons.isEmpty()
				|| !regions.isEmpty()
				|| nTask > -1;
	}
	
	public void load() {
		if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
			plugin.getLogger().info(LocaleString.LOAD_ATTEMPT.build(cFile));
		}
		if (cFile != null) {
			config = YamlConfiguration.loadConfiguration(cFile);
			if (config.contains("powers")) {
				for (String pwr : config.getConfigurationSection("powers").getKeys(false)) {
					Power power = S86Powers.getConfigManager().getPower(pwr);
					if (power != null) {
						if (config.contains("powers." + pwr + ".active", false)) {
							addPowerWithoutSaving(power, config.getBoolean("powers." + pwr + ".active", false));
						}
						if (config.contains("powers." + pwr + ".options")) {
							for (String optName : config.getConfigurationSection("powers." + pwr + ".options").getKeys(false)) {
								PowerOption<?> option = power.getOptionByName(optName);
								if (option != null) {
									options.put(option, config.get("powers." + pwr + ".options." + optName, power.getOption(option)));
								}
							}
						}
						if (config.contains("powers." + pwr + ".stats")) {
							for (String statName : config.getConfigurationSection("powers." + pwr + ".stats").getKeys(false)) {
								PowerStat stat = power.getStat(statName);
								if (stat != null) {
									stats.put(stat, config.getInt("powers." + pwr + ".stats." + statName, 0));
								}
							}
						}
					}
				}
			}
			if (config.contains("groups")) {
				for (String grp : config.getStringList("groups")) {
					PowerGroup group = S86Powers.getConfigManager().getGroup(grp);
					if (group != null) {
						addGroupWithoutSaving(group);
					}
				}
			}
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.LOAD_SUCCESS.build(cFile));
			}
		}
		else {
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.LOAD_FAIL.build(cFile));
			}
			throw new NullPointerException();
		}
	}
	
	void purge() {
		beacons = new HashSet<>();
		cooldowns = new HashMap<>();
		groups = new HashSet<>();
		powers = new HashMap<>();
		regions = new HashSet<>();
		stats = new HashMap<>();
	}
	
	public void regenHunger(int amt) {
		if (isOnline()) {
			amt = 20 - getPlayer().getFoodLevel() < amt ? 20 - getPlayer().getFoodLevel() : amt;
			getPlayer().setFoodLevel(getPlayer().getFoodLevel() + amt);
		}
	}
	
	public void removeBeacon(Beacon beacon) {
		if (beacons.contains(beacon)) {
			beacons.remove(beacon);
			deneutralize(false);
		}
		PowerTools.removeSpectralBlock(getPlayer(), beacon.getBlock());
	}
	
	void removeGroup(PowerGroup group) {
		groups.remove(group);
		if (group.hasMember(this)) {
			group.removeMember(this);
		}
		autosave();
	}
	
	public void removeOption(PowerOption<?> option) {
		if (options.containsKey(option)) {
			options.remove(option);
		}
	}
	
	/**
	 * Shortcut method to remove a potion effect from the user.
	 * <p>
	 * Note: If the player is offline this will silently fail.
	 * @param effect - {@link PotionEffectType} to remove from this user
	 */
	public void removePotionEffect(PotionEffectType effect) {
		if (isOnline()) {
			getPlayer().removePotionEffect(effect);
		}
	}
	
	/**
	 * Shortcut method to remove several potion effects from the user.
	 * <p>
	 * Note: If the player is offline this will silently fail.
	 * @param effects - {@link PotionEffect}s to remove from this user
	 */
	public void removePotionEffects(Collection<PotionEffect> effects) {
		if (isOnline()
				&& effects != null
				&& !effects.isEmpty()) {
			for (PotionEffect effect : effects) {
				getPlayer().removePotionEffect(effect.getType());
			}
		}
	}
	
	public void removePower(Power power) {
		if (powers.containsKey(power)) {
			powers.remove(power);
		}
		power.removeUser(this);
		power.disable(this);
		autosave();
	}
	
	public void removeRegion(NeutralRegion region) {
		if (regions.contains(region)) {
			regions.remove(region);
			deneutralize(false);
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
			config.set("groups", null);
			if (!getAssignedGroups().isEmpty()) {
				List<String> gList = new ArrayList<String>();
				for (PowerGroup group : getAssignedGroups()) {
					gList.add(group.getName());
				}
				config.set("groups", gList);
			}
			config.set("powers", null);
			if (!getAssignedPowers().isEmpty()) {
				for (Power power : getAssignedPowers()) {
					config.set("powers." + power.getClass().getSimpleName() + ".active", hasPowerEnabled(power));
				}
			}
			if (!options.isEmpty()) {
				for (PowerOption<?> option : options.keySet()) {
					config.set("powers." + option.getPower().getClass().getSimpleName() + ".options." + option.getPath(), options.get(option));
				}
			}
			if (!stats.isEmpty()) {
				for (PowerStat stat : stats.keySet()) {
					config.set("powers." + stat.getPower().getClass().getSimpleName() + ".stats." + stat.getPath(), stats.get(stat));
				}
			}
			try {
				config.save(cFile);
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					plugin.getLogger().info(LocaleString.SAVE_SUCCESS.build(cFile));
				}
				saveTimer = System.currentTimeMillis() + ConfigOption.Plugin.AUTO_SAVE_COOLDOWN;
			} catch (IOException e) {
				if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
					plugin.getLogger().info(LocaleString.SAVE_FAIL.build(cFile));
				}
				e.printStackTrace();
			}
		}
		else {
			if (ConfigOption.Plugin.SHOW_CONFIG_STATUS) {
				plugin.getLogger().info(LocaleString.SAVE_FAIL.build(cFile));
			}
			throw new NullPointerException();
		}
	}
	
	public void sendMessage(String message) {
		if (ConfigOption.Users.SHOW_MESSAGES_IN_ACTION_BAR
				&& S86Powers.getProtocolLib() != null) {
			PowerTools.showActionBarMessage(getPlayer(), message);
		}
		else {
			getPlayer().sendMessage(message);
		}
	}
	
	public void setAllowFlight(boolean allow) {
		if (isOnline()
				&& (getPlayer().getGameMode() == GameMode.ADVENTURE
						|| getPlayer().getGameMode() == GameMode.SURVIVAL)) {
			getPlayer().setAllowFlight(allow);
		}
	}
	
	public void setCooldown(Power power, long time) {
		if (!this.isAdmin()
				|| !ConfigOption.Admin.BYPASS_COOLDOWN) {
			cooldowns.put(power, System.currentTimeMillis() + time);
			if (ConfigOption.Powers.SHOW_COOLDOWN_ON_ITEM
					&& power.getRequiredItem() != null
					&& S86Powers.getProtocolLib() != null) {
				PowerTools.showItemCooldown(getPlayer(), power.getRequiredItem(), time);
			}
		}
	}
	
	public void setNeutralizedByPower(final Power power, long duration) {
		if (System.currentTimeMillis() + duration > nTimer) {
			neutralize(LocaleString.NEUTRALIZED_BY_POWER.build(power, duration));
			if (plugin.getServer().getScheduler().isQueued(nTask)) {
				plugin.getServer().getScheduler().cancelTask(nTask);
			}
			BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					nTask = -1;
					deneutralize(false);
				}
			}, PowerTime.toTicks(duration));
			nTask = task.getTaskId();
			nTimer = System.currentTimeMillis() + duration;
		}
	}
	
	public void setOption(PowerOption<?> option, Object value) {
		options.put(option, value);
	}
	
	public void setPowerEnabled(Power power, boolean newState) {
		if (powers.containsKey(power)) {
			powers.put(power, newState);
			if (!newState) {
				power.disable(this);
			}
		}
		if (ConfigOption.Plugin.AUTO_SAVE
				&& System.currentTimeMillis() >= saveTimer) {
			save();
		}
	}
	
	public void setPowersEnabled(boolean newState) {
		enabled = newState;
	}
	
	public void showCooldown(Power power) {
		if (isOnline()) {
			sendMessage(ChatColor.RED + LocaleString.POWER_ON_COOLDOWN.build(getCooldown(power), power));
		}
	}
	
	@SuppressWarnings("deprecation")
	public void supply(Power power) {
		if (isOnline()) {
			for (int i = 0; i < power.getSupplies().size(); i ++) {
				ItemStack item = power.getSupplies().get(i);
				boolean filled = false;
				for (ItemStack stack : getPlayer().getInventory().getContents()) {
					if (stack != null
							&& item != null) {
						if (PowerTools.usesDurability(item)
								&& item.getType() == stack.getType()) {
							stack.setDurability((short) 0);
							filled = true;
							break;
						}
						else if (item.getType() == stack.getType()
								&& item.getDurability() == stack.getDurability()) {
							if (stack.getAmount() < item.getAmount()) {
								stack.setAmount(item.getAmount());
							}
							filled = true;
							break;
						}
					}
				}
				if (!filled) {
					int j = getPlayer().getInventory().firstEmpty();
					if (j > -1) {
						getPlayer().getInventory().setItem(j, item);
					}
					else {
						getPlayer().getWorld().dropItem(getPlayer().getLocation(), item);
					}
				}
			}
		}
	}
	
}
