package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashMap;
import java.util.Map;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@PowerManifest(name = "Acrobat", type = PowerType.PASSIVE, author = "sirrus86", concept = "Air_spike", icon=Material.FEATHER,
	description = "Jump much higher than other players[noDmg] and take no damage from falls[/noDmg]. By [act:item]ing with [item], can adjust jump height.")
public final class Acrobat extends Power {

	private Map<PowerUser, Integer> jumpPower;
	private int maxJump;
	private boolean noDmg;
	private String jumpPwr, jumpOff;
	
	@Override
	protected void onEnable() {
		jumpPower = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (jumpPower.containsKey(user)
				&& jumpPower.get(user) > -1) {
			jumpPower.remove(user);
			user.removePotionEffect(PotionEffectType.JUMP);
		}
	}
	
	@Override
	protected void config() {
		item = option("item", new ItemStack(Material.FEATHER), "Item used to change jump levels.");
		maxJump = option("max-jump-level", 3, "Maximum jump level user can achieve.");
		noDmg = option("negate-fall-damage", true, "Whether fall damage should be ignored.");
		jumpOff = locale("message.jump-off", ChatColor.YELLOW + "Jump power turned off.");
		jumpPwr = locale("message.jump-power", ChatColor.YELLOW + "Jump power set to [amount].");
		supplies(item);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void reduceFall(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& event.getCause() == DamageCause.FALL
					&& noDmg) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (!jumpPower.containsKey(user)) {
				jumpPower.put(user, -1);
			}
			jumpPower.put(user, jumpPower.get(user) < maxJump ? jumpPower.get(user) + 1 : -1);
			if (jumpPower.get(user) >= 0) {
				user.sendMessage(jumpPwr.replace("[amount]", Integer.toString(jumpPower.get(user) + 1)));
				user.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, jumpPower.get(user)));
			}
			else {
				user.removePotionEffect(PotionEffectType.JUMP);
				user.sendMessage(jumpOff);
			}
		}
	}

}
