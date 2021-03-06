package me.sirrus86.s86powers.powers.internal.utility;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Neutralizer Grenade", type = PowerType.UTILITY, author = "sirrus86", concept = "sirrus86", icon = Material.LAPIS_LAZULI, usesPackets = true,
	description = "[act:item]ing while holding [item] turns it into a neutralizer grenade and arms it for [cooldown]."
			+ " While armed, throwing the grenade creates a neutral field, disabling the powers of players within [neutralize-range] meters of the impact for [neutralize-duration].")
public final class NeutralizerGrenade extends Power {
	
	private Map<Snowball, Integer> gList;
	
	private PowerOption<Long> nDur;
	private PowerOption<Double> range;
	
	@Override
	protected void onEnable() {
		gList = new WeakHashMap<>();
	}

	@Override
	protected void config() {
		item = option("item", new ItemStack(Material.LAPIS_LAZULI, 1), "Item used to throw neutralizer grenades.");
		nDur = option("neutralize-duration", PowerTime.toMillis(30, 0), "Amount of time players are neutralized when hit by a grenade.");
		range = option("neutralize-range", 5.0D, "Distance from impact point where grenade may neutralize players.");
	}
	
	private Runnable sparkle(final Snowball grenade) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				PowerTools.playParticleEffect(grenade.getLocation(), Particle.CRIT_MAGIC, 3);
			}
		};
	}
	
	@EventHandler
	private void onArm(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			event.consumeItem();
			Snowball grenade = user.getPlayer().launchProjectile(Snowball.class);
			gList.put(grenade, runTaskTimer(sparkle(grenade), 0L, 1L).getTaskId());
			PowerTools.addDisguise(grenade, getRequiredItem());
		}
	}
	
	@EventHandler
	private void onHit(ProjectileHitEvent event) {
		if (gList.containsKey(event.getEntity())) {
			cancelTask(gList.get(event.getEntity()));
			PowerTools.fakeExplosion(event.getEntity().getLocation(), 1);
			for (Player player : PowerTools.getNearbyEntities(Player.class, event.getEntity().getLocation(), getOption(range))) {
				PowerUser user = getUser(player);
				user.setNeutralizedByPower(this, getOption(nDur));
			}
		}
	}

}
