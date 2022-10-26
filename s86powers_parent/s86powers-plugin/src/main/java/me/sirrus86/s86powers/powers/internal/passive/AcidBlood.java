package me.sirrus86.s86powers.powers.internal.passive;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Acid Blood", type = PowerType.PASSIVE, author = "sirrus86", concept = "Gamekills99", icon = Material.EXPERIENCE_BOTTLE,
	description = "Absorb [absorb.percentage]% of incoming poison damage.[afflict-attackers] When struck by a melee attack, attacker becomes poisoned for 5 seconds.[/afflict-attackers]")
public final class AcidBlood extends Power {

	private PowerOption<Double> absorb;
	private PowerOption<List<String>> absorbTypes;
	private PowerOption<Boolean> afflict;
//	private PowerOption<List<PotionEffect>> effects;
	
	@Override
	protected void config() {
		absorb = option("absorb.percentage", 100.0D, "Percent of poison damage to be absorbed as health.");
		absorbTypes = option("absorb.types", List.of("POISON"), "Damage types to absorb.");
		afflict = option("afflict-attackers", true, "Whether to afflict attackers with status effects.");
		cooldown = option("cooldown", PowerTime.toMillis(0), "Period of time before an attacker can be afflicted again.");
//		effects = option("afflict-effects", List.of(new PotionEffect(PotionEffectType.POISON, (int) PowerTime.toMillis(5, 0), 1, false, true, true)), "Effects to afflict on attackers.");
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				if (user.getOption(absorbTypes).contains(event.getCause().name())) {
					double abs = event.getDamage() * (user.getOption(absorb) / 100.0D);
					user.heal(abs);
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getOption(afflict)
					&& user.getCooldown(this) <= 0L
					&& event.getDamager() instanceof LivingEntity) {
				LivingEntity target = (LivingEntity) event.getDamager();
				target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) PowerTime.toTicks(5, 0), 1, false, true, true));
				user.setCooldown(this, user.getOption(cooldown));
			}
		}
	}

}
