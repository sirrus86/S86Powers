package me.sirrus86.s86powers.powers.internal.utility;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.Sets;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Neutralizer Beacon", type = PowerType.UTILITY, author = "sirrus86", concept = "sirrus86", icon=Material.LAPIS_BLOCK,
	description = "Neutralizer beacons can be created by placing a redstone torch on top of a lapis block and a lever on one of the sides. The beacon is active so long as it remains intact and the torch is lit. While active, all players who come within [radius] meters of the beacon are unable to use powers.")
public final class NeutralizerBeacon extends Power {

	private Set<Beacon> beacons;
	
	private final Set<Material> bMats = Sets.newHashSet(Material.LEVER, Material.REDSTONE_TORCH);
	
	private boolean destructable, immuneToOwn, showAura;
	private double radius;
	private String cantDestroy;
	
	@Override
	protected void onEnable() {
		beacons = new HashSet<Beacon>();
	}
	
	@Override
	protected void onDisable() {
		for (Beacon beacon : beacons) {
			beacon.save();
		}
	}

	@Override
	protected void config() {
		destructable = option("destructable-by-others", true, "Whether beacons can be destroyed by users other than the creator.");
		immuneToOwn = option("immune-to-own-beacons", false, "Whether users should be immune to their own beacons.");
		radius = option("radius", 50.0D, "Radius of the neutralizing field from the beacon.");
		showAura = option("show-aura", false, "Whether to show the aura of an active beacon.");
		cantDestroy = locale("message.cant-destroy-others", ChatColor.RED + "You can't destroy someone else's beacon.");
		loadBeacons();
	}
	
	private Beacon getBeacon(Block block) {
		for (Beacon beacon : beacons) {
			if (beacon.getLapis().equals(block)) {
				return beacon;
			}
		}
		return null;
	}
	
	private Block getLever(Block base) {
		for (BlockFace face : BlockFace.values()) {
			Block block = base.getRelative(face);
			if (block.getType() == Material.LEVER
					&& block.getBlockData() instanceof Switch) {
				BlockFace facing = ((Switch) block.getBlockData()).getFacing().getOppositeFace();
				if (block.getRelative(facing).equals(base)) {
					return block;
				}
			}
		}
		return null;
	}
	
	private boolean isBeacon(Block block) {
		return block.getType() == Material.LAPIS_BLOCK
					&& block.getRelative(BlockFace.UP).getType() == Material.REDSTONE_TORCH
					&& getLever(block) != null;
	}
	
