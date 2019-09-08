package me.sirrus86.s86powers.powers.internal.offense;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Auto Gun", type = PowerType.OFFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.DISPENSER, usesPackets = true,
	description = "[act:item]ing against a solid block while holding [item] will place a turret against that block. Turrets will search out and fire upon any living targets within [range] blocks.")
public final class AutoGun extends Power {

	private Map<PowerUser, List<Turret>> turrets;

	private int arrowsPerVolley, maxTurrets;
	private final MultipleFacing baseData = (MultipleFacing) Material.DARK_OAK_FENCE.createBlockData();
	private final Directional cartData = (Directional) Material.DISPENSER.createBlockData();
	private boolean consumeItem, ignoreInvis;
	private final Vector cartCenter = new Vector(0.0D, 0.8D, 0.0D);
	private PowerStat dmgByTurrets;
	private long fireDelay;
	private double range;

	@Override
	protected void onEnable() {
		cartData.setFacing(BlockFace.SOUTH);
		turrets = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (turrets.containsKey(user)
				&& turrets.get(user) != null) {
			for (int i = 0; i < turrets.get(user).size(); i ++) {
				turrets.get(user).get(0).destroy();
			}
		}
	}

	@Override
	protected void config() {
		arrowsPerVolley = option("arrows-per-volley", 3, "Number of arrows fired at a time by turrets.");
		consumeItem = option("consume-item", true, "Whether item should be consumed on use.");
		cooldown = option("cooldown", PowerTime.toMillis(1, 0), "Cooldown between placing turrets.");
		dmgByTurrets = stat("damage-by-turrets", 150, "Damage done by turrets", "Can now deploy [maxTurrets] turrets at a time.");
		fireDelay = option("firing-delay", PowerTime.toMillis(3, 0), "Amount of time before another volley of arrows can be shot by a turret.");
		ignoreInvis = option("ignore-invisible-targets", true, "Whether invisible targets should be ignored by turrets.");
		item = option("item", new ItemStack(Material.DISPENSER), "Item used to deploy turrets.");
		maxTurrets = option("maximum-turrets", 3, "Maximum number of turrets that can be deployed at one time.");
		range = option("turret-range", 25.0D, "Maximum range which turrets can detect targets.");
		supplies(new ItemStack(item.getType(), item.getMaxStackSize() / 4));
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this
				&& event.hasBlock()
				&& event.getUser().getCooldown(this) <= 0) {
			Block base = event.getClickedBlock().getRelative(event.getBlockFace());
			if (base.getType() == Material.AIR
					&& event.getClickedBlock().getType().isSolid()) {
				if (!turrets.containsKey(event.getUser())) {
					turrets.put(event.getUser(), new ArrayList<Turret>());
				}
				if ((!event.getUser().hasStatMaxed(dmgByTurrets) && turrets.get(event.getUser()).size() >= 1)
						|| (event.getUser().hasStatMaxed(dmgByTurrets) && turrets.get(event.getUser()).size() >= maxTurrets)) {
					turrets.get(event.getUser()).get(0).destroy();
				}
				for (BlockFace blockFace : baseData.getAllowedFaces()) {
					baseData.setFace(blockFace, false);
				}
				if (baseData.getAllowedFaces().contains(event.getBlockFace().getOppositeFace())) {
					baseData.setFace(event.getBlockFace().getOppositeFace(), true);
				}
				base.setBlockData(baseData);
				double modY = event.getBlockFace() == BlockFace.DOWN ? -0.5D : 0.5D;
				RideableMinecart cart = base.getWorld().spawn(base.getLocation().add(0.5D, modY, 0.5D), RideableMinecart.class);
				cart.setDisplayBlockData(cartData);
				Turret turret = new Turret(event.getUser(), cart, base);
				turrets.get(event.getUser()).add(turret);
				event.getUser().setCooldown(this, cooldown);
				if (consumeItem) {
					event.consumeItem();
				}
			}
		}
	}
	
	private class Turret implements Listener {
		
		private Set<Arrow> arrows;
		private final Block base;
		private final RideableMinecart cart;
		private final PowerUser owner;
		private final Predicate<Entity> predEntity;
		private LivingEntity target = null;
		private int task = -1;
		
		public Turret(PowerUser owner, RideableMinecart cart, Block base) {
			getInstance().registerEvents(this);
			this.arrows = new HashSet<Arrow>();
			this.base = base;
			this.cart = cart;
			this.owner = owner;
			this.predEntity = entity -> {
				return entity != this.cart && entity != this.owner.getPlayer() && entity instanceof LivingEntity;
			};
			task = runTask(cycleActions).getTaskId();
		}
		
		private Runnable cycleActions = new BukkitRunnable() {

			@Override
			public void run() {
				if (target == null
						|| !target.isValid()
						|| !haveLineOfSight(target)) {
					target = findTarget();
				}
				if (target != null) {
					lookAt(target.getEyeLocation());
					fireVolley(target);
					task = getInstance().runTaskLater(cycleActions, PowerTime.toTicks(fireDelay)).getTaskId();
				}
				else {
					randomLook();
					task = getInstance().runTaskLater(cycleActions, 40L).getTaskId();
				}
			}
			
		};
		
		public void destroy() {
			if (this.base.getType() != Material.AIR) {
				this.base.getWorld().playEffect(this.base.getLocation(), Effect.STEP_SOUND, this.base.getType());
				this.base.setType(Material.AIR);
			}
			if (this.cart != null
					&& this.cart.isValid()) {
				PowerTools.fakeExplosion(this.cart.getLocation(), 2.0F);
				this.cart.remove();
			}
			if (task >= 0) {
				cancelTask(task);
			}
			turrets.get(owner).remove(this);
		}
		
		private LivingEntity findTarget() {
			List<Entity> nearby = cart.getNearbyEntities(range, range, range);
			Collections.shuffle(nearby);
			for (Entity entity : nearby) {
				if (entity instanceof LivingEntity
						&& entity != owner.getPlayer()) {
					LivingEntity lEntity = (LivingEntity) entity;
					if (haveLineOfSight(lEntity)) {
						if (!ignoreInvis
								|| !lEntity.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
							lookAt(lEntity.getEyeLocation());
							return lEntity;
						}
					}
				}
			}
			return null;
		}
		
		private void fireVolley(LivingEntity entity) {
			final Vector direction = PowerTools.getDirection(cart.getLocation(), entity.getEyeLocation());
			final float arrowPower = (float) (cart.getLocation().distance(target.getEyeLocation()) * 0.1F);
			for (int i = 0; i < arrowsPerVolley; i ++) {
				runTaskLater(new BukkitRunnable() {
					
					private Vector newDir = direction;
					private float newPower = arrowPower;
					
					@Override
					public void run() {
						if (entity.isValid()) {
							newDir = PowerTools.getDirection(cart.getLocation(), entity.getEyeLocation());
							newPower = (float) (cart.getLocation().distance(target.getEyeLocation()) * 0.1F);
						}
						Arrow arrow = cart.getWorld().spawnArrow(cart.getLocation().add(newDir).add(cartCenter), newDir, newPower, 0.0F);
						arrows.add(arrow);
					}
					
				}, 5L * i);
			}
		}
		
		private boolean haveLineOfSight(LivingEntity entity) {
			Vector direction = PowerTools.getDirection(cart.getLocation().add(cartCenter), entity.getLocation());
			return PowerTools.getTargetEntity(LivingEntity.class, cart.getLocation().clone().add(direction).add(cartCenter), direction, range, predEntity) != null;
		}
		
		private void lookAt(Location loc) {
			if (loc.getWorld() == cart.getWorld()) {
				double dx = -(loc.getX() - cart.getLocation().getX()),
						dy = loc.getY() - cart.getLocation().getY(),
						dz = -(loc.getZ() - cart.getLocation().getZ()),
						dh = Math.sqrt(dx * dx + dz * dz);
				float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI),
						pitch = (float) (-Math.atan(dy / dh) * 180.0 / Math.PI);
				PowerTools.setRotation(cart, yaw, pitch);
			}
		}
		
		private void randomLook() {
			double randX = random.nextDouble() * random.nextInt((int) range) - range / 2.0D,
					randY = random.nextDouble() * random.nextInt((int) range) - range / 2.0D,
					randZ = random.nextDouble() * random.nextInt((int) range) - range / 2.0D;
			lookAt(new Location(cart.getWorld(), cart.getLocation().getX() + randX, cart.getLocation().getY() + randY, cart.getLocation().getZ() + randZ));
		}
		
		@EventHandler (ignoreCancelled = true)
		private void onBreak(BlockBreakEvent event) {
			if (event.getBlock() == this.base) {
				this.destroy();
			}
		}
		
		@EventHandler (ignoreCancelled = true)
		private void onDamage(VehicleDamageEvent event) {
			if (event.getVehicle() == this.cart) {
				this.destroy();
			}
		}
		
		@EventHandler (ignoreCancelled = true)
		private void onArrowDamage(EntityDamageByEntityEvent event) {
			if (arrows.contains(event.getDamager())) {
				owner.causeDamage(getInstance(), event);
				owner.increaseStat(dmgByTurrets, (int) event.getDamage());
			}
		}
		
		@EventHandler
		private void onDeath(EntityDeathEvent event) {
			if (event.getEntity() == this.target) {
				this.target = null;
			}
		}
		
		@SuppressWarnings("deprecation")
		@EventHandler (ignoreCancelled = true)
		private void noPickup(PlayerPickupArrowEvent event) {
			if (arrows.contains(event.getArrow())) {
				event.setCancelled(true);
			}
		}
		
		@EventHandler (ignoreCancelled = true)
		private void noCollide(VehicleEntityCollisionEvent event) {
			if (event.getVehicle() == this.cart) {
				event.setCancelled(true);
			}
		}
		
		@EventHandler
		private void noMove(VehicleMoveEvent event) {
			if (event.getVehicle() == this.cart
					&& event.getFrom().distanceSquared(event.getTo()) > 0.0D) {
				this.cart.teleport(event.getFrom());
			}
		}
		
		@EventHandler (ignoreCancelled = true)
		private void noRide(VehicleEnterEvent event) {
			if (event.getVehicle() == this.cart) {
				event.setCancelled(true);
			}
		}
		
	}
	
}
