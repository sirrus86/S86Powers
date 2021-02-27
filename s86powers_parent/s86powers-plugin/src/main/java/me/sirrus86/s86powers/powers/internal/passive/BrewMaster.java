package me.sirrus86.s86powers.powers.internal.passive;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Brew Master", type = PowerType.PASSIVE, author = "sirrus86", concept = "Swagofswag", icon = Material.POTION,
	description = "Potions you pick up now stack up to [potion-stack-size]. Potions you drink now extend the duration of other similar potion effects on you.")
public class BrewMaster extends Power {
	
	private final EnumSet<Material> potMats = EnumSet.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
	private int stack;

	@Override
	protected void config() {
		stack = option("potion-stack-size", 16, "How many potions should fit in a stack.");
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDrink(PlayerItemConsumeEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& event.getItem() != null
				&& potMats.contains(event.getItem().getType())) {
			PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
			PotionEffect newEffect = PowerTools.getPotionEffect(meta.getBasePotionData());
			PotionEffect effect = user.getPlayer().getPotionEffect(newEffect.getType());
			if (effect != null) {
				PotionEffect addEffect = new PotionEffect(effect.getType(), effect.getDuration() + newEffect.getDuration(), Integer.max(effect.getAmplifier(), newEffect.getAmplifier()));
				runTask(new BukkitRunnable() {

					@Override
					public void run() {
						user.getPlayer().addPotionEffect(addEffect);
					}
					
				});
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onPickup(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& potMats.contains(event.getItem().getItemStack().getType())) {
				ItemStack item = event.getItem().getItemStack();
				for (ItemStack slot : user.getPlayer().getInventory().getContents()) {
					if (slot != null
							&& slot.isSimilar(item)
							&& slot.getAmount() < stack) {
						slot.setAmount(slot.getAmount() + 1);
						PowerTools.fakeCollect(event.getEntity(), event.getItem());
						event.getItem().remove();
						event.setCancelled(true);
						break;
					}
				}
			}
		}
	}

}
