package me.sirrus86.s86powers.tools.version;

import org.bukkit.Bukkit;

public enum MCServer {

	/**
	 * Represents Bukkit or CraftBukkit.
	 */
	BUKKIT("Bukkit"),
	
	/**
	 * Represents Spigot. Spigot can run anything Bukkit can, and has some of its own methods.
	 */
	SPIGOT("Spigot"),
	
	/**
	 * Represents Paper. Paper can run almost anything Spigot can, and has some of its own methods.
	 */
	PAPER("Paper"),
	
	/**
	 * Represents an unknown server software.
	 */
	UNKNOWN("Unknown");

	private final String name;
	
	public static final MCServer CURRENT_SERVER = getCurrentServer();
	
	private MCServer(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	private static final MCServer getCurrentServer() {
		for (MCServer server : MCServer.values()) {
			if (Bukkit.getVersion().contains(server.toString())) {
				return server;
			}
		}
		return MCServer.UNKNOWN;
	}
	
}
