package me.sirrus86.s86powers.tools.version;

import org.bukkit.Bukkit;

public enum MCVersion {
	
	/**
	 * Represents server version 1.13.
	 */
	v1_13("v1_13_R1", 4.4D),
	
	/**
	 * Represents server version 1.13.1.
	 */
	v1_13_1("v1_13_R2", 4.4D),
	
	/**
	 * Represents server version 1.13.2.
	 */
	v1_13_2("v1_13_R2", 4.4D),
	
	/**
	 * Represents server version 1.14.
	 */
	v1_14("v1_14_R1", 4.5D),
	
	/**
	 * Represents server version 1.14.1.
	 */
	v1_14_1("v1_14_R1", 4.5D),
	
	/**
	 * Represents server version 1.14.2.
	 */
	v1_14_2("v1_14_R1", 4.5D),
	
	/**
	 * Represents server version 1.14.3.
	 */
	v1_14_3("v1_14_R1", 4.5D),
	
	/**
	 * Represents server version 1.14.4.
	 */
	v1_14_4("v1_14_R1", 4.5D),
	
	/**
	 * Represents server version 1.15.
	 */
	v1_15("v1_15_R1", 4.5D),
	
	/**
	 * Represents server version 1.15.1.
	 */
	v1_15_1("v1_15_R1", 4.5D),
	
	/**
	 * Represents server version 1.15.2.
	 */
	v1_15_2("v1_15_R1", 4.5D),
	
	/**
	 * Represents server version 1.16.1.
	 */
	v1_16_1("v1_16_R1", 4.6D),
	
	/**
	 * Represents server version 1.16.2.
	 */
	v1_16_2("v1_16_R2", 4.6D),
	
	/**
	 * Represents server version 1.16.3.
	 */
	v1_16_3("v1_16_R2", 4.6D),
	
	/**
	 * Represents server version 1.16.4.
	 */
	v1_16_4("v1_16_R3", 4.6D),
	
	/**
	 * Represents server version 1.16.5.
	 */
	v1_16_5("v1_16_R3", 4.6D),
	
	/**
	 * Represents server version 1.17.
	 */
	v1_17("v1_17", 4.7D),
	
	/**
	 * Represents server version 1.17.1.
	 */
	v1_17_1("v1_17_1", 4.7D),
	
	/**
	 * Represents an unsupported server version.
	 */
	UNSUPPORTED("unsupported", 0.0D);
	
	/**
	 * Represents the server version currently running.
	 */
	public static final MCVersion CURRENT_VERSION = getCurrentVersion();
	
	private final String path;
	private final double pLibVer;
	
	private MCVersion(final String path, final double pLibVer) {
		this.path = path;
		this.pLibVer = pLibVer;
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
	
	public double getRequiredProtocolLib() {
		return this.pLibVer;
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
