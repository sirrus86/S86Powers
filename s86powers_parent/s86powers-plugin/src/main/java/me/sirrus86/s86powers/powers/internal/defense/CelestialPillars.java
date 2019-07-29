package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Celestial Pillars", type = PowerType.DEFENSE, author = "sirrus86", concept = "TotalPotato", icon=Material.SEA_LANTERN,
	description = "[act:item]ing the top of a block while holding [item][consume] consumes it, then[/consume] creates a circle of pillars around you. Entities within the circle are trapped inside, while entities outside the pillar cannot enter. You may pass beyond your own pillars. Pillars last for [pDur]. [cooldown] cooldown.")
public class CelestialPillars extends Power {

	private Map<PowerUser, Pillar> pillars, sPillars;
	private Map<FallingBlock, PowerUser> falling;
	private Set<FallingBlock> superFalling;
	
	private boolean consume;
	private long pDur;
	private int pHeight, pRange, sPRange;
	private PowerStat pillarsSummoned;
	
	@Override
	protected void onEnable() {
		falling = new HashMap<>();
		pillars = new HashMap<>();
		sPillars = new HashMap<>();
		superFalling = new HashSet<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (pillars.containsKey(user)) {
			pillars.get(user).shatter();
		}
		if (sPillars.containsKey(user)) {
			sPillars.get(user).shatter();
		}
	}

	@Override
	protected void options() {
		consume = option("consume-item", true, "Whether item should be consumed when power is used.");
		cooldown = option("cooldown", PowerTime.toMillis(45, 0), "Amount of time before power can be used again.");
		item = option("item", new ItemStack(Material.SEA_LANTERN), "Item used to trigger pillars.");
		pDur = option("pillar-dureation", PowerTime.toMillis(30, 0), "How long pillars last once created.");
		pHeight = option("pillar-height", 5, "Height of pillars.");
		pRange = option("pillar-range", 4, "Distance the pillars are in meters away from the origin point.");
		sPRange = option("superpower.pillar-range", 8, "Distance the extra pillars are in meters away from the origin point.");
		pillarsSummoned = stat("pillars-summoned", 60, "Number of pillars summoned", "[act:item]ing the top of the same block creates a second circle of pillars farther away.");
		supplies(new ItemStack(item.getType(), item.getMaxStackSize() / 4));
	}
	
