package me.sirrus86.s86powers.powers.internal.passive;

import org.bukkit.Material;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;

@PowerManifest(name = "Shapeshifter", type = PowerType.PASSIVE, author = "sirrus86", concept = "sirrus86", incomplete = true, icon=Material.PAPER,
	description = "...")
public class Shapeshifter extends Power {
	
	@Override
	protected void onEnable() {
		incomplete = true;
	}

	@Override
	protected void options() {
		
	}

}
