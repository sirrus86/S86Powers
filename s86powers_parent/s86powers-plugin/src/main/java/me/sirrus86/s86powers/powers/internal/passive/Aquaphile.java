package me.sirrus86.s86powers.powers.internal.passive;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Aquaphile", type = PowerType.PASSIVE, author = "sirrus86", concept = "bobby16may", icon=Material.NAUTILUS_SHELL, usesPackets = true,
	description = "Can breath[night-vision] and see better[/night-vision] underwater.[heal-underwater] Healed by water.[/heal-underwater]"
			+ " Movement while underwater is much faster.[enable-dolphin-form] Sprinting while underwater transforms you into a dolphin.[/enable-dolphin-form]")
public final class Aquaphile extends Power {

	private final Set<Material> waterMats = EnumSet.of(Material.KELP_PLANT, Material.SEAGRASS, Material.TALL_SEAGRASS, Material.WATER);
	
	private Set<PowerUser> isDolphin, nvList;
	
	private boolean canDolphin, heal, nv;
	private String dolphinOnLand, turnToDolphin, turnToHuman;
	
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
			user.removePotionEffect(PotionEffectType.WATER_BREATHING);
			nvList.remove(user);
		}
	}

	@Override
	protected void config() {
		canDolphin = option("enable-dolphin-form", true, "Whether dolphin form should be enabled.");
		heal = option("heal-underwater", true, "Whether users should be healed while underwater.");
		nv = option("night-vision", true, "Whether users should get night-vision underwater.");
		dolphinOnLand = locale("message.dolphin-on-land", ChatColor.RED + "You can't become a dolphin on land.");
		turnToDolphin = locale("message.turn-to-dolphin", ChatColor.GREEN + "You transform into a dolphin.");
		turnToHuman = locale("message.turn-to-human", ChatColor.YELLOW + "You return to human form.");
		supplies(item);
	}
	
	private boolean isWater(Block block) {
		return waterMats.contains(block.getType())
				|| (block.getBlockData() instanceof Waterlogged
						&& ((Waterlogged) block.getBlockData()).isWaterlogged());
	}
	
	private void setDolphin(PowerUser user, boolean dolphin) {
		if (dolphin) {
			if (isWater(user.getPlayer().getEyeLocation().getBlock())) {
				user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
				isDolphin.add(user);
				PowerTools.addDisguise(user.getPlayer(), EntityType.DOLPHIN);
				user.sendMessage(turnToDolphin);
			}
			else {
				user.sendMessage(dolphinOnLand);
			}
		}
		else if (isDolphin.contains(user)) {
			if (user.isOnline()) {
				user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
				PowerTools.removeDisguise(user.getPlayer());
				user.sendMessage(turnToHuman);
			}
			isDolphin.remove(user);
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
					user.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0));
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
					user.removePotionEffect(PotionEffectType.WATER_BREATHING);
					nvList.remove(user);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onSwim(EntityToggleSwimEvent event) {
		if (event.getEntity() instanceof Player
				&& canDolphin) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)) {
				setDolphin(user, event.isSwimming());
			}
		}
	}

}