	private void loadBeacons() {
		if (getConfig().contains("beacons")) {
			for (String bLoc : getConfig().getConfigurationSection("beacons").getKeys(false)) {
				String[] coords = bLoc.split(",");
				Block lapis = Bukkit.getWorld(coords[0]).getBlockAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				if (isBeacon(lapis)) {
					PowerUser owner = getUser(UUID.fromString(getConfig().getString("beacons." + bLoc + ".owner")));
					Beacon beacon = new Beacon(owner, lapis, getLever(lapis), lapis.getRelative(BlockFace.UP));
					beacon.setActive(getConfig().getBoolean("beacons." + bLoc + "active"));
				}
				else {
					getConfig().set("beacons." + lapis.getWorld().getName() + "," + lapis.getX() + "," + lapis.getY() + "," + lapis.getZ(), null);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onPlace(BlockPlaceEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (bMats.contains(event.getBlockPlaced().getType())
				&& isBeacon(event.getBlockAgainst())
				&& getBeacon(event.getBlockAgainst()) == null) {
			Block lapis = event.getBlockAgainst(),
					lever = getLever(lapis),
					torch = lapis.getRelative(BlockFace.UP);
			Beacon beacon = new Beacon(user, lapis, lever, torch);
			beacons.add(beacon);
			beacon.save();
		}
	}
	
	public class Beacon implements Listener {
		
		private boolean active = true;
		private final Set<Vector> auraCoords = PowerTools.getSphereCoords(radius);
		private int auraTask = -1;
		private final Block lapis, lever, torch;
		private final PowerUser owner;
		
		public Beacon(PowerUser owner, Block lapis, Block lever, Block torch) {
			this.owner = owner;
			this.lapis = lapis;
			this.lever = lever;
			this.torch = torch;
			registerEvents(this);
			setActive(this.active);
		}
		
		private Runnable aura = new BukkitRunnable() {
			
			@Override
			public void run() {
				if (active) {
					Location loc = lapis.getLocation().clone().add(0.5D, 0.5D, 0.5D);
					for (Vector vec : auraCoords) {
						loc.add(vec);
						loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(Color.BLUE, 0.75F));
						loc.subtract(vec);
					}
				}
			}
			
		};
		
		private void erase() {
			unregisterEvents(this);
			this.active = false;
			getConfig().set("beacons." + lapis.getWorld().getName() + "," + lapis.getX() + "," + lapis.getY() + "," + lapis.getZ(), null);
			saveConfig();
		}
		
		public Block getLapis() {
			return lapis;
		}
		
		public Block getLever() {
			return lever;
		}
		
		public PowerUser getOwner() {
			return owner;
		}
		
		public Block getTorch() {
			return torch;
		}
		
		public boolean isActive() {
			return active;
		}
		
		public void save() {
			ConfigurationSection confSec = getConfig().createSection("beacons." + lapis.getWorld().getName() + "," + lapis.getX() + "," + lapis.getY() + "," + lapis.getZ());
			confSec.set("owner", owner.getUUID().toString());
			confSec.set("active", active);
			saveConfig();
		}
		
		public void setActive(boolean active) {
			this.active = active;
			((Switch)this.lever.getBlockData()).setPowered(active);
			update();
		}
		
		@EventHandler(ignoreCancelled = true)
		private void onBreak(BlockBreakEvent event) {
			if (event.getBlock() == this.lapis
					|| event.getBlock() == this.lever
					|| event.getBlock() == this.torch) {
				PowerUser user = getUser(event.getPlayer());
				if (user == this.owner
						|| destructable) {
					setActive(false);
					erase();
				}
				else {
					user.sendMessage(cantDestroy);
					event.setCancelled(true);
				}
			}
		}
		
		@EventHandler(ignoreCancelled = true)
		private void onMove(PlayerMoveEvent event) {
			if (event.getTo().getWorld() == event.getFrom().getWorld()
					&& event.getTo().distanceSquared(event.getFrom()) > 0.0D) {
				PowerUser user = getUser(event.getPlayer());
				if (event.getTo().getWorld() != this.lapis.getWorld()) {
					user.removeBeacon(this);
				}
				else {
					if (lapis.getLocation().clone().add(0.5D, 0.5D, 0.5D).distanceSquared(event.getTo()) < radius * radius
							&& active
							&& (owner != user || !immuneToOwn)) {
						user.addBeacon(this);
					}
					else if (lapis.getLocation().clone().add(0.5D, 0.5D, 0.5D).distanceSquared(event.getTo()) >= radius * radius
									|| !active) {
						user.removeBeacon(this);
					}
				}
			}
		}
		
		@EventHandler(ignoreCancelled = true)
		private void onPhysics(BlockRedstoneEvent event) {
			if (event.getBlock().equals(this.lever)) {
				setActive(this.lever.isBlockIndirectlyPowered());
			}
		}
		
		private void update() {
			if (this.active
					&& showAura) {
				auraTask = runTaskTimer(aura, 0L, 10L).getTaskId();
			}
			else if (auraTask > -1) {
				cancelTask(auraTask);
			}
			for (Player player : PowerTools.getNearbyEntities(Player.class, lapis.getLocation(), radius)) {
				PowerUser user = getUser(player);
				if (this.active
						&& (this.owner != user || !immuneToOwn)) {
					user.addBeacon(this);
				}
				else {
					user.removeBeacon(this);
				}
			}
		}
		
	}

}
