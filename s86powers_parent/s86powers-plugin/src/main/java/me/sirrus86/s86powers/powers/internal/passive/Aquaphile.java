package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Aquaphile", type = PowerType.PASSIVE, author = "sirrus86", concept = "bobby16may", icon=Material.NAUTILUS_SHELL,
	description = "Can breath[nv] and see better[/nv] underwater.[heal] Healed by water.[/heal][canDolphin] [act:item]ing while holding [item] while underwater transforms you into a dolphin that others can ride.[/canDolphin] Movement while underwater is much faster.")
public class Aquaphile extends Power {

	private Set<PowerUser> isDolphin, nvList;
	
	private boolean canDolphin, heal, nv;
	
	@Override
	protected void onEnable() {
		isDolphin = new HashSet<>();
		nvList = new HashSet<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (isDolphin.contains(user)) {
			setDolphin(user, false);
		}
		if (nvList.contains(user)) {
			if (nv) {
				user.removePotionEffect(PotionEffectType.NIGHT_VISION);
			}
			if (heal) {
				user.removePotionEffect(PotionEffectType.REGENERATION);
			}
			user.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
			nvList.remove(user);
		}
	}

	@Override
	protected void options() {
		canDolphin = option("enable-dolphin-form", true, "Whether dolphin form should be enabled.");
		heal = option("heal-underwater", true, "Whether users should be healed while underwater.");
		item = option("item", new ItemStack(Material.INK_SAC, 1), "Item used to change between human and dolphin form.");
		nv = option("night-vision", true, "Whether users should get night-vision underwater.");
		supplies(item);
	}
	
	private boolean isWater(Block block) {
		return block.getType() == Material.WATER
				|| (block.getBlockData() instanceof Waterlogged
						&& ((Waterlogged) block.getBlockData()).isWaterlogged());
	}
	
	private void setDolphin(PowerUser user, boolean dolphin) {
		if (dolphin) {
			if (isWater(user.getPlayer().getEyeLocation().getBlock())) {
				PowerTools.poof(user.getPlayer().getLocation());
				isDolphin.add(user);
				PowerTools.addDisguise(user.getPlayer(), EntityType.DOLPHIN);
				user.sendMessage(ChatColor.GREEN + "You transform into a dolphin.");
			}
			else {
				user.sendMessage(ChatColor.RED + "You can't become a dolphin on land.");
			}
		}
		else if (isDolphin.contains(user)) {
			if (user.isOnline()) {
				user.getPlayer().eject();
				PowerTools.poof(user.getPlayer().getLocation());
				PowerTools.removeDisguise(user.getPlayer());
				user.sendMessage(ChatColor.YELLOW + "You return to human form.");
			}
			isDolphin.remove(user);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void infiniteAir(EntityAirChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getPlayer().getEyeLocation().getBlock().getType() == Material.WATER) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			PowerUser user = getUser((Player) event.getRightClicked());
			if (isDolphin.contains(user)) {
				user.getPlayer().addPassenger(event.getPlayer());
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onMove(PlayerMoveEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)) {
			Block block = user.getPlayer().getEyeLocation().getBlock();
			if (isWater(block)) {
				if (!nvList.contains(user)) {
					if (nv) {
						user.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
					}
					if (heal) {
						user.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
					}
					user.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0));
					nvList.add(user);
				}
			}
			else {
				if (isDolphin.contains(user)) {
					setDolphin(user, false);
				}
				if (nvList.contains(user)) {
					if (nv) {
						user.removePotionEffect(PotionEffectType.NIGHT_VISION);
					}
					if (heal) {
						user.removePotionEffect(PotionEffectType.REGENERATION);
					}
					user.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
					nvList.remove(user);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this
				&& canDolphin) {
			PowerUser user = event.getUser();
			setDolphin(user, !isDolphin.contains(user));
		}
	}

}
