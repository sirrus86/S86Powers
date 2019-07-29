package me.sirrus86.s86powers.tools.version;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public abstract class VersionTools {
	
	public abstract <T extends Entity> T getTargetEntity(Class<T> clazz, Location location, Vector direction, double maxDistance, Predicate<Entity> filter);

}
