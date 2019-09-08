package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Brawler", type = PowerType.OFFENSE, author = "sirrus86", concept = "diamondmario", icon = Material.DAMAGED_ANVIL,
	description = "All barehanded attacks deal [dmgIncr]% normal damage. Barehanded attacks while crouching will slow the target for [slowDur], while attacks within [uppercutBuffer] of rising will uppercut them into the air. Falling enemies above you can be juggled for additional damage.")
public final class Brawler extends Power {

	private Map<PowerUser, Long> canUppercut;
	
	private double dmgIncr, juggleVert, offHandDef, uppercutVert;
	private int slowAmp;
	private long slowDur, uppercutBuffer;
	private PowerStat totalDmg;
	
	@Override
	protected void onEnable() {
		canUppercut = new HashMap<>();
	}
	
	@Override
	protected void config() {
		dmgIncr = option("damage-increase", 400.0D, "Percentage increase for damage done while barehanded.");
		juggleVert = option("juggle-vertical-modifier", 0.5D, "Velocity modifier to entities hit while falling.");
		offHandDef = option("offhand-defense", 75.0D, "Percentage decrease to incoming melee damage while off-hand has no item equipped.");
		slowAmp = option("sweep-slow-amplifier", 2, "Amplifier of slow effect applied to those hit by low sweep attacks.");
		slowDur = option("sweep-slow-duration", PowerTime.toMillis(3, 0), "Amount of time to slow those hit by low sweep attacks.");
		totalDmg = stat("total-barehanded-damage", 100, "Damage dealt while barehanded", "Keeping you off-hand empty allows you to block [offHandDef]% of melee damage from enemies.");
		uppercutBuffer = option("uppercut-buffer", PowerTime.toMillis(500), "Amount of time after standing from crouch to perform an uppercut.");
		uppercutVert = option("uppercut-vertical-modifier", 0.75D, "Velocity modifier to entities hit by an uppercut.");
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onDmg(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			PowerUser user = getUser((Player) event.getDamager());
			if (user.allowPower(this)
					&& user.getEquipment(EquipmentSlot.HAND).getType() == Material.AIR) {
				event.setDamage(event.getDamage() * (dmgIncr / 100.0D));
				if (user.getPlayer().isSneaking()
						&& event.getEntity() instanceof LivingEntity) {
					((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toTicks(slowDur), slowAmp, false, false, false), true);
				}
				else if (canUppercut.containsKey(user)
						&& System.currentTimeMillis() < canUppercut.get(user)) {
					runTask(new BukkitRunnable() {
						@Override
						public void run() {
							event.getEntity().setVelocity(new Vector(event.getEntity().getVelocity().getX(), uppercutVert, event.getEntity().getVelocity().getZ()));
						}
					});
				}
				else if (event.getEntity().getFallDistance() > 0.0F
						&& event.getEntity().getLocation().getY() > user.getPlayer().getLocation().getY()) {
					runTask(new BukkitRunnable() {
						@Override
						public void run() {
							event.getEntity().setVelocity(new Vector(event.getEntity().getVelocity().getX(), juggleVert, event.getEntity().getVelocity().getZ()));
						}
					});
				}
				user.increaseStat(totalDmg, (int) event.getDamage());
			}
		}
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getEquipment(EquipmentSlot.OFF_HAND).getType() == Material.AIR
					&& event.getEntity() instanceof LivingEntity
					&& user.hasStatMaxed(totalDmg)) {
				event.setDamage(event.getDamage() * (offHandDef / 100.0D));
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onSneak(PlayerToggleSneakEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& !event.isSneaking()) {
			canUppercut.put(user, System.currentTimeMillis() + uppercutBuffer);
		}
	}

}
