package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Dodge", type = PowerType.DEFENSE, author = "sirrus86", concept = "n33dy1", icon=Material.ENDER_EYE,
	description = "Always have a [base]% chance to dodge melee attacks. Chance to dodge increases as you fail to dodge attacks, up to a maximum of [max]%. Upon death your dodge chance resets back to [base]%.")
public class Dodge extends Power {

	private Map<PowerUser, Double> dodge;
	
	private double base, dpl, max;
	private int steps;
	
	@Override
	protected void onEnable() {
		dodge = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (dodge.containsKey(user)) {
			dodge.remove(user);
		}
	}

	@Override
	protected void options() {
		base = option("base-dodge-chance", 15.0D, "Minimum dodge chance while using this power.");
		max = option("maximum-dodge-chance", 75.0D, "Maximum dodge chance while using this power.");
		steps = option("increment-steps", 15, "Number of times dodge chance can increment.");
		dpl = (max - base) / steps;
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getDamager() instanceof LivingEntity) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				if (!dodge.containsKey(user)) {
					dodge.put(user, base);
				}
				double chance = random.nextDouble();
				if (chance < dodge.get(user) / 100.0D) {
					String name = event.getDamager().getCustomName() != null ? event.getDamager().getCustomName() : event.getDamager().getClass().getSimpleName().replace("Craft", "");
					if (event.getDamager() instanceof Player) {
						getUser(((Player)event.getDamager())).sendMessage(ChatColor.RED + user.getPlayer().getName() + " dodged your attack.");
						name = ((Player) event.getDamager()).getDisplayName() + ChatColor.GREEN;
					}
					user.sendMessage(ChatColor.GREEN + "You dodged " + name + "'s attack!");
					PowerTools.playParticleEffect(user.getPlayer().getLocation(), Particle.CLOUD);
					Vector difference = user.getPlayer().getLocation().clone().subtract(event.getDamager().getLocation()).toVector();
					user.getPlayer().setVelocity(difference);
					event.setCancelled(true);
				}
				else if (dodge.get(user) < max) {
					double newDodge = dodge.get(user) + dpl;
					dodge.put(user, newDodge <= max ? newDodge : max);
					user.sendMessage(ChatColor.GREEN + "Dodge chance increased to " + dodge.get(user) + "%.");
				}
			}
		}
	}
	
	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		PowerUser user = getUser(event.getEntity());
		if (user.allowPower(this)) {
			dodge.put(user, base);
			user.sendMessage(ChatColor.RED + "Dodge chance decreased to " + dodge.get(user) + "%.");
		}
	}

}
