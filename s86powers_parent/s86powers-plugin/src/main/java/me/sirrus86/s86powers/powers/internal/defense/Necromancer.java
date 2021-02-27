package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Sets;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Necromancer", type = PowerType.DEFENSE, author = "sirrus86", concept = "nazizombies2354", icon=Material.BONE,
	description = "[act:item]ing while holding [item] creates an aura of death around you. The aura causes nearby vegetation to decay,"
			+ " causes weakness to the living, and pulls the dead from their graves. Reanimated undead will follow and defend you. [cooldown] cooldown.")
public final class Necromancer extends Power {

	private final Set<Material> turnToDirt = Sets.newHashSet(Material.GRASS_BLOCK, Material.MYCELIUM, Material.PODZOL);
	
	private Set<Aura> auras;
	private Map<Monster, PowerUser> minions;
	
	private long auraDur;
	private int auraRad, maxMinions, wkLvl;
	private boolean noIgnite;
	private PowerStat summons;
	
	@Override
	protected void onEnable() {
		auras = new HashSet<>();
		minions = new HashMap<>();
	}

	@Override
	protected void onDisable(PowerUser user) {
		Iterator<Aura> it = auras.iterator();
		while (it.hasNext()) {
			Aura aura = it.next();
			if (aura.getOwner() == user) {
				aura.destroy();
			}
		}
		Iterator<Monster> it2 = minions.keySet().iterator();
		while (it2.hasNext()) {
			Monster mob = it2.next();
			if (minions.get(mob) == user) {
				Monster newMob = (Monster) mob.getWorld().spawnEntity(mob.getLocation(), mob.getType());
				mob.remove();
				newMob.setTarget(user.getPlayer());
				it2.remove();
			}
		}
	}

	@Override
	protected void config() {
		auraDur = option("aura-duration", PowerTime.toMillis(10, 0), "Amount of time an aura will last before dissipating.");
		auraRad = option("aura-radius", 5, "Maximum radius of a given aura.");
		cooldown = option("cooldown", PowerTime.toMillis(30, 0), "Amount of time before power can be used again.");
		item = option("item", new ItemStack(Material.ROTTEN_FLESH), "Item used to create auras.");
		maxMinions = option("maximum-undead", 3, "Maximum number of undead that can be reanimated. Players will be reanimated regardless of whether the cap is met.");
		noIgnite = option("prevent-ignition", true, "Whether to prevent undead from igniting in sunlight.");
		summons = stat("undead-reanimated", 75, "Undead reanimated", "Players killed while in your aura will immediately be resurrected as an undead wearing their equipment.");
		wkLvl = option("weakness-amplifier", 4, "Amplification of weakness effect on living entities within an aura.");
		supplies(item, new ItemStack(item.getType(), item.getMaxStackSize()));
	}
	
