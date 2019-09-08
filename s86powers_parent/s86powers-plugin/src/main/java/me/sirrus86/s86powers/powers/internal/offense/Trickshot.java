package me.sirrus86.s86powers.powers.internal.offense;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Trickshot", type = PowerType.OFFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.BOW, usesPackets = true,
	description = "While holding a bow at maximum power, targets are highlighted. Upon release, the arrow will attempt to home in on the highlighted target.")
public final class Trickshot extends Power {

	private Map<PowerUser, Integer> aiming;
	private Map<Entity, Integer> arrows;
	private Map<PowerUser, LivingEntity> targets;
	
	private double maxDist, maxAngle;
	private boolean targetAnimals, targetMonsters, targetPlayers, targetVillagers;
	private long targetDelay;
	private PowerStat targetsHit;
	
	@Override
	protected void onEnable() {
		aiming = new ConcurrentHashMap<>();
		arrows = new HashMap<>();
		targets = new HashMap<>();
	}
	
	@Override
	protected void onDisable() {
		super.onDisable();
		for (Entity arrow : arrows.keySet()) {
			cancelTask(arrows.get(arrow));
		}
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (aiming.containsKey(user)) {
			cancelTask(aiming.get(user));
		}
		if (targets.containsKey(user)) {
			PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), ChatColor.WHITE, false);
		}
	}

	@Override
	protected void config() {
		maxDist = option("maximum-target-distance", 50.0D, "Maximum distance for which targets can be chosen.");
		maxAngle = option("maximum-angle", 0.12D, "Maximum rotational angle homing arrows will turn.");
		targetAnimals = option("target-animals", false, "Whether animals should be targetted by ricochet arrows.");
		targetDelay = option("target-delay", PowerTime.toMillis(1, 500), "How long after aiming bow before target acquisition should begin.");
		targetMonsters = option("target-monsters", true, "Whether monsters should be targetted by ricochet arrows.");
		targetPlayers = option("target-players", true, "Whether players should be targetted by ricochet arrows.");
		targetsHit = stat("targets-hit", 50, "Targets hit with homing arrows", "Arrows that miss will often ricochet.");
		targetVillagers = option("target-villagers", false, "Whether villagers should be targetted by ricochet arrows.");
		supplies(new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 64));
	}
	
	private Runnable homingTask(Entity arrow, LivingEntity target) {
		return new BukkitRunnable() {

			// Credit for algorithm goes to sethbling
			@Override
			public void run() {
				if (target.isValid()
						&& !target.isDead()) {
					double speed = arrow.getVelocity().length();
					Vector toTarget = target.getEyeLocation().clone().subtract(arrow.getLocation()).toVector();
					Vector dirVelocity = arrow.getVelocity().clone().normalize();
					Vector dirToTarget = toTarget.clone().normalize();
					double angle = dirVelocity.angle(dirToTarget);
					double newSpeed = 0.9D * speed + 0.14D;
					Vector newVelocity;
					if (angle < maxAngle) {
						newVelocity = dirVelocity.clone().multiply(newSpeed);
					}
					else {
						Vector newDir = dirVelocity.clone().multiply((angle - maxAngle) / angle).add(dirToTarget.clone().multiply(maxAngle / angle));
						newDir.normalize();
						newVelocity = newDir.clone().multiply(newSpeed);
					}
					arrow.setVelocity(newVelocity.add(new Vector(0.0D, 0.03D, 0.0D)));
					arrows.put(arrow, getInstance().runTaskLater(homingTask(arrow, target), 1L).getTaskId());
				}
			}
			
		};
	}
	
	private Runnable targetTask(PowerUser user) {
		return new BukkitRunnable() {

			@Override
			public void run() {
				if (aiming.containsKey(user)) {
					LivingEntity target = user.getTargetEntity(LivingEntity.class, maxDist);
					if (target != null) {
						if (targets.containsKey(user)
								&& targets.get(user) != target) {
							PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), ChatColor.WHITE, false);
						}
						targets.put(user, target);
						PowerTools.showAsSpectral(user.getPlayer(), target, ChatColor.WHITE, true);
					}
					aiming.put(user, getInstance().runTaskLater(targetTask(user), 1L).getTaskId());
				}
			}
			
		};
	}
	
	@EventHandler
	private void onShoot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (aiming.containsKey(user)) {
				cancelTask(aiming.get(user));
				if (targets.containsKey(user)) {
					arrows.put(event.getProjectile(), runTaskLater(homingTask(event.getProjectile(), targets.get(user)), 3L).getTaskId());
					PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), ChatColor.WHITE, false);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onChange(PlayerItemHeldEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& aiming.containsKey(user)) {
			cancelTask(aiming.get(user));
			if (targets.containsKey(user)) {
				PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), ChatColor.WHITE, false);
			}
		}
	}
	
	@EventHandler
	private void onAim(PlayerInteractEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& event.getItem() != null
				&& event.getItem().getType() == Material.BOW
				&& event.getAction().name().startsWith("RIGHT")) {
			aiming.put(user, runTaskLater(targetTask(user), PowerTime.toTicks(targetDelay)).getTaskId());
		}
	}
	
	@EventHandler
	private void onHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getEntity();
			if (arrows.containsKey(arrow)) {
				cancelTask(arrows.get(arrow));
			}
			if (arrow.getShooter() instanceof Player) {
				PowerUser user = getUser((Player) arrow.getShooter());
				if (user.allowPower(this)) {
					if (event.getHitEntity() != null
							&& arrows.containsKey(arrow)) {
						user.increaseStat(targetsHit, 1);
					}
					else if (event.getHitBlock() != null
							&& user.hasStatMaxed(targetsHit)) {
						float speed = (float) arrow.getVelocity().length();
						if (speed > 2.0F) {
							Predicate<Entity> pred = entity -> {
								return entity instanceof LivingEntity
										&& entity != arrow
										&& entity != user.getPlayer()
										&& (targetAnimals || !(entity instanceof Animals))
										&& (targetMonsters || !(entity instanceof Monster))
										&& (targetPlayers || !(entity instanceof Player))
										&& (targetVillagers || !(entity instanceof AbstractVillager));
							};
							LivingEntity target = null;
							Vector direction = null;
							List<Entity> entities = arrow.getNearbyEntities(maxDist / 3.0D, maxDist / 3.0D, maxDist / 3.0D);
							Collections.shuffle(entities);
							for (Entity entity : entities) {
								if (entity instanceof LivingEntity) {
									Location targetLoc = entity.getLocation().clone();
									direction = targetLoc.subtract(arrow.getLocation()).toVector().normalize();
									target = PowerTools.getTargetEntity(LivingEntity.class, arrow.getLocation(), direction, maxDist / 3.0D, pred);
									if (target != null) {
										break;
									}
								}
							}
							if (target != null
									&& direction != null) {
								Projectile newArrow = arrow.getWorld().spawnArrow(arrow.getLocation(), direction, speed * 0.9F, 0.0F);
								newArrow.setShooter(user.getPlayer());
								arrow.remove();
							}
						}
					}
				}
			}
		}
	}	

}
