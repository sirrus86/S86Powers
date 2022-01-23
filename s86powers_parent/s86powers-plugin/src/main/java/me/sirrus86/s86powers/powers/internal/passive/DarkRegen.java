package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Dark Regen", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", icon = Material.END_CRYSTAL,
	description = "[regenAny]Regenerate [regenerate-health]health [/regenerate-health][regenBoth]and [/regenBoth][regenerate-hunger]hunger [/regenerate-hunger]while in dark areas."
			+ " [/regenAny][night-vision]Gain night vision in dark areas.[/night-vision]")
public final class DarkRegen extends Power {

	private Set<PowerUser> hasNV;
	private Map<PowerUser, Integer> regenTask;
	
	private PowerOption<Boolean> doNV, noRegen, regenFood, regenHP;
	private PowerOption<Integer> maxLvl, minLvl;
	@SuppressWarnings("unused")
	private boolean regenAny, regenBoth;
	
	@Override
	protected void onEnable() {
		hasNV = new HashSet<>();
		regenTask = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (hasNV.contains(user)) {
			user.removePotionEffect(PotionEffectType.NIGHT_VISION);
			hasNV.remove(user);
		}
		if (regenTask.containsKey(user)) {
			cancelTask(regenTask.get(user));
		}
	}

	@Override
	protected void config() {
		cooldown = option("minimum-cooldown", PowerTime.toMillis(200), "Minimum amount of time before user can regenerate in darkness.");
		maxLvl = option("maximum-light-level", 8, "Maximum light level (from 0-15, darkest to brightest) in which the power will work.");
		minLvl = option("minimum-light-level", 0, "Minimum light level (from 0-15, darkest to brightest) in which the power will work.");
		doNV = option("night-vision", false, "Whether night vision should be granted in dark areas.");
		noRegen = option("prevent-regen-in-light", true, "Whether to prevent users from regenerating health in higher light levels.");
		regenFood = option("regenerate-hunger", true, "Whether user will regenerate hunger in dark areas.");
		regenHP = option("regenerate-health", true, "Whether user will regenerate health in dark areas.");
		regenAny = getOption(regenFood) || getOption(regenHP);
		regenBoth = getOption(regenFood) && getOption(regenHP);
	}
	
	private Runnable doRegen(PowerUser user) {
		return new BukkitRunnable() {

			@Override
			public void run() {
				if (user.allowPower(getInstance())
						&& user.getPlayer().getEyeLocation().getBlock().getLightLevel() >= user.getOption(minLvl)
						&& user.getPlayer().getEyeLocation().getBlock().getLightLevel() <= user.getOption(maxLvl)) {
					if ((user.getPlayer().getHealth() < user.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() && user.getOption(regenHP))
							|| (user.getPlayer().getFoodLevel() < 20 && user.getOption(regenFood))) {
						if (user.getOption(regenHP)) {
							user.heal(1.0D);
						}
						if (user.getOption(regenFood)) {
							user.regenHunger(1);
						}
						PowerTools.playParticleEffect(user.getPlayer().getEyeLocation(), Particle.PORTAL, 10);
					}
					if (user.getOption(doNV)
							&& !hasNV.contains(user)) {
						user.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
						hasNV.add(user);
					}
					regenTask.put(user, getInstance().runTaskLater(doRegen(user), PowerTime.toTicks(user.getOption(cooldown))).getTaskId());
				}
				else if (hasNV.contains(user)) {
					user.removePotionEffect(PotionEffectType.NIGHT_VISION);
					hasNV.remove(user);
				}
			}
			
		};
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& user.getPlayer().getEyeLocation().getBlock().getLightLevel() >= user.getOption(minLvl)
				&& user.getPlayer().getEyeLocation().getBlock().getLightLevel() <= user.getOption(maxLvl)
				&& (!regenTask.containsKey(user) || !isTaskLive(regenTask.get(user)))) {
			regenTask.put(user, runTask(doRegen(user)).getTaskId());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onRegen(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getOption(noRegen)
					&& (user.getPlayer().getEyeLocation().getBlock().getLightLevel() < user.getOption(minLvl)
							|| user.getPlayer().getEyeLocation().getBlock().getLightLevel() > user.getOption(maxLvl))) {
				event.setCancelled(true);
			}
		}
	}

}
