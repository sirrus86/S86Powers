package me.sirrus86.s86powers.powers.internal.offense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerIgniteEvent;
import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Blastwave", type = PowerType.OFFENSE, author = "sirrus86", concept = "brysi", icon=Material.BLAZE_POWDER,
	description = "[act:item]ing while holding [item] creates a fiery explosion beneath you, propelling you up and away from the direction you're facing. [cooldown] cooldown.")
public final class Blastwave extends Power {
	
	private Map<PowerUser, Long> fallCDs;
	
	private double blastRad, hVec, vVec;
	private boolean consume;
	private long fallCD;
	
	@Override
	protected void onEnable() {
		fallCDs = new HashMap<>();
	}
	
	@Override
	protected void config() {
		blastRad = option("blast-radius", 3.0D, "Radius size of the blast.");
		consume = option("consume-item", true, "Whether item should be consumed when power is used.");
		cooldown = option("cooldown", PowerTime.toMillis(10, 0), "Amount of time before power can be used again.");
		fallCD = option("fall-cooldown", PowerTime.toMillis(2, 0), "How long to give player fall damage immunity after using power.");
		hVec = option("horizontal-momentum", -1.5D, "Vector modifier for horizontal movement after blast is initiated.");
		item = option("item", new ItemStack(Material.BLAZE_ROD), "Item used to create blasts.");
		vVec = option("vertical-momentum", 1.0D, "Vector modifier for vertical movement after blast is initiated.");
		supplies(new ItemStack(item.getType(), item.getMaxStackSize() / 4));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0) {
				for (Block block : PowerTools.getNearbyBlocks(user.getPlayer().getLocation(), blastRad)) {
					PowerIgniteEvent pEvent = new PowerIgniteEvent(this, user, block, BlockFace.SELF);
					callEvent(pEvent);
				}
				Vector dir = user.getPlayer().getLocation().getDirection();
				user.getPlayer().setVelocity(new Vector(dir.getX() * hVec, vVec, dir.getZ() * hVec));
				user.setCooldown(this, cooldown);
				if (consume) {
					event.consumeItem();
				}
				fallCDs.put(user, System.currentTimeMillis() + fallCD);
			}
			else {
				user.showCooldown(this);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onFall(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getCause() == DamageCause.FALL) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& fallCDs.containsKey(user)
					&& fallCDs.get(user) > System.currentTimeMillis()) {
				event.setCancelled(true);
			}
		}
	}

}
