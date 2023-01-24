package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.events.PowerUseOnEntityEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Bloodbend", type = PowerType.OFFENSE, author = "sirrus86", concept = "TheClownOfCrime", icon = Material.GHAST_TEAR, usesPackets = true,
	description = "[act:item]ing an entity while holding [item] allows you to momentarily control them, freezing and levitating them while you drain their blood."
			+ " This damages them while restoring your own hunger, then health. Does not work on undead. Can only be used against a given entity once every [victim-cooldown]. [cooldown] cooldown.")
public final class Bloodbend extends Power {

	private Map<LivingEntity, Long> vCooldown;
	private Set<BendTarget> targets;
	
	private PowerOption<Double> dmg, heal, range;
	private PowerOption<Long> dur, vCD;
	private PowerOption<Integer> freq;
	private String targetRecent;
	
	@Override
	protected void onEnable() {
		targets = new HashSet<>();
		vCooldown = new WeakHashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		for (BendTarget target : targets) {
			if (target.getUser() == user) {
				target.disable();
			}
		}
	}

	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(5, 0), "Amount of time before power can be used again.");
		dmg = option("damage", 1.0D, "Amount of damage done to targets per tick.");
		dur = option("duration", PowerTime.toMillis(1, 0), "Amount of time target is drained before being let go.");
		freq = option("drain-frequency", 5, "How often damage and healing occur while using power. Lower values are more frequent.");
		heal = option("healing", 1.0D, "Amount of healing done to user while power is used.");
		item = option("item", new ItemStack(Material.GHAST_TEAR), "Item required to use power.");
		range = option("range", 7.5D, "Maximum range which power can be used on targets.");
		vCD = option("victim-cooldown", PowerTime.toMillis(15, 0), "Amount of time before an entity can be targetted again.");
		targetRecent = locale("message.target-too-recent", ChatColor.RED + "Entity has been targetted too recently.");
		supplies(getRequiredItem());
	}
	
	private void doBend(PowerUser user, LivingEntity target) {
		if (target != null
				&& !(target instanceof Skeleton)
				&& !(target instanceof Zombie)) {
			if (!vCooldown.containsKey(target)
					|| vCooldown.get(target) <= System.currentTimeMillis()) {
				targets.add(new BendTarget(user, target));
				vCooldown.put(target, System.currentTimeMillis() + user.getOption(vCD));
				user.setCooldown(this, user.getOption(cooldown));
			}
			else {
				user.sendMessage(targetRecent);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onTarget(PowerUseEvent event) {
		if (event.getPower() == this) {
			if (event.getUser().getCooldown(this) <= 0) {
				doBend(event.getUser(), event.getUser().getTargetEntity(LivingEntity.class, event.getUser().getOption(range)));
			}
			else {
				event.getUser().showCooldown(this);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onTarget(PowerUseOnEntityEvent event) {
		if (event.getPower() == this
				&& event.getEntity() instanceof LivingEntity) {
			if (event.getUser().getCooldown(this) <= 0) {
				doBend(event.getUser(), (LivingEntity) event.getEntity());
			}
			else {
				event.getUser().showCooldown(this);
			}
		}
	}
	
	private class BendTarget {
		
		private final int taskID;
		private final PowerUser user;
		
		protected BendTarget(final PowerUser user, final LivingEntity target) {
			this.user = user;
			taskID = runTaskTimer(new Runnable() {
				long i = PowerTime.toTicks(user.getOption(dur));
				@Override
				public void run() {
					if (i > 0
							&& user.isOnline()
							&& user.getPlayer().getWorld() == target.getWorld()) {
						target.setVelocity(new Vector(0.0D, 0.1D, 0.0D));
						PowerTools.playRedstoneEffect(target.getEyeLocation(), Vector.getRandom(), 3, new DustOptions(Color.RED, 1.5F));
						Item red = target.getWorld().dropItem(target.getEyeLocation(), new ItemStack(Material.REDSTONE, 1));
						red.setPickupDelay(Integer.MAX_VALUE);
						PowerTools.fakeCollect(user.getPlayer(), red);
						red.remove();
						if (i % user.getOption(freq) == 0) {
							user.causeDamage(getInstance(), target, DamageCause.MAGIC, user.getOption(dmg));
							if (user.getPlayer().getFoodLevel() < 20) {
								user.regenHunger((int) Math.round(user.getOption(heal)));
							}
							else user.heal(user.getOption(heal));
						}
						i --;
					}
					else disable();
				}
			}, 0L, 1L).getTaskId();
		}
		
		public PowerUser getUser() {
			return user;
		}
		
		public void disable() {
			if (taskID > -1) {
				cancelTask(taskID);
			}
		}
		
	}
	
}
