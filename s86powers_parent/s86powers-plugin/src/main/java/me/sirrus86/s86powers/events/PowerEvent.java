package me.sirrus86.s86powers.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.sirrus86.s86powers.powers.Power;

public class PowerEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Power power;
	
	public PowerEvent(final Power power) {
		this.power = power;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public final Power getPower() {
		return power;
	}
	
}
