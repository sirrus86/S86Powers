package me.sirrus86.s86powers.listeners;

import java.util.Map;
import java.util.WeakHashMap;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.events.PowerDamageEvent;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.events.PowerUseOnEntityEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

import org.bukkit.Bukkit;
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

public final class PowerListener implements Listener {

	private Map<Damageable, PowerDamageCause> trackList = new WeakHashMap<>();
	
	public PowerListener(S86Powers plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@SuppressWarnings("unchecked")
	private boolean hasCorrectItem(PowerUser user, Power power, ItemStack item) {
		PowerOption<Boolean> wAxe = (PowerOption<Boolean>) power.getOptionByField("wAxe"),
				wItem = (PowerOption<Boolean>) power.getOptionByField("wItem"),
				wSword = (PowerOption<Boolean>) power.getOptionByField("wSword");
		PowerOption<ItemStack> itemOpt = (PowerOption<ItemStack>) power.getOptionByField("item");
		if ((wItem == null || user.getOption(wItem))
				&& item != null
				&& itemOpt != null
				&& user.getOption(itemOpt) != null
				&& item.getType() == user.getOption(itemOpt).getType()) {
			return true;
		}
		else if (wAxe != null
				&& user.getOption(wAxe)
				&& PowerTools.isAxe(item)) {
			return true;
		}
		else if (wSword != null
				&& user.getOption(wSword)
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
				PowerUser victim = S86Powers.getConfigManager().getUser(event.getEntity().getUniqueId());
				((PlayerDeathEvent) event).setDeathMessage(LocaleString.KILLED_BY_POWER.build(victim, (cause.getUser() != null && cause.getUser().getName() != null ? cause.getUser().getName() : "someone"), cause.getPower()));
			}
			else {
				// TODO create loot
			}
		}
	}
	
	@EventHandler
	private void onInteract(PlayerInteractEvent event) {
		PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
		for (Power power : user.getAllUsablePowers()) {
			if (user.allowPower(power)
					&& power.isEnabled()
					&& power.getRequiredItem() != null
					&& hasCorrectItem(user, power, event.getItem())
					&& !(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType().isBlock())) {
				Bukkit.getServer().getPluginManager().callEvent(new PowerUseEvent(user, power, event.getItem(), event.getHand(), event.getClickedBlock(), event.getBlockFace()));
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() == event.getPlayer()) {
			event.setCancelled(true);
		}
		else {
			PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
			for (Power power : user.getAllUsablePowers()) {
				if (user.allowPower(power)
						&& power.isEnabled()
						&& power.getRequiredItem() != null
						&& hasCorrectItem(user, power, event.getHand() == EquipmentSlot.HAND ? event.getPlayer().getInventory().getItemInMainHand() : event.getPlayer().getInventory().getItemInOffHand())) {
					Bukkit.getServer().getPluginManager().callEvent(new PowerUseOnEntityEvent(user, power, event.getRightClicked()));
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(PowerDamageEvent event) {
		PowerUser user = event.getUser();
		Damageable target = event.getTarget();
		PowerUser uTarget = target instanceof Player ? S86Powers.getConfigManager().getUser(((Player) target).getUniqueId()) : null;
		if (uTarget == null
				|| ConfigOption.Powers.DAMAGE_PLAYERS) {
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
