package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Decoy", type = PowerType.DEFENSE, author = "sirrus86", concept = "heylookoverthere", icon = Material.ARMOR_STAND, usesPackets = true,
	description = "By [act:item]ing any non-player entity while holding [item], transform the entity into a copy of yourself[consume], consuming the [item] in the process[/consume]. Effect lasts until entity is killed or despawns. [cooldown] cooldown.")
public final class Decoy extends Power {

	private Map<LivingEntity, PowerUser> decoys;
	private boolean consume;
	private double range;
	
	@Override
	protected void onEnable() {
		decoys = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		for (Entry<LivingEntity, PowerUser> entry : decoys.entrySet()) {
			if (entry.getValue() == user) {
				PowerTools.removeDisguise(entry.getKey());
			}
		}
	}

	@Override
	protected void options() {
		consume = option("consume-item", false, "Whether item should be consumed on use.");
		cooldown = option("cooldown", PowerTime.toMillis(0), "Amount of time before power can be used again.");
		item = option("item", new ItemStack(Material.BLAZE_ROD), "Item used to create decoys.");
		range = option("range", 5.0D, "How far away user can be to turn an entity into a decoy.");
		supplies(item);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			LivingEntity target = user.getTargetEntity(LivingEntity.class, range);
			if (target != null
					&& !(target instanceof Player)) {
				if (user.getCooldown(this) <= 0) {
					user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
					PowerTools.addDisguise(target, user.getPlayer());
					if (consume) {
						event.consumeItem();
					}
					user.setCooldown(this, cooldown);
					decoys.put(target, user);
				}
				else {
					user.showCooldown(this);
				}
			}
		}
	}

}
