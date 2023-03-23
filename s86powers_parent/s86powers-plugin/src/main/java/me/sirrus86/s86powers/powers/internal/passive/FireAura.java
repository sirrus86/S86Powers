package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Fire Aura", type = PowerType.PASSIVE, author = "sirrus86", concept = "FyreCat", icon = Material.BLAZE_POWDER,
	description = "Regenerate hunger from fire and magma blocks, while regenerating health while in lava. Lose hunger in rain, and lose health in water.")
public final class FireAura extends Power {

	private Map<PowerUser, Integer> rainTask, sparkTask, waterTask;
	private Map<PowerUser, Long> sparkDur;

	private PowerOption<Integer> foodRegen;
	private PowerOption<Double> healthRegen;
	private PowerOption<Integer> rainDmg;
	private PowerOption<Double> waterDmg;
	
	@Override
	protected void onEnable() {
		rainTask = new HashMap<>();
		sparkDur = new HashMap<>();
		sparkTask = new HashMap<>();
		waterTask = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (rainTask.containsKey(user)) {
			cancelTask(rainTask.get(user));
			rainTask.remove(user);
		}
		if (sparkTask.containsKey(user)) {
			cancelTask(sparkTask.get(user));
			sparkTask.remove(user);
		}
		if (waterTask.containsKey(user)) {
			cancelTask(waterTask.get(user));
			waterTask.remove(user);
		}
	}
	
	@Override
	protected void config() {
		foodRegen = option("food-regen", 1, "Hunger regenerated while on fire or on magma blocks.");
		healthRegen = option("health-regen", 1.0D, "Health regenerated while in lava.");
		rainDmg = option("rain-damage", 1, "Hunger caused by being in the rain.");
		waterDmg = option("water-damage", 4.0D, "Damage caused by being in water.");
	}
	
	private void rainTask(PowerUser user) {
		if (inRain(user)
				&& !inWater(user)) {
			rainTask.put(user, runTaskLater(new BukkitRunnable() {

				@Override
				public void run() {
					user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
					int food = user.getPlayer().getFoodLevel();
					if (food > 0) {
						user.getPlayer().setFoodLevel(food - user.getOption(rainDmg));
					}
					rainTask(user);
				}
				
			}, 10L).getTaskId());
		}
		else {
			rainTask.remove(user);
		}
	}
	
	private void sparkTask(PowerUser user) {
		if (sparkDur.containsKey(user)
				&& System.currentTimeMillis() < sparkDur.get(user)) {
			sparkTask.put(user, runTaskLater(new BukkitRunnable() {

				@Override
				public void run() {
					PowerTools.playParticleEffect(user.getPlayer().getEyeLocation(), Particle.LAVA);
					sparkTask(user);
				}
				
			}, 5L).getTaskId());
		}
		else {
			sparkTask.remove(user);
		}
	}
	
	private void waterTask(PowerUser user) {
		if (inWater(user)) {
			waterTask.put(user, runTaskLater(new BukkitRunnable() {

				@Override
				public void run() {
					user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
					EntityDamageEvent event = new EntityDamageEvent(user.getPlayer(), DamageCause.DROWNING, user.getOption(waterDmg));
					callEvent(event);
					if (!event.isCancelled()) {
						user.getPlayer().damage(event.getDamage());
					}
					waterTask(user);
				}
				
			}, 10L).getTaskId());
		}
		else {
			waterTask.remove(user);
		}
	}
	
	private boolean inRain(PowerUser user) {
		Location loc = user.getPlayer().getEyeLocation();
		return PowerTools.isOutside(loc)
				&& loc.getWorld() != null
				&& loc.getWorld().hasStorm();
	}
	
	private boolean inWater(PowerUser user) {
		List<Block> blocks = Lists.newArrayList(user.getPlayer().getEyeLocation().getBlock());
		if (user.getPlayer().getVehicle() == null) {
			blocks.add(user.getPlayer().getLocation().getBlock());
		}
		for (Block block : blocks) {
			if (block.getType() == Material.WATER
					|| (block.getBlockData() instanceof Waterlogged
					&& ((Waterlogged) block.getBlockData()).isWaterlogged())) {
				return true;
			}
		}
		return false;
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				switch (event.getCause()) {
					case FIRE, FIRE_TICK, HOT_FLOOR -> {
						if (user.getOption(foodRegen) > 0) {
							user.regenHunger(user.getOption(foodRegen));
							event.setCancelled(true);
						}
					}
					case LAVA -> {
						if (user.getOption(healthRegen) > 0.0D) {
							user.heal(user.getOption(healthRegen));
							sparkDur.put(user, System.currentTimeMillis() + PowerTime.toMillis(3, 0));
							if (!sparkTask.containsKey(user)) {
								sparkTask(user);
							}
							event.setCancelled(true);
						}
					}
					default -> {
					}
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void inWater(PlayerMoveEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)) {
			if (inWater(user)
					&& !waterTask.containsKey(user)) {
				waterTask(user);
			}
			else if (inRain(user)
					&& !rainTask.containsKey(user)) {
				rainTask(user);
			}
		}
	}

}
