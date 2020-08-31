package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Bulwark", type = PowerType.DEFENSE, author = "sirrus86", concept = "Neubulae", icon = Material.SHIELD,
	description = "Blocking with a shield [parryTime] before being hit will parry melee attacks, or deflect arrows back at the shooter. [doFatigue]Entities that are parried will also be afflicted with fatigue for [fatigueTime]. [/doFatigue][cooldown] cooldown.")
public final class Bulwark extends Power {
	
	private Map<PowerUser, Long> parryWindow;
	
	private boolean doFatigue;
	private int fatigueAmp;
	private double knockback;
	private long fatigueTime, parryTime;
	private String didDeflect, didParry, wasParried;
	
	@Override
	protected void onEnable() {
		parryWindow = new HashMap<>();
	}

	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(3, 0), "Amount of time after a successful parry before an attack can be parried again.");
		doFatigue = option("fatigue", true, "Whether to afflict the parried entity with fatigue.");
		fatigueAmp = option("fatigue-amplifier", 0, "Amplifier for fatigue effect.");
		fatigueTime = option("fatigue-duration", PowerTime.toMillis(5, 0), "Duration for fatigue effect.");
		item = new ItemStack(Material.SHIELD);
		knockback = option("knockback", 1.3D, "Velocity modifier for knockback when an attack is parried.");
		parryTime = option("parry-window", PowerTime.toMillis(1, 0), "Maximum amount of time after blocking to successfully parry an attack.");
		didDeflect = locale("message.you-deflected", ChatColor.GREEN + "You deflected [name]'s projectile!");
		didParry = locale("message.you-parried", ChatColor.GREEN + "You parried [name]'s attack!");
		wasParried = locale("message.you-were-parried", ChatColor.RED + "Your attack was parried by [name]!");
		supplies(new ItemStack(Material.SHIELD));
	}
	
	@EventHandler
	private void onBlock(PlayerInteractEvent event) {
		if (event.getAction().name().startsWith("RIGHT")) {
			PowerUser user = getUser(event.getPlayer());
			if (user.allowPower(this)
					&& event.getItem() != null
					&& event.getItem().getType() == Material.SHIELD) {
				parryWindow.put(user, System.currentTimeMillis() + parryTime);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getPlayer().isBlocking()
					&& parryWindow.containsKey(user)
					&& parryWindow.get(user) > System.currentTimeMillis()
					&& user.getCooldown(this) <= 0L) {
				if (event.getDamager() instanceof LivingEntity) {
					LivingEntity target = (LivingEntity) event.getDamager();
					Vector difference = target.getLocation().clone().subtract(user.getPlayer().getLocation()).toVector();
					target.setVelocity(difference.multiply(knockback));
					if (target instanceof Player) {
						getUser((Player) target).sendMessage(wasParried.replace("[name]", user.getName() + ChatColor.RED));
					}
					user.sendMessage(didParry.replace("[name]", PowerTools.getFriendlyName(target) + ChatColor.GREEN));
					if (doFatigue) {
						target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, (int) PowerTime.toTicks(fatigueTime), fatigueAmp));
					}
				}
				else if (event.getDamager() instanceof Arrow
						&& ((Arrow)event.getDamager()).getShooter() != null
						&& ((Arrow)event.getDamager()).getShooter() instanceof LivingEntity) {
					Arrow proj = (Arrow) event.getDamager();
					LivingEntity target = (LivingEntity) proj.getShooter();
					if (proj.getWorld() == target.getWorld()) {
						double speed = proj.getVelocity().length();
						Vector direction = target.getEyeLocation().toVector().subtract(new Vector(0.0D, 0.25D, 0.0D)).subtract(proj.getLocation().toVector()).normalize();
						Arrow newProj = proj.getWorld().spawnArrow(proj.getLocation(), direction, (float) speed * 0.9F, 0.0F);
						proj.remove();
						newProj.getWorld().playSound(newProj.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
						newProj.setShooter(target);
						user.sendMessage(didDeflect.replace("[name]", PowerTools.getFriendlyName(target) + ChatColor.GREEN));
						user.setCooldown(this, cooldown);
						event.setCancelled(true);
					}
				}
			}
		}
	}

}
