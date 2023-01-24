package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Laser", type = PowerType.OFFENSE, author = "sirrus86", concept = "n33dy1", icon = Material.DISPENSER,
	description = "[act:item]ing while holding [item] shoots a laser in front of you. The laser can penetrate walls and will constantly damage enemies."
			+ "[use-consumable] Consumes [consumable] as fuel.[/use-consumable] [cooldown] cooldown.")
public final class Laser extends Power {

	private Map<PowerUser, Beam> lasers;
	
	private PowerOption<Double> damage, dist;
	private PowerOption<Integer> laserBlue, laserGreen, laserRed;
	private PowerOption<Boolean> useConsume;
	
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
	protected void config() {
		consumable = option("consumable", new ItemStack(Material.REDSTONE, 1), "Item used as fuel for the laser.");
		cooldown = option("cooldown", PowerTime.toMillis(10, 0), "Amount of time before power can be used again.");
		damage = option("damage", 5.0D, "Amount of damage done by laser beams.");
		dist = option("laser-distance", 10.0D, "Maximum distance a laser can travel.");
		item = option("item", new ItemStack(Material.DISPENSER), "Item used to start/stop firing the laser.");
		laserBlue = option("color.blue", 0, "Blue value for laser color. Values below 0 or above 255 may provide unexpected results.");
		laserGreen = option("color.green", 255, "Green value for laser color. Values below 0 or above 255 may provide unexpected results.");
		laserRed = option("color.red", 255, "Red value for laser color. Values below 0 or above 255 may provide unexpected results.");
		useConsume = option("use-consumable", true, "Whether consumable item should be required and consumed when laser is used.");
		supplies(getRequiredItem(), new ItemStack(getConsumable().getType(), getConsumable().getMaxStackSize()));
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
			Runnable doBeam = new BukkitRunnable() {
				@Override
				public void run() {
					if (!owner.getOption(useConsume)
							|| owner.getPlayer().getInventory().containsAtLeast(getConsumable(), 1)) {
						Location loc = owner.getPlayer().getEyeLocation().add(0.0D, -0.5D, 0.0D);
						if (loc.getWorld() != null) {
							loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
						}
						double getDist = owner.getOption(dist);
						List<Entity> entities = owner.getPlayer().getNearbyEntities(getDist, getDist, getDist);
						Vector dir = loc.getDirection();
						for (int i = 0; i < owner.getOption(dist) * 10; i++) {
							Vector newDir = dir.clone().multiply(i * 0.1D);
							loc.add(newDir);
							PowerTools.playRedstoneEffect(loc, new Vector(0, 0, 0), 1, new DustOptions(Color.fromRGB(owner.getOption(laserRed), owner.getOption(laserGreen), owner.getOption(laserBlue)), 1.0F));
							for (Entity entity : entities) {
								if (entity instanceof Damageable
										&& entity.getLocation().distanceSquared(loc) < 1.0D) {
									owner.causeDamage(getInstance(), (Damageable) entity, DamageCause.PROJECTILE, owner.getOption(damage));
								}
							}
						}
						if (owner.getOption(useConsume)) {
							owner.getPlayer().getInventory().removeItem(getConsumable());
						}
					} else {
						disable();
					}
				}
			};
			task = runTaskTimer(doBeam, 0L, 1L).getTaskId();
		}
		
		public void disable() {
			cancelTask(task);
			owner.setCooldown(getInstance(), owner.getOption(cooldown));
			lasers.remove(owner);
		}

	}

}
