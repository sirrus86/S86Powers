package me.sirrus86.s86powers.tools.version;

import org.bukkit.Bukkit;

public enum MCVersion {
	
	/**
	 * Represents server version 1.13.
	 */
	v1_13("v1_13_R1"),
	
	/**
	 * Represents server version 1.13.1.
	 */
	v1_13_1("v1_13_R2"),
	
	/**
	 * Represents server version 1.13.2.
	 */
	v1_13_2("v1_13_R2"),
	
	/**
	 * Represents server version 1.14.
	 */
	v1_14("v1_14_R1"),
	
	/**
	 * Represents server version 1.14.1.
	 */
	v1_14_1("v1_14_R1"),
	
	/**
	 * Represents server version 1.14.2.
	 */
	v1_14_2("v1_14_R1"),
	
	/**
	 * Represents server version 1.14.3.
	 */
	v1_14_3("v1_14_R1"),
	
	/**
	 * Represents server version 1.14.4.
	 */
	v1_14_4("v1_14_R1"),
	
	/**
	 * Represents an unsupported server version.
	 */
	UNSUPPORTED("unsupported");
	
	/**
	 * Represents the server version currently running.
	 */
	public static final MCVersion CURRENT_VERSION = getCurrentVersion();
	
	private final String path;
	
	private MCVersion(final String path) {
		this.path = path;
	}
	
	private static MCVersion getCurrentVersion() {
		String version = "v" + Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(MC:") + 5, Bukkit.getVersion().indexOf(")")).replaceAll("\\.", "_");
		try {
			return MCVersion.valueOf(version);
		} catch (IllegalArgumentException e) {
			return MCVersion.UNSUPPORTED;
		}
	}
	
	public String getPath() {
		return this.path;
	}
	
	public static final boolean isLessThan(MCVersion version) {
		return MCVersion.CURRENT_VERSION.ordinal() < version.ordinal();
	}
	
	public static final boolean isVersion(MCVersion... versions) {
		for (int i = 0; i < versions.length; i ++) {
			if (MCVersion.CURRENT_VERSION.equals(versions[i])) {
				return true;
			}
		}
		return false;
	}
	
}
