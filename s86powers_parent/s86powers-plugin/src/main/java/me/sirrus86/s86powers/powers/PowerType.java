package me.sirrus86.s86powers.powers;

import org.bukkit.ChatColor;

public enum PowerType {

	/**
	 * Power type which allows the user to defend themselves.
	 */
	DEFENSE(ChatColor.BLUE),
	/**
	 * Power type which allows the user to attack others.
	 */
	OFFENSE(ChatColor.RED),
	/**
	 * Power type which is active without user interaction or is considered neutral.
	 */
	PASSIVE(ChatColor.YELLOW),
	/**
	 * Power type that can be used by anyone at any time.
	 */
	UTILITY(ChatColor.GREEN);
	
	private final ChatColor color;
	
	private PowerType(ChatColor color) {
		this.color = color;
	}
	
	public final ChatColor getColor() {
		return this.color;
	}
	
	public final String getName() {
		return toString().substring(0, 1).toUpperCase() + toString().substring(1).toLowerCase();
	}
	
}
