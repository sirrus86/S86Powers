package me.sirrus86.s86powers.powers.internal.defense;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Snow Miser", type = PowerType.DEFENSE, author = "sirrus86", concept = "bobby16may", icon = Material.SNOWBALL,
	description = "[snowOrIce]Thrown snowballs[freeze-water] freeze water into ice[/freeze-water][snowAndIce] and[/snowAndIce][add-snow-cover] produce snow on other surfaces[/add-snow-cover]."
			+ " [/snowOrIce]Snowballs thrown at enemies are slowed for [freeze-duration], increasing in effect with each application. Users of this power cannot be frozen by other users.")
public class SnowMiser extends Power {

	private Map<Snowball, Integer> snowballs;
	
	private PowerOption<Boolean> addSnow, freezeWater;
	private PowerOption<Integer> freezeCap;
	private PowerOption<Long> freezeDur;
	@SuppressWarnings("unused")
	private boolean snowAndIce, snowOrIce;

	@Override
	protected void onEnable() {
		snowballs = new WeakHashMap<>();
	}
	
	@Override
	protected void onDisable() {
		for (Snowball snowball : snowballs.keySet()) {
			if (snowballs.get(snowball) >= 0) {
				cancelTask(snowballs.get(snowball));
			}
		}
	}
	
	@Override
	protected void config() {
		addSnow = option("add-snow-cover", true, "Whether snowballs that hit solid surfaces should cover them with a snow layer.");
		freezeCap = option("freeze-amplifier-cap", 6, "Maximum amplifier of slowness effect applied to victims.");
		freezeDur = option("freeze-duration", PowerTime.toMillis(3, 0), "How long victims of snowballs are slowed.");
		freezeWater = option("freeze-water", true, "Whether water hit by snowballs should turn into ice.");
		snowAndIce = getOption(addSnow) && getOption(freezeWater);
		snowOrIce = getOption(addSnow) || getOption(freezeWater);
		supplies(new ItemStack(Material.SNOWBALL, 16));
	}
	
	private int trackSnowball(Snowball snowball) {
		return runTaskLater(new BukkitRunnable() {

			@Override
			public void run() {
				if (snowball.isValid()) {
					Block block = snowball.getLocation().getBlock();
					if (block.getType() == Material.WATER
							&& getOption(freezeWater)) {
						block.setType(Material.ICE);
						snowball.remove();
					}
					else {
						snowballs.put(snowball, trackSnowball(snowball));
					}
				}
			}
			
		}, 1L).getTaskId();
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onHit(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity
				&& !(event.getEntity() instanceof Snowman)
				&& event.getDamager() instanceof Snowball
				&& snowballs.containsKey(event.getDamager())) {
			LivingEntity target = (LivingEntity) event.getEntity();
			if (!(target instanceof Player)
					|| !getUser((Player)target).hasPower(this)) {
				int amplifier = 0;
				if (target.hasPotionEffect(PotionEffectType.SLOW)) {
					amplifier = Integer.min(getOption(freezeCap), target.getPotionEffect(PotionEffectType.SLOW).getAmplifier() + 1);
				}
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toTicks(getOption(freezeDur)), amplifier));
			}
		}
	}
	
	@EventHandler
	private void onHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Snowball
				&& snowballs.containsKey(event.getEntity())) {
			Block hitBlock = event.getEntity().getLocation().getBlock(),
					checkBlock = hitBlock.getRelative(BlockFace.DOWN);
			if (checkBlock.getType().isSolid()
					&& hitBlock.isEmpty()
					&& getOption(addSnow)) {
				hitBlock.setType(Material.SNOW);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onThrow(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof Snowball
				&& event.getEntity().getShooter() != null
				&& event.getEntity().getShooter() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity().getShooter());
			if (user.allowPower(this)) {
				snowballs.put((Snowball) event.getEntity(), user.getOption(freezeWater) ? trackSnowball((Snowball) event.getEntity()) : -1);
			}
		}
	}

}
