package me.sirrus86.s86powers.events;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.users.PowerUser;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UserEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	protected final S86Powers plugin = (S86Powers) Bukkit.getServer().getPluginManager().getPlugin("S86 Powers");
	private final PowerUser user;
	
	public UserEvent(final PowerUser user) {
		this.user = user;
	}
	
	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	@SuppressWarnings("unused")
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public final PowerUser getUser() {
		return user;
	}

}
