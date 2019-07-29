package me.sirrus86.s86powers.events;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.users.PowerUser;

public class PowerDamageEvent extends PowerEvent implements Cancellable {

	private boolean cancelled = false;
	private double cap, damage;
	private final DamageCause cause;
	private final EntityDamageEvent event;
	private final Damageable target;
	private final PowerUser user;
	
	public PowerDamageEvent(final Power power, final PowerUser user, final EntityDamageEvent event, final double cap) {
		super(power);
		this.cause = event.getCause();
		this.event = event;
		this.user = user;
		this.target = event.getEntity() instanceof Damageable ? (Damageable) event.getEntity() : null;
		this.damage = event.getDamage();
		this.cap = cap;
	}
	
	public PowerDamageEvent(final Power power, final EntityDamageByEntityEvent event, final double cap) {
		super(power);
		this.cause = event.getCause();
		this.event = event;
		this.user = event.getDamager() instanceof Player ? plugin.getConfigManager().getUser(event.getDamager().getUniqueId()) : null;
		this.target = event.getEntity() instanceof Damageable ? (Damageable) event.getEntity() : null;
		this.damage = event.getDamage();
		this.cap = cap;
	}
	
	public PowerDamageEvent(final Power power, final PowerUser user, final Damageable target, final DamageCause cause, final double damage, final double cap) {
		super(power);
		this.cause = cause;
		this.event = null;
		this.user = user;
		this.target = target;
		this.damage = damage;
		this.cap = cap;
	}
	
	public double getCap() {
		return this.cap;
	}
	
	public DamageCause getCause() {
		return this.cause;
	}
	
	public double getDamage() {
		return this.damage;
	}
	
	public final EntityDamageEvent getEvent() {
		return this.event;
	}
	
	public final Damageable getTarget() {
		return this.target;
	}
	
	public final PowerUser getUser() {
		return this.user;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}
	
	public void setDamage(final double damage) {
		this.damage = damage;
	}
	
}
