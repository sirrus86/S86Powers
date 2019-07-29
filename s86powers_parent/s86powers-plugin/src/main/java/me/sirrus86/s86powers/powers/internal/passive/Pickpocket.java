package me.sirrus86.s86powers.powers.internal.passive;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
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

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Pickpocket", type = PowerType.PASSIVE, author = "sirrus86", concept = "grimm294", icon=Material.CHEST,
	description = "Sneaking makes you partially invisible. Right-clicking any monster or player while barehanded and sneaking will make you steal a random item from their inventory. Taking items from the target's inventory has a [brChance]% chance to break your stealth. Taking damage will also break stealth. Stealing has a [cooldown] cooldown.")
public class Pickpocket extends Power {

	private Set<LivingEntity> noDrop;
	private Set<PowerUser> stealth;
	private double brChance;
	private boolean dontDrop, repeat;
	private PowerStat thefts;
	
	@Override
	protected void onEnable() {
		noDrop = new HashSet<>();
		stealth = new HashSet<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (stealth.contains(user)) {
			PowerTools.poof(user.getPlayer().getLocation());
			PowerTools.removeGhost(user.getPlayer());
			stealth.remove(user);
		}
	}

	@Override
	protected void options() {
		brChance = option("stealth-break-chance", 20.0D, "Percentage chance that stealth will break when attempting to pickpocket.");
		cooldown = option("steal-cooldown", PowerTime.toMillis(5, 0), "Amount of time after stealing before you can pickpocket again.");
		dontDrop = option("no-drops-after-theft", true, "Whether non-player pickpocket victims should no longer be able to drop items on death.");
		repeat = option("steal-repeatedly", true, "Whether user can attempt to steal from same non-player target repeatedly.");
		thefts = stat("items-stolen", 50, "Items stolen", "Stealing items can no longer break stealth.");
	}
	
	private ItemStack getDrop(LivingEntity entity, HumanEntity killer) {
		if (entity instanceof HumanEntity) {
			PlayerInventory inv = ((HumanEntity) entity).getInventory();
			List<ItemStack> items = Lists.newArrayList(inv.getContents());
			int id = random.nextInt(items.size()) - 1;
			ItemStack drop = new ItemStack(items.get(id).getType(), 1);
			inv.removeItem(drop);
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
				user.sendMessage(ChatColor.RED + "Your stealth was broken.");
				PowerTools.poof(user.getPlayer().getLocation());
				PowerTools.removeGhost(user.getPlayer());
				stealth.remove(user);
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
				user.sendMessage(ChatColor.RED + "You've been detected.");
				PowerTools.poof(user.getPlayer().getLocation());
				PowerTools.removeGhost(user.getPlayer());
				stealth.remove(user);
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
						target.getWorld().dropItemNaturally(target.getLocation(), drop);
						if (random.nextDouble() < (brChance / 100.0D)
								&& !user.hasStatMaxed(thefts)) {
							if (target instanceof Monster) {
								if (target instanceof PigZombie) ((PigZombie) target).setAngry(true);
								((Monster) target).setTarget(user.getPlayer());
							}
							user.sendMessage(ChatColor.RED + "You were caught pickpocketing.");
							PowerTools.poof(user.getPlayer().getLocation());
							PowerTools.removeGhost(user.getPlayer());
							stealth.remove(user);
						}
						if (!(target instanceof Player)
								&& !repeat) {
							noDrop.add(target);
						}
						user.increaseStat(thefts, 1);
						user.setCooldown(this, cooldown);
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
				PowerTools.poof(user.getPlayer().getLocation());
				PowerTools.addGhost(user.getPlayer());
				stealth.add(user);
			}
			else {
				PowerTools.poof(user.getPlayer().getLocation());
				PowerTools.removeGhost(user.getPlayer());
				stealth.remove(user);
			}
		}
	}

}
