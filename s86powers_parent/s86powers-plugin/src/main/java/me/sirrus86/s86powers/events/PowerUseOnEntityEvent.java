package me.sirrus86.s86powers.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.users.PowerUser;

public class PowerUseOnEntityEvent extends UserEvent implements Cancellable {

	private boolean cancelled = false;
	private final Entity entity;
	private final Power power;
	
	public PowerUseOnEntityEvent(final PowerUser user, final Power power, final Entity entity) {
		super(user);
		this.power = power;
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public Power getPower() {
		return power;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean arg0) {
		cancelled = arg0;
	}

}
