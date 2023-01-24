package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;

@SuppressWarnings("unused")
@PowerManifest(name = "Haste", type = PowerType.PASSIVE, author = "sirrus86", concept = "JJoiler", icon = Material.ELYTRA,
	description = "Perform hand-related tasks (mining, digging, attacking, etc) much faster than other players.")
public final class Haste extends Power {

	private Set<PowerUser> hasEffects;
	
	@Override
	protected void onEnable() {
		hasEffects = new HashSet<>();
	}
	
	@Override
	protected void onEnable(PowerUser user) {
		if (user.allowPower(this)
				&& !hasEffects.contains(user)) {
			user.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 3, false, false, true));
			hasEffects.add(user);
		}
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (hasEffects.contains(user)) {
			user.removePotionEffect(PotionEffectType.FAST_DIGGING);
			hasEffects.remove(user);
		}
	}

	@Override
	protected void config() {}

}
