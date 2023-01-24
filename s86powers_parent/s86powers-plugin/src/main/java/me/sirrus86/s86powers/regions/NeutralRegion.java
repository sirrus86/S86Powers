package me.sirrus86.s86powers.regions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.users.PowerUser;
import org.jetbrains.annotations.NotNull;

public class NeutralRegion implements Comparable<NeutralRegion>, ConfigurationSerializable, Listener {

	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	private Vector vec1 = new Vector(0, 0, 0),
			vec2 = new Vector(0, 0, 0);
	private String name;
	private boolean active = false;
	private final World world;
	
	public NeutralRegion(String name, World world) {
		this.name = name;
		this.world = world;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@SuppressWarnings("unused")
	public NeutralRegion(Map<String, Object> args) {
		this.name = (String) args.get("name");
		this.active = (boolean) args.get("neutral");
		this.world = plugin.getServer().getWorld(UUID.fromString((String) args.get("world")));
		this.vec1 = (Vector) args.get("vector-1");
		this.vec2 = (Vector) args.get("vector-2");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public int compareTo(NeutralRegion region) {
		return getName().compareTo(region.getName());
	}
	
	public void deactivate() {
		HandlerList.unregisterAll(this);
		vec1 = new Vector(0, 0, 0);
		vec2 = new Vector(0, 0, 0);
		this.name = null;
	}
	
	public Vector getMaxCoords() {
		return Vector.getMaximum(vec1, vec2);
	}
	
	public Vector getMinCoords() {
		return Vector.getMinimum(vec1, vec2);
	}
	
	public String getName() {
		return this.name;
	}
	
	public final World getWorld() {
		return this.world;
	}
	
	public boolean isActive() {
		return active;
	}
	
	private boolean isInside(Location loc) {
		return loc.getX() > getMinCoords().getX()
				&& loc.getX() < getMaxCoords().getX()
				&& loc.getY() > getMinCoords().getY()
				&& loc.getY() < getMaxCoords().getY()
				&& loc.getZ() > getMinCoords().getZ()
				&& loc.getZ() < getMaxCoords().getZ();
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
		if (event.getPlayer().getWorld() == this.world
				&& isInside(event.getPlayer().getLocation().clone().add(0.5D, 0.0D, 0.5D))
				&& this.active) {
			user.addRegion(this);
		}
		else {
			user.removeRegion(this);
		}
	}
	
	public void resize(double x1, double y1, double z1, double x2, double y2, double z2) {
		vec1.setX(x1).setY(y1).setZ(z1);
		vec2.setX(x2).setY(y2).setZ(z2);
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", this.name);
		map.put("neutral", active);
		map.put("world", world.getUID().toString());
		map.put("vector-1", vec1.clone());
		map.put("vector-2", vec2.clone());
		return map;
	}
	
	public void setNeutral(boolean neutral) {
		this.active = neutral;
	}
	
}
