package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Locust Swarm", type = PowerType.DEFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.STONE,
	description = "[act:item]ing while holding [item] will cause Silverfish to break from any nearby [infested-only]infested [/infested-only]stone blocks,"
			+ " targeting nearby entities at random. After [silverfish-lifespan], spawned Silverfish reform into blocks. [cooldown] cooldown.")
public final class LocustSwarm extends Power {
	
	private Map<PowerUser, Set<Swarm>> swarms;
	
	private PowerOption<Boolean> infestOnly;
	private PowerOption<Long> lifespan;
	private PowerOption<List<String>> spawnBlocks;
	private PowerStat summonCount;
	private PowerOption<Integer> summonMax;
	private PowerOption<Double> summonRad;
	
	@Override
	protected void onEnable() {
		swarms = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (swarms.containsKey(user)
				&& swarms.get(user) != null) {
			for (Swarm swarm : swarms.get(user)) {
				swarm.killOff();
			}
		}
	}

	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(30, 0), "How long before power can be used again.");
		infestOnly = option("infested-only", true, "Whether only infested stone blocks should spawn Silverfish. If false, all nearby stone blocks will spawn them.");
		item = option("item", new ItemStack(Material.ROTTEN_FLESH, 1), "Item used to summon silverfish.");
		lifespan = option("silverfish-lifespan", PowerTime.toMillis(15, 0), "How long before silverfish should despawn or reform.");
		spawnBlocks = option("spawnable-blocks", List.of("CHISELED_STONE_BRICKS", "COBBLESTONE", "CRACKED_STONE_BRICKS", "DEEPSLATE", "INFESTED_CHISELED_STONE_BRICKS",
				"INFESTED_COBBLESTONE", "INFESTED_CRACKED_STONE_BRICKS", "INFESTED_DEEPSLATE", "INFESTED_MOSSY_STONE_BRICKS", "INFESTED_STONE", "INFESTED_STONE_BRICKS",
				"MOSSY_STONE_BRICKS", "STONE", "STONE_BRICKS"), "Blocks which can spawn silverfish when summoned.");
		summonMax = option("summon-maximum", 15, "Maximum number of silverfish that can be summoned at one time.");
		summonRad = option("summon-radius", 10.0D, "Radius at which silverfish are summoned.");
		summonCount = stat("summon-count", 100, "Silverfish summoned",
				"Can now mine infested stone using a Silk Touch pickaxe. Left-clicking while holding a stack of infested stone in your main hand will cause the entire stack to hatch.");
		supplies(getRequiredItem());
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onBreak(BlockBreakEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& user.hasStatMaxed(summonCount)
				&& event.getBlock().getType().name().startsWith("INFESTED")) {
			ItemStack item = user.getPlayer().getInventory().getItemInMainHand();
			if (item.getType().name().contains("PICKAXE")
					&& item.hasItemMeta()
					&& item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(),
						new ItemStack(event.getBlock().getType(), 1));
				event.getBlock().setType(Material.AIR);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	private void onInteract(PlayerInteractEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& user.hasStatMaxed(summonCount)
				&& event.getHand() == EquipmentSlot.HAND
				&& event.getItem() != null
				&& event.getItem().getType().name().startsWith("INFESTED")) {
			if (user.getCooldown(this) <= 0) {
				ItemStack item = event.getItem();
				int count = item.getAmount();
				Map<Silverfish, Material> sList = new HashMap<>();
				for (int i = 0; i < Math.min(count, user.getOption(summonMax)); i ++) {
					Silverfish sfish = user.getPlayer().getWorld().spawn(user.getPlayer().getLocation(), Silverfish.class);
					sList.put(sfish, item.getType());
					item.setAmount(item.getAmount() - 1);
				}
				Swarm swarm = new Swarm(user, sList);
				if (!swarms.containsKey(user)
						|| swarms.get(user) == null) {
					swarms.put(user, new HashSet<>());
				}
				swarms.get(user).add(swarm);
				user.setCooldown(this, user.getOption(cooldown));
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player
				&& event.getEntity() instanceof Silverfish) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0L) {
				Swarm swarm = new Swarm(user, user.getPlayer().getLocation());
				if (!swarms.containsKey(user)
						|| swarms.get(user) == null) {
					swarms.put(user, new HashSet<>());
				}
				swarms.get(user).add(swarm);
				user.setCooldown(this, user.getOption(cooldown));
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	private class Swarm implements Listener {
		
		private Map<Silverfish, Material> sList = new HashMap<>();
		
		private int kTask = -1;
		private final PowerUser owner;
		
		public Swarm(PowerUser owner, Map<Silverfish, Material> sfish) {
			this.owner = owner;
			for (Silverfish fish : sfish.keySet()) {
				PowerTools.setTamed(fish, this.owner);
				Set<LivingEntity> targetList = new HashSet<>();
				targetList.addAll(sList.keySet());
				targetList.add(owner.getPlayer());
				fish.setTarget(PowerTools.getRandomEntity(fish, 10.0D, targetList));
				sList.putAll(sfish);
			}
			registerEvents(this);
			prepareKillOff();
		}
		
		public Swarm(PowerUser owner, Location loc) {
			this.owner = owner;
			for (int i = 0; i < owner.getOption(summonRad); i ++) {
				for (Block block : PowerTools.getNearbyBlocks(loc, i)) {
					if (sList.size() < owner.getOption(summonMax)
							&& owner.getOption(spawnBlocks).contains(block.getType().name())
							&& (!owner.getOption(infestOnly) || block.getType().name().startsWith("INFESTED"))) {
						Material mat = block.getState().getType();
						loc.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
						block.setType(Material.AIR);
						Silverfish sfish = loc.getWorld().spawn(block.getLocation(), Silverfish.class);
						sList.put(sfish, mat);
						PowerTools.setTamed(sfish, this.owner);
						Set<LivingEntity> targetList = new HashSet<>();
						targetList.addAll(sList.keySet());
						targetList.add(owner.getPlayer());
						sfish.setTarget(PowerTools.getRandomEntity(sfish, 10.0D, targetList));
						owner.increaseStat(summonCount, 1);
					}
				}
			}
			registerEvents(this);
			prepareKillOff();
		}
		
		public void killOff() {
			for (Silverfish sfish : sList.keySet()) {
				if (sfish.isValid()
						&& !sfish.isDead()) {
					sfish.getLocation().getBlock().setType(sList.get(sfish).name().startsWith("INFESTED") ? sList.get(sfish) : Material.valueOf("INFESTED_" + sList.get(sfish).name()));
					sfish.getWorld().playEffect(sfish.getLocation(), Effect.STEP_SOUND, sList.get(sfish));
					sfish.remove();
				}
				PowerTools.setTamed(sfish, null);
			}
			sList.clear();
			swarms.get(owner).remove(this);
			if (kTask >= 0) {
				cancelTask(kTask);
			}
			unregisterEvents(this);
		}
		
		private void prepareKillOff() {
			kTask = runTaskLater(new BukkitRunnable() {
				@Override
				public void run() {
					killOff();
				}
			}, PowerTime.toTicks(owner.getOption(lifespan))).getTaskId();
		}
		
		@EventHandler (ignoreCancelled = true)
		private void onSpawn(CreatureSpawnEvent event) {
			if (!sList.containsKey(event.getEntity())
					&& event.getEntity() instanceof Silverfish
					&& event.getSpawnReason() == SpawnReason.SILVERFISH_BLOCK) {
				Silverfish sfish = (Silverfish) event.getEntity();
				for (Silverfish fish : sList.keySet()) {
					if (fish.isValid()
							&& fish.getWorld().equals(sfish.getWorld())
							&& fish.getLocation().distanceSquared(sfish.getLocation()) < owner.getOption(summonRad) * owner.getOption(summonRad)) {
						sList.put(sfish, Material.STONE);
						PowerTools.setTamed(sfish, owner);
						owner.increaseStat(summonCount, 1);
						return;
					}
				}
			}
		}
		
		@EventHandler
		private void onDeath(EntityDeathEvent event) {
			if (sList.containsKey(event.getEntity())) {
				PowerTools.setTamed((Silverfish) event.getEntity(), null);
			}
		}
		
		@EventHandler (ignoreCancelled = true)
		private void onTarget(EntityTargetEvent event) {
			if (event.getTarget() instanceof Player
					&& this.sList.containsKey(event.getEntity())) {
				PowerUser target = getUser((Player) event.getTarget());
				if (target == this.owner
						|| sList.containsKey(event.getTarget())) {
					event.setCancelled(true);
				}
			}
		}
		
	}

}
