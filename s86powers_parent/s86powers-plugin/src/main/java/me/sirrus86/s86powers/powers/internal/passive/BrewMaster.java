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

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Brew Master", type = PowerType.PASSIVE, author = "sirrus86", concept = "Swagofswag", icon = Material.POTION,
	description = "Potions you pick up now stack up to [stack]. Potions you drink now extend the duration of other similar potion effects on you.")
public class BrewMaster extends Power {
	
	private final EnumSet<Material> potMats = EnumSet.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION);
	private int stack;
//	private PowerStat consumes;

	@Override
	protected void config() {
//		consumes = stat("potions-consumed", 50, "Potions consumed", "You can combine potions by placing multiple potions in a crafting grid.");
		stack = option("potion-stack-size", 16, "How many potions should fit in a stack.");
	}
	
//	private ItemStack createPotion(ItemStack[] items) {
//		boolean isSplash = false, isLingering = false;
//		Collection<PotionEffect> effects = new ArrayList<PotionEffect>();
//		for (ItemStack slot : items) {
//			if (slot != null
//					&& slot.getType() != Material.AIR
//					&& potMats.contains(slot.getType())) {
//				if (slot.getType() == Material.LINGERING_POTION) {
//					isLingering = true;
//				}
//				if (slot.getType() == Material.SPLASH_POTION) {
//					isSplash = true;
//				}
//				if (slot.getItemMeta() instanceof PotionMeta) {
//					PotionMeta meta = (PotionMeta) slot.getItemMeta();
//					if (meta.hasCustomEffects()) {
//						effects.addAll(meta.getCustomEffects());
//					}
//				}
//			}
//			else {
//				return null;
//			}
//		}
//		if (!effects.isEmpty()) {
//			ItemStack potion = new ItemStack(Material.POTION, 1);
//			if (isSplash) potion.setType(Material.SPLASH_POTION);
//			if (isLingering) potion.setType(Material.LINGERING_POTION);
//			PotionMeta meta = (PotionMeta) potion.getItemMeta();
//			for (PotionEffect effect : effects) {
//				if (meta.hasCustomEffect(effect.getType())) {
//					for (PotionEffect pEffect : meta.getCustomEffects()) {
//						if (pEffect.getType() == effect.getType()) {
//							PotionEffect newEffect = new PotionEffect(effect.getType(), effect.getDuration() + pEffect.getDuration(), Integer.max(effect.getAmplifier(), pEffect.getAmplifier()));
//							meta.addCustomEffect(newEffect, true);
//						}
//					}
//				}
//				else {
//					meta.addCustomEffect(effect, true);
//				}
//			}
//			meta.setDisplayName(ChatColor.RESET + "Brew Master's Potion");
//			potion.setItemMeta(meta);
//			return potion;
//		}
//		return null;
//	}
	
//	@SuppressWarnings("deprecation")
//	@EventHandler(ignoreCancelled = true)
//	private void onClick(InventoryClickEvent event) {
//		if (event.getWhoClicked() instanceof Player) {
//			final PowerUser user = getUser((Player) event.getWhoClicked());
//			if (user.hasStatMaxed(consumes)) {
//				if (event.getInventory() instanceof CraftingInventory) {
//					final CraftingInventory inv = (CraftingInventory) event.getInventory();
//					runTask(new Runnable() {
//						@Override
//						public void run() {
//							ItemStack potion = createPotion(inv.getMatrix());
//							if (potion != null) {
//								inv.setResult(potion);
//								user.getPlayer().updateInventory();
//							}
//						}
//					});
//					if (potMats.contains(event.getCurrentItem().getType())) {
//						if (event.getSlotType() == SlotType.RESULT) {
//							if (event.isShiftClick()) {
//								event.getView().getBottomInventory().addItem(event.getCurrentItem());
//								inv.setMatrix(new ItemStack[inv.getMatrix().length]);
//								inv.setResult(null);
//								user.getPlayer().updateInventory();
//							}
//							else {
//								if (event.getCursor() == null
//										|| event.getCursor().getType() == Material.AIR) {
//									event.setCursor(event.getCurrentItem());
//									inv.setMatrix(new ItemStack[inv.getMatrix().length]);
//									inv.setResult(null);
//									user.getPlayer().updateInventory();
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDrink(PlayerItemConsumeEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& event.getItem() != null
				&& potMats.contains(event.getItem().getType())) {
			PotionMeta meta = (PotionMeta) event.getItem().getItemMeta();
			if (meta.hasCustomEffects()) {
				for (PotionEffect newEffect : meta.getCustomEffects()) {
					for (PotionEffect effect : user.getPlayer().getActivePotionEffects()) {
						if (effect.getType() == newEffect.getType()) {
							PotionEffect addEffect = new PotionEffect(effect.getType(), effect.getDuration() + newEffect.getDuration(), Integer.max(effect.getAmplifier(), newEffect.getAmplifier()));
							user.getPlayer().addPotionEffect(addEffect, true);
						}
					}
				}
//				user.increaseStat(consumes, 1);
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
