package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Web Wall", type = PowerType.DEFENSE, author = "sirrus86", concept = "vashvhexx", icon=Material.COBWEB,
	description = "[act:item]ing a block while holding [item] will cause a wall made of spider webs to sprout from the surface.")
public class WebWall extends Power {

	private final EnumSet<BlockFace> directions = EnumSet.of(BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.WEST);
	private Map<PowerUser, List<Wall>> walls;
	
	private boolean doConsume;
	private double maxGrowth;
	private int maxWalls;
	private long wallDur;
	private PowerStat wallsMade;
	
	@Override
	protected void onEnable() {
		walls = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (walls.containsKey(user)) {
			for (Wall wall : Sets.newHashSet(walls.get(user))) {
				wall.terminate();
			}
		}
	}
	
	@Override
	protected void options() {
		cooldown = option("cooldown", PowerTime.toMillis(30, 0), "Amount of time before power can be used again.");
		doConsume = option("consume-item", true, "Whether item should be consumed on use.");
		item = option("item", new ItemStack(Material.COBWEB), "Item used to create web walls.");
		maxGrowth = option("maximum-growth", 5.0D, "Maximum distance webs can grow from their starting point.");
		maxWalls = option("maximum-walls", 3, "Maximum number of walls user can create after maxing stat.");
		wallDur = option("wall-duration", PowerTime.toMillis(15, 0), "How long web walls last before disappearing.");
		wallsMade = stat("walls-created", 30, "Web walls created", "Can now create [maxWalls] web walls at a time.");
		supplies(new ItemStack(item.getType(), item.getMaxStackSize()));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this
				&& event.getClickedBlock() != null
				&& event.getBlockFace() != null
				&& event.getClickedBlock().getRelative(event.getBlockFace()).isEmpty()) {
			PowerUser user = event.getUser();
			if (!walls.containsKey(user)) {
				walls.put(user, new ArrayList<>());
			}
			if (user.getCooldown(this) <= 0L
					|| (walls.get(user).size() < maxWalls
							&& user.hasStatMaxed(wallsMade))) {
				Wall wall = new Wall(user, event.getClickedBlock().getRelative(event.getBlockFace()), PowerTools.getDirection(user.getPlayer().getEyeLocation(), false));
				walls.get(user).add(wall);
				user.increaseStat(wallsMade, 1);
				if (doConsume) {
					event.consumeItem();
				}
				if (user.getCooldown(this) <= 0L) {
					user.setCooldown(this, cooldown);
				}
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	private class Wall implements Listener {
		
		private Set<Block> blocks = new HashSet<>();
		private final Block core;
		List<BlockFace> faces = Lists.newArrayList(directions);
		private int growTask, lifeTask;
		private final PowerUser owner;
		
		public Wall(PowerUser owner, Block core, BlockFace facing) {
			registerEvents(this);
			this.core = core;
			faces.remove(facing);
			faces.remove(facing.getOppositeFace());
			this.owner = owner;
			core.setType(Material.COBWEB);
			blocks.add(core);
			growTask = getInstance().runTaskLater(growth, 1L).getTaskId();
			lifeTask = getInstance().runTaskLater(new BukkitRunnable() {

				@Override
				public void run() {
					terminate();
				}
				
			}, PowerTime.toTicks(wallDur)).getTaskId();
		}
		
		private Runnable growth = new BukkitRunnable() {

			int tries = 0;
			
			@Override
			public void run() {
				boolean added = false;
				for (Block block : Sets.newHashSet(blocks)) {
					Collections.shuffle(faces);
					BlockFace face = faces.get(0);
					if (block.getRelative(face).isEmpty()
							&& core.getLocation().distanceSquared(block.getRelative(face).getLocation()) <= maxGrowth * maxGrowth) {
						Block newWeb = block.getRelative(face);
						newWeb.getWorld().playEffect(newWeb.getLocation(), Effect.STEP_SOUND, Material.COBWEB);
						newWeb.setType(Material.COBWEB);
						blocks.add(newWeb);
						added = true;
					}
				}
				if (!added) {
					tries ++;
				}
				if (tries < 3) {
					growTask = getInstance().runTaskLater(growth, 1L).getTaskId();
				}
			}
			
		};
		
		@EventHandler(ignoreCancelled = true)
		private void onBreak(BlockBreakEvent event) {
			if (blocks.contains(event.getBlock())) {
				event.setDropItems(false);
			}
		}
		
		public void terminate() {
			cancelTask(growTask);
			cancelTask(lifeTask);
			for (Block block : blocks) {
				if (block.getType() == Material.COBWEB) {
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.COBWEB);
					block.setType(Material.AIR);
				}
			}
			blocks.clear();
			walls.get(owner).remove(this);
		}
		
	}

}
