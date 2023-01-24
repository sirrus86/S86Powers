package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Rewind", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", icon = Material.GOLDEN_BOOTS,
	description = "[act:item]ing while holding [item] allows you to go back in time up to [rewind-time], bringing you to your previous position. [cooldown] cooldown.")
public final class Rewind extends Power {

	private Map<PowerUser, Long> locCD;
	private Map<PowerUser, TreeMap<Long, Location>> locs;
	private Map<PowerUser, Integer> rewindTask;
	
	private PowerOption<Long> rewindTime, storeCD;
	
	@Override
	protected void onEnable() {
		locCD = new HashMap<>();
		locs = new HashMap<>();
		rewindTask = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (rewindTask.containsKey(user)) {
			cancelTask(rewindTask.get(user));
		}
	}
	
	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(1, 0, 0), "Amount of time before power can be used again.");
		item = option("item", new ItemStack(Material.CLOCK), "Item used to rewind time.");
		rewindTime = option("rewind-time", PowerTime.toMillis(5, 0), "Maximum amount of time that can be rewound.");
		storeCD = option("time-storage-cooldown", PowerTime.toMillis(250), "Minimum time required before storing another location.");
		supplies(getRequiredItem());
	}
	
	private void doRewind(PowerUser user, TreeMap<Long, Location> locations) {
		long lastKey = locations.lastKey();
		Location loc = locations.get(lastKey);
		if (loc.getWorld() == user.getPlayer().getWorld()) {
			double fall = Math.max(0.0D, loc.getY() - user.getPlayer().getLocation().getY());
			user.getPlayer().setFallDistance(Math.max(0.0F, user.getPlayer().getFallDistance() - (float) fall));
		}
		user.getPlayer().teleport(loc);
		locations.remove(lastKey);
		if (!locations.isEmpty()) {
			rewindTask.put(user, runTaskLater(new BukkitRunnable() {
	
				@Override
				public void run() {
					doRewind(user, locations);
				}
				
			}, 1L).getTaskId());
		}
	}
	
	private void trimMap(Map<Long, Location> map, long amt) {
		if (!map.isEmpty()) {
			for (Long time : Set.copyOf(map.keySet())) {
				if (time < System.currentTimeMillis() - amt) {
					map.remove(time);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		if (event.getTo() != null) {
			if (event.getTo().getWorld() != event.getFrom().getWorld()
					|| event.getTo().distanceSquared(event.getFrom()) > 0.0D) {
				PowerUser user = getUser(event.getPlayer());
				if (user.allowPower(this)
						&& user.getCooldown(this) <= user.getOption(rewindTime)
						&& (!locCD.containsKey(user)
						|| locCD.get(user) <= System.currentTimeMillis())) {
					locs.putIfAbsent(user, new TreeMap<>());
					trimMap(locs.get(user), user.getOption(rewindTime));
					locs.get(user).put(System.currentTimeMillis(), event.getFrom());
					locCD.put(user, System.currentTimeMillis() + user.getOption(storeCD));
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0L) {
				locs.putIfAbsent(user, new TreeMap<>());
				trimMap(locs.get(user), user.getOption(rewindTime));
				if (!locs.get(user).isEmpty()) {
					doRewind(user, locs.get(user));
					user.setCooldown(this, user.getOption(cooldown));
				}
			}
			else {
				user.showCooldown(this);
			}
		}
	}

}
