package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PigZombieAngerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Summoner", type = PowerType.DEFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.BLAZE_ROD, usesPackets = true,
	description = "Most monsters from the nether will ignore you. [act:item]ing the ground while holding [item] will create a portal which summons a random minion from the nether."
			+ " The summoned minion will follow and defend you. Summoning only works in the overworld and The End. [cooldown] cooldown.")
public final class Summoner extends Power {

	private Map<PowerUser, LivingEntity> minions;
	
	private final EnumSet<EntityType> nMobs = EnumSet.of(EntityType.BLAZE, EntityType.GHAST, EntityType.HOGLIN, EntityType.MAGMA_CUBE, EntityType.PIGLIN,
			EntityType.PIGLIN_BRUTE, EntityType.STRIDER, EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN);
	
	private PowerOption<Integer> bSpawn, gSpawn, pjSpawn, pzSpawn, wsSpawn;
	private String noRoom;
	
	private final Orientable portalData = (Orientable) Material.NETHER_PORTAL.createBlockData();
	
	@Override
	protected void onEnable() {
		minions = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (minions.containsKey(user)) {
			minions.get(user).damage(200);
			minions.remove(user);
		}
	}

	@Override
	protected void config() {
		bSpawn = option("summon-chance.blaze", 10, "Chance blazes will be summoned. Higher number means greater chance, 0 means no chance.");
		cooldown = option("cooldown", PowerTime.toMillis(5, 0, 0), "Amount of time before power can be used again.");
		gSpawn = option("summon-chance.ghast", 3, "Chance ghasts will be summoned. Higher number means greater chance, 0 means no chance.");
		item = option("item", new ItemStack(Material.BLAZE_ROD), "Item used to summon minions.");
		pjSpawn = option("summon-chance.pigzombie-jockey", 1, "Chance pigzombie jockeys will be summoned. Higher number means greater chance, 0 means no chance.");
		pzSpawn = option("summon-chance.pigzombie", 30, "Chance pigzombies will be summoned. Higher number means greater chance, 0 means no chance.");
		wsSpawn = option("summon-chance.wither-skeleton", 10, "Chance blazes will be summoned. Higher number means greater chance, 0 means no chance.");
		noRoom = locale("message.no-room", ChatColor.RED + "No room to summon anything!");
		supplies(getRequiredItem());
	}
	
	private SummonType getSummonType(Location loc, BlockFace face) {
		Location testLoc = loc.add(face.getModX(), 1, face.getModZ());
		List<SummonType> tmp = new ArrayList<>();
		for (SummonType type : SummonType.values()) {
			if (type == SummonType.BLAZE
					|| type == SummonType.PIG_JOCKEY
					|| type == SummonType.PIG_ZOMBIE) {
				if (!testLoc.getBlock().getType().isSolid()
						&& !testLoc.getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
					int max = getOption(bSpawn);
					if (type == SummonType.PIG_JOCKEY) {
						max = getOption(pjSpawn);
					}
					else if (type == SummonType.PIG_ZOMBIE) {
						max = getOption(pzSpawn);
					}
					for (int i = 0; i < max - 1; i ++) {
						tmp.add(type);
					}
				}
			}
			else if (type == SummonType.GHAST) {
				Block block = testLoc.getBlock().getRelative(face.getModX(), 1, face.getModZ());
				boolean canSpawn = true;
				for (BlockFace check : BlockFace.values()) {
					if (Math.abs(check.getModX()) <= 1
							&& Math.abs(check.getModY()) <= 1
							&& Math.abs(check.getModZ()) <= 1) {
						if (block.getRelative(check).getType().isSolid()) {
							canSpawn = false;
							break;
						}
					}
				}
				if (canSpawn) {
					for (int i = 0; i < getOption(gSpawn) - 1; i ++) {
						tmp.add(type);
					}
				}
			}
			else if (type == SummonType.WITHER_SKELETON) {
				if (!testLoc.getBlock().getType().isSolid()
						&& !testLoc.getBlock().getRelative(BlockFace.UP).getType().isSolid()
						&& !testLoc.getBlock().getRelative(BlockFace.UP, 2).getType().isSolid()) {
					for (int i = 0; i < getOption(wsSpawn) - 1; i ++) {
						tmp.add(type);
					}
				}
			}
		}
		return tmp.isEmpty() ? null : tmp.get(random.nextInt(tmp.size()));
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		if (minions.containsValue(event.getEntity())) {
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onTarget(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() instanceof Player
				&& nMobs.contains(event.getEntityType())) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onAnger(PigZombieAngerEvent event) {
		if (event.getTarget() instanceof Player) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)
					&& minions.get(user) == event.getEntity()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this
				&& event.hasBlock()
				&& event.getBlockFace() == BlockFace.UP
				&& event.getClickedBlock().getWorld().getEnvironment() != Environment.NETHER) {
			PowerUser user = event.getUser();
			if (user.allowPower(this)) {
				if (user.getCooldown(this) <= 0) {
					BlockFace dir = PowerTools.getDirection(user.getPlayer().getLocation(), false);
					SummonType type = getSummonType(event.getClickedBlock().getLocation(), dir);
					if (type != null) {
						if (minions.containsKey(user)
								&& !minions.get(user).isDead()) {
							LivingEntity minion = minions.get(user);
							AttributeInstance health = minion.getAttribute(Attribute.GENERIC_MAX_HEALTH);
							if (health != null) {
								minion.damage(health.getValue());
							}
							minions.remove(user);
						}
						int height = type == SummonType.PIG_JOCKEY ? 3 : 4;
						int minX = -Math.abs(type == SummonType.GHAST ? dir.getModZ() * 2 : dir.getModZ());
						int maxX = Math.abs(type == SummonType.PIG_JOCKEY ? dir.getModZ() : dir.getModZ() * 2);
						int minZ = -Math.abs(type == SummonType.GHAST ? dir.getModX() * 2 : dir.getModX());
						int maxZ = Math.abs(type == SummonType.PIG_JOCKEY ? dir.getModX() : dir.getModX() * 2);
						List<Block> frame = new ArrayList<>(),
								portal = new ArrayList<>();
						for (int x = minX; x <= maxX; x ++) {
							for (int y = 0; y <= height; y ++) {
								for (int z = minZ; z <= maxZ; z ++) {
									if ((x == minX && minX != 0)
											|| (x == maxX && maxX != 0)
											|| (z == minZ && minZ != 0)
											|| (z == maxZ && maxZ != 0)
											|| y == 0
											|| y == height) {
										frame.add(event.getClickedBlock().getRelative(x, y, z));
									}
									else {
										portal.add(event.getClickedBlock().getRelative(x, y, z));
									}
								}
							}
						}
						new SummonPortal(user, event.getClickedBlock().getLocation(), frame, portal, dir, type);
						user.setCooldown(this, user.getOption(cooldown));
					}
					else {
						user.getPlayer().sendMessage(noRoom);
					}
				}
				else {
					user.showCooldown(this);
				}
			}
		}
	}
	
