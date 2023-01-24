package me.sirrus86.s86powers.powers.internal.defense;

import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Ender Soul", type = PowerType.DEFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.ENDER_PEARL,
	description = "[velOrRefund]Ender pearls [modify-pearl-velocity]are thrown at greater velocity[/modify-pearl-velocity][velAndRefund]"
			+ " and [/velAndRefund][refund-pearl]are immediately refunded after being thrown[/refund-pearl]."
			+ " [/velOrRefund][pearl-damage-immunity]Damage from ender pearls is negated.[/pearl-damage-immunity]")
public final class EnderSoul extends Power {

	private PowerOption<Boolean> doEffects, immunePearl, modVel, refundPearl;

	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	private boolean velAndRefund, velOrRefund;
	private PowerOption<Double> velMod;
	
	@Override
	protected void config() {
		doEffects = option("effects.enable", true, "Whether effects should be applied to user after teleporting.");
		immunePearl = option("pearl-damage-immunity", true, "Whether users should be immune to ender pearl damage.");
		modVel = option("modify-pearl-velocity", true, "Whether ender pearl velocity should be modified. If false, pearls are thrown normally.");
		refundPearl = option("refund-pearl", true, "Whether ender pearls should be refunded immediately after use.");
		velMod = option("velocity-modifier", 2.0D, "Modifier for pearl velocity. Higher value leads to faster, but less accurate throws.");
		velAndRefund = getOption(modVel) && getOption(refundPearl);
		velOrRefund = getOption(modVel) || getOption(refundPearl);
		supplies(new ItemStack(Material.ENDER_PEARL, 16));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof EnderPearl
				&& event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player)event.getEntity());
			if (user.allowPower(this)) {
				event.setCancelled(user.getOption(immunePearl));
			}
		}
	}
	
	@EventHandler
	private void onTeleport(PlayerTeleportEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.getOption(doEffects)
				&& user.allowPower(this)
				&& event.getCause() == TeleportCause.ENDER_PEARL) {
			user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PowerTime.toTicks(1, 0), 2, false, false, false));
		}
	}

	@EventHandler(ignoreCancelled = true)
	private void onLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof EnderPearl pearl) {
			if (pearl.getShooter() instanceof Player) {
				PowerUser user = getUser((Player)pearl.getShooter());
				if (user.allowPower(this)) {
					if (user.getOption(refundPearl)
							&& user.getCooldown(this) <= 0) {
						user.setCooldown(this, 50L);
						runTask(new BukkitRunnable() {
							@Override
							public void run() {
								user.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
							}
						});
					}
					if (user.getOption(modVel)) {
						Vector dir = user.getPlayer().getEyeLocation().getDirection().clone();
						pearl.setVelocity(dir.normalize().multiply(user.getOption(velMod)));
					}
				}
			}
		}
	}

}
