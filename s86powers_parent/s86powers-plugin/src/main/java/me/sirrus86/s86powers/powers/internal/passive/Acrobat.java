package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

@PowerManifest(name = "Acrobat", type = PowerType.PASSIVE, author = "sirrus86", concept = "Air_spike", icon = Material.FEATHER,
	description = "Jump much higher than other players and take no damage from falls. By [act:item]ing with [item], can adjust jump height.")
public final class Acrobat extends Power {

	private Map<PowerUser, Integer> effectPower;
	private PowerOption<List<String>> effectTypes, negateTypes;
	private PowerOption<Integer> maxEffect;
	private String effectPwr, effectOff;
	
	@Override
	protected void onEnable() {
		effectPower = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (effectPower.containsKey(user)
				&& effectPower.get(user) > -1) {
			effectPower.remove(user);
			user.removePotionEffect(PotionEffectType.JUMP);
		}
	}
	
	@Override
	protected void config() {
		effectTypes = option("effect-types", Lists.newArrayList("JUMP"), "Effects to be controlled with this power.");
		item = option("item", new ItemStack(Material.FEATHER), "Item used to change jump levels.");
		maxEffect = option("max-effect-level", 3, "Maximum effect level user can achieve.");
		negateTypes = option("negate-damage-types", Lists.newArrayList("FALL"), "Damage types to be negated by this power.");
		effectOff = locale("message.effect-off", ChatColor.YELLOW + "[type] power turned off.");
		effectPwr = locale("message.effect-power", ChatColor.YELLOW + "[type] power set to [amount].");
		supplies(getRequiredItem());
	}
	
	@EventHandler(ignoreCancelled = true)
	private void reduceFall(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getOption(negateTypes).contains(event.getCause().name())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (!effectPower.containsKey(user)) {
				effectPower.put(user, -1);
			}
			effectPower.put(user, effectPower.get(user) < user.getOption(maxEffect) ? effectPower.get(user) + 1 : -1);
			for (String effect : user.getOption(effectTypes)) {
				PotionEffectType type = PotionEffectType.getByName(effect);
				if (type != null) {
					if (effectPower.get(user) >= 0) {
						user.sendMessage(effectPwr.replace("[type]", PowerTools.getPotionEffectName(type))
								.replace("[amount]", Integer.toString(effectPower.get(user) + 1)));
						user.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, effectPower.get(user)));
					} else {
						user.removePotionEffect(type);
						user.sendMessage(effectOff.replace("[type]", PowerTools.getPotionEffectName(type)));
					}
				}
			}
		}
	}

}