	private void doPillars(final PowerUser user, Block block, boolean isSuper) {
		Block[] blocks;
		if (isSuper) {
			blocks = new Block[] {block.getRelative(BlockFace.EAST, sPRange).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.WEST, sPRange).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.NORTH_EAST, sPRange / 2).getRelative(BlockFace.NORTH, sPRange / 2).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.NORTH_WEST, sPRange / 2).getRelative(BlockFace.NORTH, sPRange / 2).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.SOUTH_EAST, sPRange / 2).getRelative(BlockFace.SOUTH, sPRange / 2).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.SOUTH_WEST, sPRange / 2).getRelative(BlockFace.SOUTH, sPRange / 2).getRelative(BlockFace.UP)};
			user.increaseStat(pillarsSummoned, 6);
		}
		else {
			user.setCooldown(this, cooldown);
			blocks = new Block[] {block.getRelative(BlockFace.NORTH, pRange).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.SOUTH, pRange).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.NORTH_EAST, pRange / 2).getRelative(BlockFace.EAST, pRange / 2).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.NORTH_WEST, pRange / 2).getRelative(BlockFace.WEST, pRange / 2).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.SOUTH_EAST, pRange / 2).getRelative(BlockFace.EAST, pRange / 2).getRelative(BlockFace.UP),
					block.getRelative(BlockFace.SOUTH_WEST, pRange / 2).getRelative(BlockFace.WEST, pRange / 2).getRelative(BlockFace.UP)};
			user.increaseStat(pillarsSummoned, 6);
		}
		if (blocks != null) {
			for (final Block b : blocks) {
				for (int i = 0; i < pHeight; i ++) {
					runTaskLater(new Runnable() {
						@Override
						public void run() {
							Block air = PowerTools.getHighestAirBlock(b.getLocation(), pHeight);
							if (air != null
									&& !air.getType().isSolid()) {
								FallingBlock fall = air.getWorld().spawnFallingBlock(air.getLocation(), Material.SEA_LANTERN.createBlockData());
								fall.setDropItem(false);
								falling.put(fall, user);
								if (isSuper) {
									superFalling.add(fall);
								}
							}
						}
					}, i * 5);
				}
			}
		}
		PowerTools.blockDisguise(block, Material.SEA_LANTERN);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onChange(EntityChangeBlockEvent event) {
		if (event.getEntity() instanceof FallingBlock) {
			FallingBlock fallBlock = (FallingBlock) event.getEntity();
			if (falling.containsKey(fallBlock)
					&& pillars.containsKey(falling.get(fallBlock))) {
				PowerUser user = falling.get(fallBlock);
				if (superFalling.contains(fallBlock)
						&& sPillars.containsKey(user)) {
					Pillar pillar = sPillars.get(user);
					pillar.addBlock(event.getBlock());
				}
				else {
					Pillar pillar = pillars.get(user);
					pillar.addBlock(event.getBlock());
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this
				&& event.hasBlock()) {
			PowerUser user = event.getUser();
			boolean isSuper = user.hasStatMaxed(pillarsSummoned)
					&& pillars.containsKey(user)
					&& event.getClickedBlock().equals(pillars.get(user).getCore());
			if (user.getCooldown(this) <= 0
					|| isSuper) {
				Pillar pillar = new Pillar(user, event.getClickedBlock(), isSuper);
				doPillars(user, event.getClickedBlock(), isSuper);
				if (consume) {
					event.consumeItem();
				}
				if (isSuper) {
					sPillars.put(user, pillar);
					pillar.setLife(pillars.get(user).getLife());
				}
				else {
					pillars.put(user, pillar);
				}
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	private class Pillar implements Listener {
		
		private Map<UUID, Location> inside, outside;
		
		private final List<Block> blocks;
		private final Block core;
		private boolean isSuper;
		private long life = System.currentTimeMillis() + pDur;
		private final int task;
		private final PowerUser user;
		
		public Pillar(PowerUser user, Block core, boolean isSuper) {
			this.user = user;
			this.blocks = new ArrayList<>();
			this.core = core;
			this.isSuper = isSuper;
			inside = new HashMap<>();
			outside = new HashMap<>();
			task = runTaskTimer(manage, 0L, 0L).getTaskId();
			registerEvents(this);
		}
		
		public void addBlock(Block block) {
			blocks.add(block);
		}
		
		public Block getCore() {
			return this.core;
		}
		
		public long getLife() {
			return this.life;
		}
		
		public void setLife(long life) {
			this.life = life;
		}
		
		public void shatter() {
			for (int i = 0; i < blocks.size(); i ++) {
				Block block = blocks.get(i);
				block.setType(Material.AIR);
				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.SEA_LANTERN);
			}
			if (pillars.containsKey(user)
					&& pillars.get(user) == this) {
				pillars.remove(user);
			}
			if (sPillars.containsKey(user)
					&& sPillars.get(user) == this) {
				sPillars.remove(user);
			}
			PowerTools.blockUpdate(core);
			blocks.clear();
			inside.clear();
			outside.clear();
			unregisterEvents(this);
			cancelTask(task);
		}
		
		private Runnable manage = new BukkitRunnable() {
			@Override
			public void run() {
				if (life > System.currentTimeMillis()) {
					double range = isSuper ? sPRange : pRange;
					for (Entity entity : PowerTools.getNearbyEntities(Entity.class, core.getLocation(), range + 2)) {
						if (entity != user.getPlayer()
								&& !falling.containsKey(entity)
								&& !superFalling.contains(entity)) {
							Location checkLoc = core.getLocation().clone();
							checkLoc.setY(entity.getLocation().getY());
							if (!inside.containsKey(entity.getUniqueId())
									&& !outside.containsKey(entity.getUniqueId())) {
								if (entity.getLocation().distanceSquared(checkLoc) > range * range) {
									outside.put(entity.getUniqueId(), entity.getLocation().clone());
								}
								else {
									inside.put(entity.getUniqueId(), entity.getLocation().clone());
								}
							}
							if (inside.containsKey(entity.getUniqueId()) && checkLoc.distanceSquared(entity.getLocation()) > range * range) {
								entity.teleport(inside.get(entity.getUniqueId()));
							}
							else if (outside.containsKey(entity.getUniqueId()) && checkLoc.distanceSquared(entity.getLocation()) <= range * range) {
								entity.teleport(outside.get(entity.getUniqueId()));
							}
							else {
								if (inside.containsKey(entity.getUniqueId())) {
									inside.put(entity.getUniqueId(), entity.getLocation().clone());
								}
								else if (outside.containsKey(entity.getUniqueId())) {
									outside.put(entity.getUniqueId(), entity.getLocation().clone());
								}
							}
						}
					}
				}
				else {
					shatter();
				}
			}
		};

		@EventHandler(ignoreCancelled = true)
		private void onBreak(BlockBreakEvent event) {
			if (blocks.contains(event.getBlock())) {
				event.setCancelled(true);
			}
		}
		
	}

}
