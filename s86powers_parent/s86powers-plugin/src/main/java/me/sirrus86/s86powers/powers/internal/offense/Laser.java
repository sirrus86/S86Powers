package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
//import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Laser", type = PowerType.OFFENSE, author = "sirrus86", concept = "n33dy1", version=MCVersion.v1_14, icon=Material.DISPENSER,
	description = "[act:item]ing while holding [item] shoots a laser in front of you. The laser can penetrate walls and will constantly damage enemies.[useConsume] Consumes [consumable] as fuel.[/useConsume] [cooldown] cooldown.")
public class Laser extends Power {

	private Map<PowerUser, Beam> lasers;
	
	private double damage, dist;
	private int laserBlue, laserGreen, laserRed;
//	private PowerStat laserDmg;
	private boolean useConsume;
	
	@Override
	protected void onEnable() {
		lasers = new HashMap<>();
	}

	@Override
	protected void onDisable(PowerUser user) {
		if (lasers.containsKey(user)) {
			Beam laser = lasers.get(user);
			laser.disable();
		}
	}

	@Override
	protected void options() {
		consumable = option("consumable", new ItemStack(Material.REDSTONE, 1), "Item used as fuel for the laser.");
		cooldown = option("cooldown", PowerTime.toMillis(10, 0), "Amount of time before power can be used again.");
		damage = option("damage", 5.0D, "Amount of damage done by laser beams.");
		dist = option("laser-distance", 10.0D, "Maximum distance a laser can travel.");
		item = option("item", new ItemStack(Material.DISPENSER), "Item used to start/stop firing the laser.");
		laserBlue = option("color.blue", 0, "Blue value for laser color. Values below 0 or above 255 may provide unexpected results.");
		laserGreen = option("color.green", 255, "Green value for laser color. Values below 0 or above 255 may provide unexpected results.");
		laserRed = option("color.red", 255, "Red value for laser color. Values below 0 or above 255 may provide unexpected results.");
//		laserDmg = stat("laser-damage", 150, "Damage done by lasers", "Lasers will now split when shot through glass."); // TODO
		useConsume = option("use-consumable", true, "Whether consumable item should be required and consumed when laser is used.");
		supplies(item, new ItemStack(consumable.getType(), consumable.getMaxStackSize()));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.allowPower(this)) {
				if (!lasers.containsKey(user)) {
					if (user.getCooldown(this) <= 0) {
						Beam laser = new Beam(user);
						lasers.put(user, laser);
					}
					else {
						user.showCooldown(this);
					}
				}
				else {
					Beam laser = lasers.get(user);
					laser.disable();
				}
			}
		}
	}
	
	private class Beam {
		
		private final PowerUser owner;
		private final int task;
		
		public Beam(PowerUser owner) {
			this.owner = owner;
			task = runTaskTimer(doBeam, 0L, 1L).getTaskId();
		}
		
		public void disable() {
			cancelTask(task);
			owner.setCooldown(getInstance(), cooldown);
			lasers.remove(owner);
		}
		
		private Runnable doBeam = new BukkitRunnable() {
			@Override
			public void run() {
				if (!useConsume
						|| owner.getPlayer().getInventory().containsAtLeast(consumable, 1)) {
					Location loc = owner.getPlayer().getEyeLocation().add(0.0D, -0.5D, 0.0D);
					loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
					List<Entity> entities = owner.getPlayer().getNearbyEntities(dist, dist, dist);
					Vector dir = loc.getDirection();
					for (int i = 0; i < dist * 10; i ++) {
						Vector newDir = dir.clone().multiply(i * 0.1D);
						loc.add(newDir);
						PowerTools.playRedstoneEffect(loc, new Vector(0, 0, 0), 1, new DustOptions(Color.fromRGB(laserRed, laserGreen, laserBlue), 1.0F));
						for (Entity entity : entities) {
							if (entity instanceof Damageable
									&& BoundingBox.of(loc, 0.25D, 0.25D, 0.25D).overlaps(entity.getBoundingBox())) {
								owner.causeDamage(getInstance(), (Damageable) entity, DamageCause.PROJECTILE, damage);
							}
						}
					}
					if (useConsume) {
						owner.getPlayer().getInventory().removeItem(consumable);
					}
				}
				else {
					disable();
				}
			}
		};
		
	}

}
