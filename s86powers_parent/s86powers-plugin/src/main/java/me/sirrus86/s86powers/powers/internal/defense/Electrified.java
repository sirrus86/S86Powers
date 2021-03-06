package me.sirrus86.s86powers.powers.internal.defense;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Electrified", type = PowerType.DEFENSE, author = "sirrus86", concept = "vashvhexx", icon = Material.GLOWSTONE_DUST,
	description = "Immune to lightning. Enemies who attack you with melee attacks while you are blocking are struck by lightning. [cooldown] cooldown.")
public final class Electrified extends Power {

	private PowerOption<Double> lDmg;
	
	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(0), "Period of time before power can be used again.");
		lDmg = option("lightning-damage", 5.0D, "Damage caused by lightning strikes to attackers.");
		supplies(new ItemStack(Material.SHIELD));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				if (event instanceof EntityDamageByEntityEvent
						&& user.getPlayer().isBlocking()) {
					if (user.getCooldown(this) <= 0) {
						Entity target = PowerTools.getEntitySource(((EntityDamageByEntityEvent) event).getDamager());
						target.getWorld().strikeLightningEffect(target.getLocation());
						user.causeDamage(this, (target instanceof Damageable ? (Damageable) target : null), DamageCause.LIGHTNING, user.getOption(lDmg));
						user.setCooldown(this, user.getOption(cooldown));
					}
				}
				if (event.getCause() == DamageCause.LIGHTNING) {
					event.setCancelled(true);
				}
			}
		}
	}

}
