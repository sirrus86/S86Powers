package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Soul Siphon", type = PowerType.OFFENSE, author = "sirrus86", concept = "sirrus86", icon = Material.END_CRYSTAL, usesPackets = true,
	description = "[act:item]ing the top of a block while holding [item] will place a Soul Siphon. Soul Siphons will drain health from nearby entities before returning the drained health to you. [cooldown] cooldown.")
public final class SoulSiphon extends Power {

	private Map<PowerUser, Siphon> siphons;
	
	private PowerOption<Double> drainRange, maxLife;
	
	@Override
	protected void onEnable() {
		siphons = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (siphons.containsKey(user)) {
			siphons.get(user).destroy();
		}
	}
	
	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(30, 0), "Amount of time before power can be used again.");
		drainRange = option("drain-range", 15.0D, "Maximum range from which siphons can drain or feed health.");
		item = option("item", new ItemStack(Material.END_CRYSTAL), "Item used to create soul siphons.");
		maxLife = option("maximum-life", 10.0D, "Maximum total amount of life siphons can drain from all targets.");
		supplies(new ItemStack(getRequiredItem().getType(), 1));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this
				&& event.getClickedBlock() != null
				&& event.getBlockFace() == BlockFace.UP
				&& event.getClickedBlock().getRelative(BlockFace.UP).isEmpty()) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0L) {
				if (siphons.containsKey(user)) {
					siphons.get(user).destroy();
				}
				Block block = event.getClickedBlock().getRelative(BlockFace.UP);
				EnderCrystal crystal = block.getWorld().spawn(block.getLocation(), EnderCrystal.class);
				Siphon siphon = new Siphon(user, crystal);
				siphons.put(user, siphon);
				user.setCooldown(this, user.getOption(cooldown));
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	private class Siphon implements Listener {
		
		private final EnderCrystal crystal;
		private int drainCD = 0, drainTask = -1;
		private double life = 0.0D;
		private final PowerUser owner;
		private LivingEntity target = null;
		
		public Siphon(PowerUser owner, EnderCrystal crystal) {
			registerEvents(this);
			this.crystal = crystal;
			this.owner = owner;
			updateHealth();
			drainTask = runTaskTimer(doDrain, 0L, 0L).getTaskId();
		}
		
		private Runnable doDrain = new BukkitRunnable() {

			@Override
			public void run() {
				if (drainCD > 0) {
					drainCD --;
				}
				updateHealth();
				if (owner.getPlayer().getWorld().equals(crystal.getWorld())
						&& owner.getPlayer().getLocation().distanceSquared(crystal.getLocation()) < owner.getOption(drainRange) * owner.getOption(drainRange)
						&& owner.getPlayer().getHealth() < owner.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
						&& !owner.getPlayer().isDead()
						&& life > 0.0D) {
					crystal.setBeamTarget(owner.getPlayer().getLocation().clone().subtract(0.0D, 1.0D, 0.0D));
					if (drainCD <= 0) {
						life --;
						owner.heal(1.0D);
						drainCD = 10;
					}
				}
				else if (target != null
						&& !target.isDead()
						&& target.getLocation().distanceSquared(crystal.getLocation()) < owner.getOption(drainRange) * owner.getOption(drainRange)
						&& life < owner.getOption(maxLife)) {
					crystal.setBeamTarget(target.getLocation().clone().subtract(0.0D, 1.0D, 0.0D));
					if (drainCD <= 0) {
						life ++;
						owner.causeDamage(getInstance(), target, DamageCause.MAGIC, 1.0D);
						drainCD = 10;
					}
				}
				else {
					crystal.setBeamTarget(null);
					target = PowerTools.getRandomEntity(crystal, owner.getOption(drainRange), owner.getPlayer());
				}
			}
			
		};
		
		public void destroy() {
			cancelTask(drainTask);
			PowerTools.fakeExplosion(crystal.getLocation(), 3.0F);
			crystal.remove();
			unregisterEvents(this);
			siphons.remove(owner);
		}
		
		private void updateHealth() {
			String tmp = "";
			double i = life / 2.0D;
			double j = owner.getOption(maxLife) / 2.0D;
			for (int k = 0; k < j; k ++) {
				if (i > 0.0D) {
					tmp = tmp + ChatColor.RED;
					i --;
				}
				else {
					tmp = tmp + ChatColor.GRAY;
				}
				tmp = tmp + "\u2665";
			}
			crystal.setCustomName(tmp);
			crystal.setCustomNameVisible(true);
		}
		
		@EventHandler(ignoreCancelled = true)
		private void onDamage(EntityDamageEvent event) {
			if (event.getEntity() == crystal) {
				if (life >= event.getDamage()) {
					life -= event.getDamage();
					updateHealth();
					event.setCancelled(true);
				}
			}
		}
		
		@EventHandler(ignoreCancelled = true)
		private void onExplode(EntityExplodeEvent event) {
			if (event.getEntity() == crystal) {
				if (ConfigOption.Powers.PREVENT_GRIEFING) {
					event.blockList().clear();
				}
				destroy();
			}
		}
		
	}

}
