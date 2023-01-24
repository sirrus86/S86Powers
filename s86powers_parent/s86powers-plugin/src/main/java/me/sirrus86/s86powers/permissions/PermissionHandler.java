package me.sirrus86.s86powers.permissions;

import java.lang.reflect.Field;

import org.bukkit.permissions.Permission;

import me.sirrus86.s86powers.S86Powers;

public class PermissionHandler {

	public PermissionHandler(S86Powers plugin) {
		for (Field field : S86Permission.class.getFields()) {
			if (field.getType() == Permission.class) {
				try {
					Permission perm = (Permission) field.get(null);
					if (!plugin.getServer().getPluginManager().getPermissions().contains(perm)) {
						plugin.getServer().getPluginManager().addPermission(perm);
					}
				} catch (Exception ignored) {}
			}
		}
	}
	
}
