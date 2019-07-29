package me.sirrus86.s86powers.users;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.powers.PowerFire;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.powers.internal.utility.NeutralizerBeacon.Beacon;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.utils.PowerTime;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
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

/**
 * Class for any potential user of any power.
 * <p>
 * S86 Powers ultimately considers every player a PowerUser, regardless of whether they have or use powers.
 */
public class PowerUser implements Comparable<PowerUser> {

	Set<Beacon> beacons = new HashSet<>();
	Map<Power, Long> cooldowns = new HashMap<>();
	Set<PowerGroup> groups = new HashSet<>();
	Map<Power, Boolean> powers = new HashMap<>();
	Set<NeutralRegion> regions = new HashSet<>();
	Map<PowerStat, Integer> stats = new HashMap<>();
	
	final File cFile;
	YamlConfiguration config;
	boolean enabled = true;
	private String name;
	int nTask = -1;
	private long nTimer = 0L;
	long saveTimer = 0L;
	private OfflinePlayer oPlayer;
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
		name = getOfflinePlayer().getName();
		cFile = new File(plugin.getUserDirectory(), uuid.toString() + ".yml");
		if (!cFile.exists()) {
			try {
				cFile.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().severe(LocaleString.FILE_CREATE_FAIL.build(cFile));
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adds a potion effect discretely to the user.
	 * <p>
	 * Note: If the player is offline this will silently fail.
	 * @param effect - {@link PotionEffect} to apply to this user
	 */
	public void addPotionEffect(PotionEffect effect) {
		if (isOnline()) {
			getPlayer().addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), effect.getAmplifier(), false, false, false), true);
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
	 * Determines whether the specified power can be used by this user at this time.
	 * <p>
	 * This will automatically check whether the player is online, whether they have the power assigned in any capacity (directly assigned, permission, etc), and that they're not neutralized.
	 * <p>
	 * Powers with {@link PowerType#UTILITY} will always return true.
	 * @param power - {@link Power} to check
	 * @return <b>true</b> if player is online, power is assigned, power is enabled, and user is not neutralized
	 */
	public boolean allowPower(Power power) {
		return power.getType() == PowerType.UTILITY ? true : UserContainer.getContainer(this).hasPower(power)
				&& UserContainer.getContainer(this).hasPowerEnabled(power)
				&& UserContainer.getContainer(this).hasPowersEnabled()
				&& !UserContainer.getContainer(this).isNeutralized();
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
			UserContainer.getContainer(this).save();
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
		if (!UserContainer.getContainer(this).isNeutralized()
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
		if (!UserContainer.getContainer(this).isNeutralized()) {
			this.getPlayer().getWorld().playEffect(this.getPlayer().getEyeLocation(), Effect.STEP_SOUND, Material.BLUE_STAINED_GLASS);
			this.sendMessage(ChatColor.BLUE + message);
			for (Power power : powers.keySet()) {
				PowerContainer.getContainer(power).disable(this);
			}
		}
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
	
	public Player getPlayer() {
		return getOfflinePlayer().getPlayer();
	}
	
	public int getStatCount(PowerStat stat) {
		return stats.containsKey(stat) ? stats.get(stat) : 0;
	}
	
//	private String getStatReward(PowerStat stat) {
//		return plugin.getPowerTools().getPowerDesc(stat.getPower(), stat.getReward());
//	}
	
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
		if (stats.get(stat) < power.getStatValue(stat)) {
			int newStat = stats.get(stat) + amount;
			stats.put(stat, newStat > power.getStatValue(stat) ? power.getStatValue(stat) : newStat);
			if (isOnline()) {
				sendMessage(power.getType().getColor() + power.getName() + ChatColor.RESET + " > " + ChatColor.YELLOW + stat.getDescription() + ChatColor.RESET + ": " + (stats.get(stat) < power.getStatValue(stat) + 1 ? stats.get(stat) : power.getStatValue(stat)) + "/" + power.getStatValue(stat));
				if (hasStatMaxed(stat)) {
					UserMaxedStatEvent event = new UserMaxedStatEvent(this, stat);
					plugin.getServer().getPluginManager().callEvent(event);
					sendMessage(power.getType().getColor() + power.getName() + ChatColor.RESET + " > " + ChatColor.YELLOW +
							PowerContainer.getContainer(power).getFilteredText(stat.getReward()));
				}
			}
			if (ConfigOption.Plugin.AUTO_SAVE
					&& System.currentTimeMillis() >= saveTimer) {
				UserContainer.getContainer(this).save();
				saveTimer = System.currentTimeMillis() + ConfigOption.Plugin.AUTO_SAVE_COOLDOWN;
			}
		}
	}
	
	public boolean isHoldingItem(ItemStack item) {
		return (getPlayer().getInventory().getItemInMainHand() != null && getPlayer().getInventory().getItemInMainHand().getType() == item.getType())
				|| (getPlayer().getInventory().getItemInOffHand() != null && getPlayer().getInventory().getItemInOffHand().getType() == item.getType());
	}
	
	public boolean isOnline() {
		return getOfflinePlayer().isOnline();
	}
	
	public void regenHunger(int amt) {
		if (isOnline()) {
			amt = 20 - getPlayer().getFoodLevel() < amt ? 20 - getPlayer().getFoodLevel() : amt;
			getPlayer().setFoodLevel(getPlayer().getFoodLevel() + amt);
		}
	}
	
	public void sendMessage(String message) {
		if (ConfigOption.Users.SHOW_MESSAGES_IN_ACTION_BAR) {
			PowerTools.showActionBarMessage(getPlayer(), message);
		}
		else {
			getPlayer().sendMessage(message);
		}
	}
	
	public void setCooldown(Power power, long time) {
		cooldowns.put(power, System.currentTimeMillis() + time);
		if (ConfigOption.Powers.SHOW_COOLDOWN_ON_ITEM
				&& PowerContainer.getContainer(power).getRequiredItem() != null) {
			PowerTools.showItemCooldown(getPlayer(), PowerContainer.getContainer(power).getRequiredItem(), time);
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
	
	public void showCooldown(Power power) {
		if (isOnline()) {
			sendMessage(ChatColor.RED + LocaleString.POWER_ON_COOLDOWN.build(getCooldown(power), power));
		}
	}
	
}
