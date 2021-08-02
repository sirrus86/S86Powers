package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerIgniteEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Pyro Bow", type = PowerType.OFFENSE, author = "sirrus86", concept = "FyreCat", icon = Material.BOW,
	description = "All arrows fired ignite their targets[ignite-blocks], or the block they hit if they miss[/ignite-blocks]."
			+ "[disable-power-if-wet] Cannot use power if you've been in water or rain within the last [wet-cooldown].[/disable-power-if-wet]")
public final class PyroBow extends Power {

	private Map<Arrow, Boolean> arrows;
	private Map<Arrow, Integer> tasks;
	
	private PowerStat canExplode;
	private PowerOption<Double> explodeRad;
	private PowerOption<Boolean> disableIfWet, igniteBlocks;
	
	@Override
	protected void onEnable() {
		arrows = new HashMap<>();
		tasks = new HashMap<>();
	}

	@Override
	protected void config() {
		canExplode = stat("enemies-ignited", 50, "Enemies ignited", "Arrows now explode, covering the nearby area in flames.");
		cooldown = option("wet-cooldown", PowerTime.toMillis(5, 0), "How long after being wet before power can be used again.");
		disableIfWet = option("disable-power-if-wet", true, "Whether the power should fail if the user has been exposed to rain or water recently.");
		explodeRad = option("explosion-radius", 3.0D, "Radius of explosion when arrows are superpowered.");
		igniteBlocks = option("ignite-blocks", true, "Whether arrows should ignite blocks they hit.");
		supplies(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 64));
	}
	
	private Runnable doSmoke(Arrow arrow) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if (arrow.isValid()) {
					arrow.getWorld().spawnParticle(Particle.LAVA, arrow.getLocation(), 1);
				}
			}
		};
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onShoot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getCooldown(this) <= 0
					&& event.getProjectile() instanceof Arrow) {
				Arrow arrow = (Arrow) event.getProjectile();
				arrows.put(arrow, user.hasStatMaxed(canExplode));
				tasks.put(arrow, runTaskTimer(doSmoke(arrow), 0L, 1L).getTaskId());
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.getOption(disableIfWet)
				&& user.allowPower(this)) {
			if (event.getTo().getBlock().getType() == Material.WATER
					|| (PowerTools.isOutside(event.getTo())
							&& event.getTo().getWorld().hasStorm())) {
				user.setCooldown(this, user.getOption(cooldown));
			}
		}
	}
	
	@EventHandler
	private void onHit(ProjectileHitEvent event) {
		if (arrows.containsKey(event.getEntity())) {
			Arrow arrow = (Arrow) event.getEntity();
			PowerUser user = getUser((Player)arrow.getShooter());
			if (event.getHitBlock() != null
					&& user.getOption(igniteBlocks)) {
				callEvent(new PowerIgniteEvent(this, user, arrow.getLocation().getBlock(), null));
			}
			else if (event.getHitEntity() != null) {
				callEvent(new PowerIgniteEvent(this, user, event.getEntity()));
				user.increaseStat(canExplode, 1);
			}
			if (arrows.get(arrow)) {
				arrow.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, arrow.getLocation(), 1);
				for (Block block : PowerTools.getNearbyBlocks(arrow.getLocation(), user.getOption(explodeRad))) {
					callEvent(new PowerIgniteEvent(this, user, block, BlockFace.SELF));
				}
			}
			arrows.remove(arrow);
			cancelTask(tasks.get(arrow));
			tasks.remove(arrow);
		}
	}


}
