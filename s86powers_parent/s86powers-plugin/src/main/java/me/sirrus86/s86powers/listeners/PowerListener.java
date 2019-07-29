package me.sirrus86.s86powers.listeners;

import java.util.Map;
import java.util.WeakHashMap;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.events.PowerDamageEvent;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.events.PowerUseOnEntityEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.UserContainer;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PowerListener implements Listener {

	private Map<Damageable, PowerDamageCause> trackList = new WeakHashMap<>();
	
	private final S86Powers plugin;
	
	public PowerListener(S86Powers plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private boolean hasCorrectItem(Power power, ItemStack item) {
		if (power.canUseSpecificItem()
				&& item != null
				&& item.getType() == PowerContainer.getContainer(power).getRequiredItem().getType()) {
			return true;
		}
		else if (power.canUseAnyAxe()
				&& PowerTools.isAxe(item)) {
			return true;
		}
		else if (power.canUseAnySword()
				&& PowerTools.isSword(item)) {
			return true;
		}
		return false;
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		if (trackList.containsKey(event.getEntity())) {
			PowerDamageCause cause = trackList.get(event.getEntity());
			if (event.getEntity().getLastDamageCause() != cause.getEvent()) {
				trackList.remove(event.getEntity());
			}
			else if (event instanceof PlayerDeathEvent) {
				PowerUser victim = plugin.getConfigManager().getUser(event.getEntity().getUniqueId());
				((PlayerDeathEvent) event).setDeathMessage(LocaleString.KILLED_BY_POWER.build(victim, (cause.getUser() != null && cause.getUser().getName() != null ? cause.getUser().getName() : "someone"), cause.getPower()));
			}
			else {
				// TODO create loot
			}
		}
	}
	
	@EventHandler
	private void onInteract(PlayerInteractEvent event) {
		PowerUser user = plugin.getConfigManager().getUser(event.getPlayer().getUniqueId());
		for (Power power : UserContainer.getContainer(user).getPowers(true)) {
			if (user.allowPower(power)
					&& PowerContainer.getContainer(power).getRequiredItem() != null
					&& hasCorrectItem(power, event.getItem())
					&& !(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType().isBlock())) {
				plugin.getServer().getPluginManager().callEvent(new PowerUseEvent(user, power, event.getItem(), event.getHand(), event.getClickedBlock(), event.getBlockFace()));
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() == event.getPlayer()) {
			event.setCancelled(true);
		}
		else {
			PowerUser user = plugin.getConfigManager().getUser(event.getPlayer().getUniqueId());
			for (Power power : UserContainer.getContainer(user).getPowers(true)) {
				if (user.allowPower(power)
						&& PowerContainer.getContainer(power).getRequiredItem() != null
						&& hasCorrectItem(power, event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand())) {
					plugin.getServer().getPluginManager().callEvent(new PowerUseOnEntityEvent(user, power, event.getRightClicked()));
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(PowerDamageEvent event) {
		PowerUser user = event.getUser();
		Damageable target = event.getTarget();
		PowerUser uTarget = target instanceof Player ? plugin.getConfigManager().getUser(((Player) target).getUniqueId()) : null;
		if (uTarget == null) {
			double damage = event.getDamage();
			if (damage > event.getCap()) {
				damage = event.getCap();
			}
			if (event.getEvent() != null) {
				event.getEvent().setDamage(damage);
				trackList.put(target, new PowerDamageCause(event.getPower(), user, event.getEvent()));
			}
			else {
				trackList.put(target, new PowerDamageCause(event.getPower(), user));
				target.damage(damage, user != null ? user.getPlayer() : null);
				trackList.remove(target);
			}
		}
		else {
			if (event.getEvent() != null) {
				event.getEvent().setCancelled(true);
			}
			event.setCancelled(true);
		}
	}
	
	private class PowerDamageCause {
		
		private final EntityDamageEvent event;
		private final Power power;
		private final PowerUser user;
		
		public PowerDamageCause(Power power, PowerUser user) {
			this(power, user, null);
		}
		
		public PowerDamageCause(Power power, PowerUser user, EntityDamageEvent event) {
			this.power = power;
			this.user = user;
			this.event = event;
		}
		
		public final EntityDamageEvent getEvent() {
			return event;
		}
		
		public final Power getPower() {
			return power;
		}
		
		public final PowerUser getUser() {
			return user;
		}
		
	}
	
}
