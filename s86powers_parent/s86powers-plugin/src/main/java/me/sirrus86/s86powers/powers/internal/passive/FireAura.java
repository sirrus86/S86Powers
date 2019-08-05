package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Fire Aura", type = PowerType.PASSIVE, author = "sirrus86", concept = "FyreCat", icon = Material.BLAZE_POWDER,
	description = "Regenerate hunger from fire and magma blocks, while regenerating health while in lava. Lose hunger in rain, and lose health in water.[goBlind] Vision while in lava is improved.[/goBlind]")
public class FireAura extends Power {
	
	private Set<PowerUser> isBlind;
	private Map<PowerUser, Integer> rainTask, sparkTask, waterTask;
	private Map<PowerUser, Long> sparkDur;
	
	private boolean goBlind;
	private int rainDmg;
	private double waterDmg;
	
	@Override
	protected void onEnable() {
		isBlind = new HashSet<>();
		rainTask = new HashMap<>();
		sparkDur = new HashMap<>();
		sparkTask = new HashMap<>();
		waterTask = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (isBlind.contains(user)) {
			user.removePotionEffect(PotionEffectType.BLINDNESS);
			isBlind.remove(user);
		}
		if (rainTask.containsKey(user)) {
			cancelTask(sparkTask.get(user));
			rainTask.remove(user);
		}
		if (sparkTask.containsKey(user)) {
			cancelTask(sparkTask.get(user));
			sparkTask.remove(user);
		}
		if (waterTask.containsKey(user)) {
			cancelTask(sparkTask.get(user));
			waterTask.remove(user);
		}
	}
	
	@Override
	protected void options() {
		goBlind = option("blind-in-lava", true, "Whether to apply blindness to player while in lava. This actually improves vision.");
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
						user.getPlayer().setFoodLevel(food - rainDmg);
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
					EntityDamageEvent event = new EntityDamageEvent(user.getPlayer(), DamageCause.DROWNING, waterDmg);
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
	
	private boolean inLava(PowerUser user) {
		Block block = user.getPlayer().getEyeLocation().getBlock();
		return block.getType() == Material.LAVA;
	}
	
	private boolean inRain(PowerUser user) {
		Location loc = user.getPlayer().getEyeLocation();
		return PowerTools.isOutside(loc)
				&& loc.getWorld().hasStorm();
	}
	
	private boolean inWater(PowerUser user) {
		Block[] blocks = new Block[] { user.getPlayer().getLocation().getBlock(), user.getPlayer().getEyeLocation().getBlock() };
		for (int i = 0; i < blocks.length; i ++) {
			if (blocks[i].getType() == Material.WATER
				|| (blocks[i].getBlockData() instanceof Waterlogged
						&& ((Waterlogged) blocks[i].getBlockData()).isWaterlogged())) {
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
					case FIRE: case FIRE_TICK: case HOT_FLOOR: {
						user.regenHunger((int) event.getDamage());
						event.setCancelled(true);
						break;
					}
					case LAVA: {
						user.heal(event.getDamage());
						sparkDur.put(user, System.currentTimeMillis() + PowerTime.toMillis(3, 0));
						if (!sparkTask.containsKey(user)) {
							sparkTask(user);
						}
						event.setCancelled(true);
						break;
					}
					default: break;
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void inWater(PlayerMoveEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)) {
			if (inLava(user)
					&& goBlind) {
				user.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0));
				isBlind.add(user);
			}
			else if (!inLava(user)
					&& isBlind.contains(user)) {
				user.removePotionEffect(PotionEffectType.BLINDNESS);
				isBlind.remove(user);
			}
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