	private class SummonPortal {
		
		private final Collection<Block> frame, portal;
		private final BlockFace face;

		public SummonPortal(PowerUser owner, Location loc, Collection<Block> frame, Collection<Block> portal, BlockFace face, SummonType type) {
			this.face = face;
			this.frame = frame;
			this.portal = portal;
			renderPortal();
			Runnable delayedSpawn = () -> {
				Location spawn = loc.add(face.getModX(), 1, face.getModZ());
				LivingEntity minion = null;
				if (spawn.getWorld() != null) {
					switch (type) {
						case BLAZE -> minion = spawn.getWorld().spawn(spawn, Blaze.class);
						case GHAST -> minion = spawn.getWorld().spawn(spawn, Ghast.class);
						case PIG_JOCKEY -> {
							minion = spawn.getWorld().spawn(spawn, PigZombie.class);
							Chicken ch = spawn.getWorld().spawn(spawn, Chicken.class);
							((PigZombie) minion).setBaby();
							ch.addPassenger(minion);
						}
						case PIG_ZOMBIE -> minion = spawn.getWorld().spawn(spawn, PigZombie.class);
						case WITHER_SKELETON -> minion = spawn.getWorld().spawn(spawn, WitherSkeleton.class);
					}
					if (minion != null) {
						if (minion instanceof Creature) {
							PowerTools.setTamed((Creature) minion, owner);
						}
						minions.put(owner, minion);
					}
				}
			};
			runTaskLater(delayedSpawn, 20L);
			BukkitRunnable breakPortal = new BukkitRunnable() {
				@Override
				public void run() {
					for (Block block : frame) {
						block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.OBSIDIAN);
					}
					for (Block block : portal) {
						block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.NETHER_PORTAL);
					}
					PowerTools.blockUpdate(frame);
					PowerTools.blockUpdate(portal);
				}
			};
			runTaskLater(breakPortal, 40L);
		}
		
		private void renderPortal() {
			runTask(() -> {
				PowerTools.blockDisguise(frame, Material.OBSIDIAN, Material.OBSIDIAN.createBlockData());
				portalData.setAxis(PowerTools.getAxis(face));
				PowerTools.blockDisguise(portal, Material.NETHER_PORTAL, portalData);
			});
		}

	}
	
	private enum SummonType {
		
		BLAZE, GHAST, PIG_JOCKEY, PIG_ZOMBIE, WITHER_SKELETON
	}

}