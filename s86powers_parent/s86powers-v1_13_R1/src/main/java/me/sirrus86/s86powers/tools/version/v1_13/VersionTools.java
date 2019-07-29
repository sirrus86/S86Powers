package me.sirrus86.s86powers.tools.version.v1_13;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class VersionTools extends me.sirrus86.s86powers.tools.version.VersionTools {

	@Override
	public <T extends Entity> T getTargetEntity(Class<T> clazz, Location location, Vector direction, double maxDistance, Predicate<Entity> filter) {
		for (Location checkLoc = location.clone(); checkLoc.distanceSquared(location) < maxDistance * maxDistance; checkLoc.add(direction)) {
			Block block = checkLoc.getBlock();
			if (block.getType().isSolid()) {
				return null;
			}
			for (T entity : location.getWorld().getEntitiesByClass(clazz)) {
				if (checkLoc.distanceSquared(entity.getLocation()) < 1.0D
						&& filter.test(entity)) {
					return entity;
				}
			}
		}
		return null;
	}
	
}