	private boolean hasTooManyMinions(PowerUser user) {
		int i = 0;
		for (Entry<Monster, PowerUser> entry : minions.entrySet()) {
			if (entry.getValue() == user
					&&entry.getKey().isValid()) {
				i ++;
			}
			if (i >= maxMinions) return true;
		}
		return false;
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onIgnite(EntityCombustEvent event) {
		if (noIgnite
				&& !(event instanceof EntityCombustByBlockEvent)
				&& !(event instanceof EntityCombustByEntityEvent)
				&& minions.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.SKELETON
				|| event.getEntityType() == EntityType.ZOMBIE) {
			if (minions.containsKey(event.getEntity())) {
				minions.remove(event.getEntity());
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onTarget(EntityTargetLivingEntityEvent event) {
		if ((event.getEntity() instanceof Skeleton || event.getEntity() instanceof Zombie)
				&& event.getTarget() instanceof Player) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		for (Aura aura : auras) {
			if (aura.getBlocks().contains(player.getLocation().getBlock())
					&& aura.getOwner().hasStatMaxed(summons)) {
				Zombie zombie = player.getWorld().spawn(player.getLocation(), Zombie.class);
				PowerTools.setTamed(zombie, aura.getOwner());
				zombie.setCustomName(player.getName() + "'s Zombie");
				Iterator<ItemStack> it = event.getDrops().iterator();
				while (it.hasNext()) {
					ItemStack drop = it.next();
					if (it == player.getInventory().getBoots()) {
						zombie.getEquipment().setBoots(drop);
						zombie.getEquipment().setBootsDropChance(1.0F);
						it.remove();
					}
					if (it == player.getInventory().getChestplate()) {
						zombie.getEquipment().setChestplate(drop);
						zombie.getEquipment().setChestplateDropChance(1.0F);
						it.remove();
					}
					if (it == player.getInventory().getHelmet()) {
						zombie.getEquipment().setHelmet(drop);
						zombie.getEquipment().setHelmetDropChance(1.0F);
						it.remove();
					}
					if (it == player.getInventory().getLeggings()) {
						zombie.getEquipment().setLeggings(drop);
						zombie.getEquipment().setLeggingsDropChance(1.0F);
						it.remove();
					}
					if (it == player.getInventory().getItemInMainHand()) {
						zombie.getEquipment().setItemInMainHand(drop);
						zombie.getEquipment().setItemInMainHandDropChance(1.0F);
						it.remove();
					}
					if (it == player.getInventory().getItemInOffHand()) {
						zombie.getEquipment().setItemInOffHand(drop);
						zombie.getEquipment().setItemInOffHandDropChance(1.0F);
						it.remove();
					}
				}
				minions.put(zombie, aura.getOwner());
				aura.getOwner().increaseStat(summons, 1);
				break;
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.allowPower(this)) {
				if (user.getCooldown(this) <= 0) {
					auras.add(new Aura(user));
					user.setCooldown(this, cooldown);
				}
				else {
					user.showCooldown(this);
				}
			}
		}
	}
	
	private class Aura {
		
		private Set<Block> blocks = new HashSet<Block>();
		private final Location loc;
		private final int task;
		private final long time;
		private final PowerUser user;
		
		public Aura(PowerUser user) {
			this.user = user;
			loc = user.getPlayer().getEyeLocation();
			time = System.currentTimeMillis() + auraDur;
			task = runTaskTimer(createAura, 0L, 5L).getTaskId();
		}
		
		public Set<Block> getBlocks() {
			return blocks;
		}
		
		public PowerUser getOwner() {
			return user;
		}
		
		public void destroy() {
			auras.remove(this);
			cancelTask(task);			
		}
		
		private Runnable createAura = new BukkitRunnable() {
			private int i = 0;
			@Override
			public void run() {
				int j = random.nextInt(5);
				if (i <= auraRad) {
					blocks.addAll(PowerTools.getNearbyBlocks(loc, i));
					i ++;
				}
				for (Block block : blocks) {
					if (turnToDirt.contains(block.getType())) {
						block.setType(Material.DIRT);
					}
					if (!block.getType().isSolid()
							&& !block.getRelative(BlockFace.UP).getType().isSolid()
							&& block.getRelative(BlockFace.DOWN).getType().isSolid()
							&& random.nextInt(5) == j
							&& !hasTooManyMinions(user)) {
						Block bDown = block.getRelative(BlockFace.DOWN);
						bDown.getWorld().playEffect(bDown.getLocation(), Effect.STEP_SOUND, bDown.getType());
						Monster newMob;
						if (random.nextInt(2) == 0) {
							newMob = block.getWorld().spawn(block.getLocation(), Skeleton.class);
						}
						else {
							newMob = block.getWorld().spawn(block.getLocation(), Zombie.class);
						}
						PowerTools.setTamed(newMob, user);
						minions.put(newMob, user);
						user.increaseStat(summons, 1);
					}
					PowerTools.playParticleEffect(block.getLocation(), Particle.TOWN_AURA, 1);
				}
				for (LivingEntity entity : PowerTools.getNearbyEntities(LivingEntity.class, loc, i)) {
					if (!(entity instanceof Skeleton || entity instanceof Zombie)
							&& entity != user.getPlayer()) {
						entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, wkLvl));
					}
				}
				if (System.currentTimeMillis() >= time) {
					destroy();
				}
			}
		};
		
	}

}