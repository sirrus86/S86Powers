package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Trickshot", type = PowerType.OFFENSE, author = "sirrus86", concept = "sirrus86", version = MCVersion.v1_14, icon = Material.BOW,
	description = "While holding a bow at maximum power, targets are highlighted. Upon release, the arrow will attempt to home in on the highlighted target.")
public class Trickshot extends Power {

	private Map<PowerUser, Integer> aiming;
	private Map<Entity, Integer> arrows;
	private Map<PowerUser, LivingEntity> targets;
	
	private double maxDist, maxAngle;
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
			PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), false);
		}
	}

	@Override
	protected void options() {
		maxDist = option("maximum-target-distance", 50.0D, "Maximum distance for which targets can be chosen.");
		maxAngle = option("maximum-angle", 0.12D, "Maximum rotational angle homing arrows will turn.");
		targetDelay = option("target-delay", PowerTime.toMillis(1, 500), "How long after aiming bow before target acquisition should begin.");
		targetsHit = stat("targets-hit", 50, "Targets hit with homing arrows", "Homing arrows that miss will sometimes ricochet.");
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
							PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), false);
						}
						targets.put(user, target);
						PowerTools.showAsSpectral(user.getPlayer(), target, true);
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
					PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), false);
				}
				((Projectile) event.getProjectile()).setBounce(user.hasStatMaxed(targetsHit));
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
				PowerTools.showAsSpectral(user.getPlayer(), targets.get(user), false);
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
		Projectile arrow = event.getEntity();
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
						&& event.getHitBlockFace() != null
						&& user.hasStatMaxed(targetsHit)) {
					Vector velocity = arrow.getVelocity().clone();
					Vector newVelocity;
					switch (event.getHitBlockFace()) {
						case NORTH: case SOUTH: {
							newVelocity = new Vector(velocity.getX(), velocity.getY(), -velocity.getZ());
							break;
						}
						case EAST: case WEST: {
							newVelocity = new Vector(-velocity.getX(), velocity.getY(), velocity.getZ());
							break;
						}
						case DOWN: case UP: {
							newVelocity = new Vector(velocity.getX(), -velocity.getY(), velocity.getZ());
							break;
						}
						default: newVelocity = velocity;
					}
					Vector newDirection = newVelocity.clone().normalize();
					Projectile newArrow = arrow.getWorld().spawnArrow(arrow.getLocation(), newDirection, 1.0F, 1.0F);
					newArrow.setShooter(user.getPlayer());
					arrow.remove();
					newArrow.setVelocity(newVelocity);
					Predicate<Entity> pred = entity -> {
						return entity != newArrow;
					};
					RayTraceResult rayTrace = newArrow.getWorld().rayTrace(newArrow.getLocation(),
							newDirection, maxDist, FluidCollisionMode.NEVER, true, 1.0D, pred);
					if (rayTrace != null
							&& rayTrace.getHitEntity() != null
							&& rayTrace.getHitBlock() == null
							&& rayTrace.getHitEntity() instanceof LivingEntity) {
						Entity target = rayTrace.getHitEntity();
						arrows.put(newArrow, runTaskLater(homingTask(newArrow, (LivingEntity) target), 3L).getTaskId());
					}
				}
			}
		}
	}	

}
