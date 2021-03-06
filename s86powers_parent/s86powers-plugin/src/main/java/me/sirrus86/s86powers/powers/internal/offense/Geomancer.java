package me.sirrus86.s86powers.powers.internal.offense;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Geomancer", type = PowerType.OFFENSE, author = "sirrus86", concept = "kamyarm007", incomplete = true, icon = Material.MOSSY_COBBLESTONE,
description = "[act:item]ing a distant entity while holding [item] will cause the ground beneath the target to deal unique effects to the target. Target must be on the ground. [cooldown] cooldown.")
public class Geomancer extends Power {

	private PowerOption<Double> minRange, range;
	
	@Override
	protected void onEnable() {
		incomplete = true;
	}

	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(2, 0), "Period of time before power can be used again.");
		item = option("item", new ItemStack(Material.MOSSY_COBBLESTONE), "Item required to use power.");
		minRange = option("minimum-range", 5.0D, "Minimum range which power can be used on targets.");
		range = option("range", 25.0D, "Maximum range which power can be used on targets.");
		supplies(getRequiredItem());
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0) {
				Damageable target = user.getTargetEntity(Damageable.class, user.getOption(range));
				if (target != null
						&& user.getPlayer().getLocation().distanceSquared(target.getLocation()) >= user.getOption(minRange) * user.getOption(minRange)
						&& target.isOnGround()) {
					
				}
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	private enum GeoType {
		
		SAND_SWALLOW;
		
		public static GeoType getByBlockType(Material type) {
			switch(type) {
				case COARSE_DIRT: case DIRT: case GRASS_BLOCK: case PODZOL: return null; //TODO
				case GRAVEL: case RED_SAND: case SAND: return SAND_SWALLOW;
				default: return null;
			}
		}
		
	}

}
