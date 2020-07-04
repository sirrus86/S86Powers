package me.sirrus86.s86powers.tools.version.v1_16;

import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class VersionTools extends me.sirrus86.s86powers.tools.version.VersionTools {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> T getTargetEntity(Class<T> clazz, Location location, Vector direction, double maxDistance, Predicate<Entity> filter) {
		RayTraceResult rayTrace = location.getWorld().rayTrace(location, direction, maxDistance, FluidCollisionMode.NEVER, true, 1.0D, filter);
		if (rayTrace != null
				&& rayTrace.getHitEntity() != null
				&& rayTrace.getHitBlock() == null) {
			Entity target = rayTrace.getHitEntity();
			return (T) target;
		}
		return null;
	}
	
	@Override
	public EntityType resolveEntityType(String name) {
		switch(name) {
			case "PIGLIN": case "PIGZOMBIE": return EntityType.PIGLIN;
		}
		return null;
	}

}
