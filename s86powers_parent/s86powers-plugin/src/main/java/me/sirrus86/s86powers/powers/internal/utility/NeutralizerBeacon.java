package me.sirrus86.s86powers.powers.internal.utility;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Neutralizer Beacon", type = PowerType.UTILITY, author = "sirrus86", concept = "sirrus86", icon = Material.LAPIS_BLOCK,
	description = "Neutralizer beacons can be created by applying a redstone current to a Lapis Lazuli block. While active, all players who come within up to [radius] meters of the beacon are unable to use powers.")
public class NeutralizerBeacon extends Power {

	private final Set<BlockFace> adjacent = EnumSet.of(BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.SELF, BlockFace.SOUTH, BlockFace.UP, BlockFace.WEST);
	
	private Map<Block, Beacon> beacons;
	
	private PowerOption<Boolean> canExclude, showAura;
	private PowerOption<Double> radius;
	
	@Override
	protected void onEnable() {
		beacons = new HashMap<Block, Beacon>();
	}
	
	@Override
	protected void onDisable() {
		getConfig().createSection("beacons");
		for (Block block : beacons.keySet()) {
			getConfig().getConfigurationSection("beacons")
				.set(block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ(), true);
		}
		saveConfig();
	}
	
	@Override
	protected void config() {
		canExclude = option("can-exclude", true, "Allows player names on adjacent signs to be excluded from beacon effects.");
		radius = option("radius", 50.0D, "Maximum radius of the neutralizing field from the beacon.");
		showAura = option("show-aura", false, "Whether to show the aura of an active beacon.");
		loadBeacons();
	}
	
	private void loadBeacons() {
		if (getConfig().contains("beacons")) {
			for (String bLoc : getConfig().getConfigurationSection("beacons").getKeys(false)) {
				String[] coords = bLoc.split(",");
				Block lapis = Bukkit.getWorld(coords[0]).getBlockAt(Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
				Beacon beacon = new Beacon(lapis);
				beacons.put(lapis, beacon);
				beacon.update();
			}
		}
	}
	
	private BukkitRunnable checkForBeacon(Block lapis) {
		return new BukkitRunnable() {

			@Override
			public void run() {
				if (lapis.getBlockPower() > 0) {
					if (!beacons.containsKey(lapis)) {
						beacons.put(lapis, new Beacon(lapis));
					}
					Beacon beacon = beacons.get(lapis);
					beacon.update();
				}
				else if (beacons.containsKey(lapis)) {
					Beacon beacon = beacons.get(lapis);
					beacons.remove(lapis);
					beacon.terminate();
				}
			}
			
		};
		
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		for (BlockFace face : BlockFace.values()) {
			Block nearby = block.getRelative(face);
			if (nearby.getType() == Material.LAPIS_BLOCK) {
				runTask(checkForBeacon(nearby));
			}
		}
		
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		for (BlockFace face : adjacent) {
			Block nearby = block.getRelative(face);
			if (nearby.getType() == Material.LAPIS_BLOCK) {
				runTask(checkForBeacon(nearby));
			}
		}
		
	}
	
	@EventHandler
	private void onPower(BlockRedstoneEvent event) {
		Block block = event.getBlock();
		for (BlockFace face : adjacent) {
			Block nearby = block.getRelative(face);
			if (nearby.getType() == Material.LAPIS_BLOCK) {
				runTask(checkForBeacon(nearby));
			}
		}
		
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		for (BlockFace face : adjacent) {
			Block nearby = block.getRelative(face);
			if (nearby.getType() == Material.LAPIS_BLOCK) {
				runTask(checkForBeacon(nearby));
			}
		}
		
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onInteract(PlayerInteractEvent event) {
		if (event.hasBlock()) {
			Block block = event.getClickedBlock();
			for (BlockFace face : adjacent) {
				Block nearby = block.getRelative(face);
				if (nearby.getType() == Material.LAPIS_BLOCK) {
					runTask(checkForBeacon(nearby));
				}
			}
		}
		
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onSignChange(SignChangeEvent event) {
		Block block = event.getBlock();
		for (BlockFace face : adjacent) {
			Block nearby = block.getRelative(face);
			if (nearby.getType() == Material.LAPIS_BLOCK) {
				runTask(checkForBeacon(nearby));
			}
		}
		
	}
	
	public class Beacon implements Listener {
		
		private Set<Vector> auraCoords;
		private int auraTask = -1;
		private List<String> excluded = new ArrayList<>();
		private final Block lapis;
		private double range;
		
		public Beacon(Block lapis) {
			this.lapis = lapis;
			registerEvents(this);
		}
		
		private Runnable aura = new BukkitRunnable() {
			
			@Override
			public void run() {
				Location loc = lapis.getLocation().clone().add(0.5D, 0.5D, 0.5D);
				for (Vector vec : auraCoords) {
					loc.add(vec);
					loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(Color.BLUE, 0.75F));
					loc.subtract(vec);
				}
			}
			
		};
		
		public final Block getBlock() {
			return this.lapis;
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
					if (lapis.getLocation().clone().add(0.5D, 0.5D, 0.5D).distanceSquared(event.getTo()) < range * range
							&& !excluded.contains(user.getName())) {
						user.addBeacon(this);
					}
					else if (lapis.getLocation().clone().add(0.5D, 0.5D, 0.5D).distanceSquared(event.getTo()) >= range * range
							|| excluded.contains(user.getName())) {
						user.removeBeacon(this);
					}
				}
			}
		}
		
		public void terminate() {
			if (auraTask > -1) {
				cancelTask(auraTask);
			}
			for (Player player : PowerTools.getNearbyEntities(Player.class, lapis.getLocation(), range)) {
				PowerUser user = getUser(player);
				user.removeBeacon(this);
			}
			unregisterEvents(this);
			getConfig().set("beacons." + lapis.getWorld().getName() + "," + lapis.getX() + "," + lapis.getY() + "," + lapis.getZ(), null);
			saveConfig();
		}
		
		public void update() {
			double oldRange = range;
			if (lapis.getType() == Material.LAPIS_BLOCK
					&& lapis.getBlockPower() > 0) {
				double strength = lapis.getBlockPower() / 15.0D;
				range = getInstance().getOption(radius) * strength;
				auraCoords = PowerTools.getSphereCoords(range);
			}
			else {
				terminate();
				return;
			}
			if (getInstance().getOption(showAura)) {
				if (oldRange != range
						&& auraTask > -1) {
					cancelTask(auraTask);
					auraTask = -1;
				}
				if (auraTask <= -1) {
					auraTask = runTaskTimer(aura, 0L, 10L).getTaskId();
				}
			}
			else if (auraTask > -1) {
				cancelTask(auraTask);
			}
			excluded.clear();
			if (getInstance().getOption(canExclude)) {
				for (BlockFace face : adjacent) {
					if (lapis.getRelative(face).getState() instanceof Sign) {
						Sign sign = (Sign) lapis.getRelative(face).getState();
						for (String line : sign.getLines()) {
							excluded.add(line);
						}
					}
				}
			}
			for (Player player : PowerTools.getNearbyEntities(Player.class, lapis.getLocation(), range)) {
				PowerUser user = getUser(player);
				if (excluded.contains(user.getName())) {
					user.removeBeacon(this);
				}
				else {
					user.addBeacon(this);
				}
			}
		}
		
	}

}
