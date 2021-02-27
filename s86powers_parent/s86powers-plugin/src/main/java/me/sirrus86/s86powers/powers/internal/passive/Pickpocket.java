package me.sirrus86.s86powers.powers.internal.passive;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.Lootable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Pickpocket", type = PowerType.PASSIVE, author = "sirrus86", concept = "grimm294", icon = Material.CHEST, usesPackets = true,
	description = "Sneaking makes you partially invisible. Right-clicking any monster or player while barehanded and sneaking will make you steal a random item from their inventory."
			+ " Taking items from the target's inventory has a [stealth-break-chance]% chance to break your stealth. Taking damage will also break stealth. Stealing has a [cooldown] cooldown.")
public final class Pickpocket extends Power {

	private Set<LivingEntity> noDrop;
	private Set<PowerUser> stealth;
	private double brChance;
	private boolean dontDrop, goToInv, lookAtThief, repeat;
	private PowerStat thefts;
	private String caught, detected, failedToSteal, stealthBroken, triedToSteal;
	
	@Override
	protected void onEnable() {
		noDrop = new HashSet<>();
		stealth = new HashSet<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (stealth.contains(user)) {
			removeStealth(user);
		}
	}

	@Override
	protected void config() {
		brChance = option("stealth-break-chance", 20.0D, "Percentage chance that stealth will break when attempting to pickpocket.");
		cooldown = option("steal-cooldown", PowerTime.toMillis(5, 0), "Amount of time after stealing before you can pickpocket again.");
		dontDrop = option("no-drops-after-theft", true, "Whether non-player pickpocket victims should no longer be able to drop items on death.");
		goToInv = option("steal-to-inventory", true, "Whether items stolen should go directly to inventory.");
		lookAtThief = option("look-at-thief", true, "Whether players should look at thieves when they are detected.");
		repeat = option("steal-repeatedly", true, "Whether user can attempt to steal from same non-player target repeatedly.");
		thefts = stat("items-stolen", 50, "Items stolen", "Stealing items can no longer break stealth.");
		caught = locale("message.caught-stealing", ChatColor.RED + "You were caught pickpocketing.");
		detected = locale("message.detected", ChatColor.RED + "You've been detected.");
		failedToSteal = locale("message.failed-to-steal", ChatColor.RED + "Failed to steal anything.");
		stealthBroken = locale("message.stealth-broken", ChatColor.RED + "Your stealth was broken.");
		triedToSteal = locale("message.tried-to-steal", ChatColor.RED + "[name] tried to steal from you!");
	}
	
	private void removeStealth(PowerUser user) {
		user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
		PowerTools.removeGhost(user.getPlayer());
		stealth.remove(user);
	}
	
	private ItemStack getDrop(LivingEntity entity, HumanEntity killer) {
		if (entity instanceof HumanEntity) {
			ItemStack drop = null;
			PlayerInventory inv = ((HumanEntity) entity).getInventory();
			for (int i = 0; i < inv.getContents().length; i ++) {
				int j = random.nextInt(inv.getContents().length);
				if (inv.getContents()[j] != null) {
					drop = inv.getContents()[j].clone();
					inv.getContents()[j].setAmount(inv.getContents()[j].getAmount() - 1);
					break;
				}
			}
			if (drop != null) {
				drop.setAmount(1);
			}
			return drop;
		}
		else if (entity instanceof Lootable) {
			Collection<ItemStack> loot = ((Lootable) entity).getLootTable().populateLoot(random,
					new LootContext.Builder(entity.getLocation()).killer(killer).lootedEntity(entity).build());
			if (loot.size() > 0) {
				int chance = random.nextInt(loot.size());
				return loot.stream().skip(chance).findFirst().get();
			}
		}
		return null;
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& stealth.contains(user)) {
				user.sendMessage(stealthBroken);
				removeStealth(user);
			}
		}
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		if (noDrop.contains(event.getEntity())
				&& dontDrop) {
			event.getDrops().clear();
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onTarget(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() instanceof Player) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)
					&& stealth.contains(user)) {
				user.sendMessage(detected);
				removeStealth(user);
			}
		}
	}
	
	@EventHandler
	private void onSteal(PlayerInteractEntityEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& stealth.contains(user)
				&& PowerTools.isGhost(user.getPlayer())
				&& (event.getRightClicked() instanceof Monster
						|| event.getRightClicked() instanceof Player)
				&& !(event.getRightClicked() instanceof Wither)) {
			if (user.getCooldown(this) <= 0) {
				LivingEntity target = (LivingEntity) event.getRightClicked();
				if (!noDrop.contains(target)) {
					ItemStack drop = getDrop(target, user.getPlayer());
					if (drop != null) {
						if (goToInv) {
							user.addItems(drop);
						}
						else {
							target.getWorld().dropItemNaturally(target.getLocation(), drop);
						}
						if (random.nextDouble() < (brChance / 100.0D)
								&& !user.hasStatMaxed(thefts)) {
							if (target instanceof Monster) {
								if (target instanceof PigZombie) {
									((PigZombie) target).setAngry(true);
								}
								((Monster) target).setTarget(user.getPlayer());
							}
							else if (target instanceof Player) {
								PowerUser victim = getUser((Player) target);
								victim.sendMessage(triedToSteal.replace("[name]", user.getName()));
								if (lookAtThief) {
									PowerTools.setLook((Player) target, user.getPlayer().getEyeLocation());
								}
							}
							user.sendMessage(caught);
							removeStealth(user);
						}
						if (!(target instanceof Player)
								&& !repeat) {
							noDrop.add(target);
						}
						user.increaseStat(thefts, 1);
						user.setCooldown(this, cooldown);
					}
					else {
						user.sendMessage(failedToSteal);
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onSneak(PlayerToggleSneakEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)) {
			if (event.isSneaking()) {
				user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
				PowerTools.addGhost(user.getPlayer());
				stealth.add(user);
			}
			else {
				removeStealth(user);
			}
		}
	}

}
