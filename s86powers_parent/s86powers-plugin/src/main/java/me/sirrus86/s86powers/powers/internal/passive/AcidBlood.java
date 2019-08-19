package me.sirrus86.s86powers.powers.internal.passive;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Acid Blood", type = PowerType.PASSIVE, author = "sirrus86", concept = "Gamekills99", icon=Material.EXPERIENCE_BOTTLE,
	description = "Absorb [absorb]% of incoming poison damage.[afflict] When struck by a melee attack, attacker becomes poisoned for [affDur].[/afflict]")
public final class AcidBlood extends Power {

	private double absorb;
	private long affDur;
	private int affInt;
	private boolean afflict;
	
	@Override
	protected void options() {
		absorb = option("absorb-percentage", 100.0D, "Percent of poison damage to be absorbed as health.");
		affDur = option("afflict-duration", PowerTime.toMillis(5, 0), "How long attackers should be afflicted with poison.");
		affInt = option("afflict-intensity", 1, "Intensity of poison afflicted to attackers.");
		afflict = option("afflict-attackers", true, "Whether to afflict attackers with poison.");
		cooldown = option("cooldown", PowerTime.toMillis(0), "Period of time before an attacker can be afflicted again.");
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				if (event.getCause() == DamageCause.POISON) {
					double abs = event.getDamage() * (absorb / 100.0D);
					user.heal(abs);
					event.setCancelled(true);
				}
				else if (event instanceof EntityDamageByEntityEvent) {
					if (((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity
							&& afflict && user.getCooldown(this) <= 0) {
						LivingEntity target = (LivingEntity) ((EntityDamageByEntityEvent) event).getDamager();
						target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) PowerTime.toTicks(affDur), affInt), true);
						user.setCooldown(this, cooldown);
					}
				}
			}
		}
	}

}
