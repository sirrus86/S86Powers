package me.sirrus86.s86powers.powers.internal.offense;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Polar Blade", type = PowerType.OFFENSE, author = "sirrus86", concept = "bobby16may", version = MCVersion.v1_14, icon = Material.IRON_SWORD,
	description = "Able to craft Polar Blades. Polar Blades require a sword and snow materials. When crafted, comes equipped with a Sharpness enchant. Attacks with a Polar Blade also slow enemies.")
public final class PolarBlade extends Power {

	private String effectDesc;
	
	private final NamespacedKey isPolar = createNamespacedKey("is-polar-blade"),
			pBladeEffectAmps = createNamespacedKey("slow-amp");
	
	@Override
	protected void config() {
		effectDesc = locale("message.effect-descriptor", ChatColor.RED + "[effect] [power] ([time])");
		supplies(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.ICE, 16), new ItemStack(Material.SNOW_BLOCK, 16));
	}
	
	private ItemStack createPBlade(ItemStack[] matrix) {
		ItemStack sword = null;
		int slow = 0;
		int sharpness = 0;
		for (ItemStack stack : matrix) {
			if (stack != null && stack.getType() != Material.AIR) {
				if (PowerTools.isSword(stack)) {
					sword = stack.clone();
				} else if (stack.getType() == Material.ICE) {
					sharpness++;
				} else if (stack.getType() == Material.SNOWBALL) {
					slow++;
				} else if (stack.getType() == Material.SNOW_BLOCK) {
					slow += 3;
				}
			}
		}
		if (sword != null) {
			ItemMeta meta = sword.hasItemMeta() ? getRequiredItem().getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(sword.getType());
			if (meta != null) {
				meta.setDisplayName(ChatColor.RESET + this.getName());
				meta.getPersistentDataContainer().set(isPolar, PersistentDataType.BYTE, (byte) 0x1);
				if (sharpness > 0) {
					meta.addEnchant(Enchantment.DAMAGE_ALL, sharpness, true);
				}
				if (slow > 0) {
					slow --;
					meta.getPersistentDataContainer().set(pBladeEffectAmps, PersistentDataType.INTEGER, slow);
					String effectText = effectDesc.replace("[effect]", "Slowness")
							.replace("[power]", PowerTools.getRomanNumeral(slow))
							.replace("[time]", PowerTime.asClock(PowerTime.toMillis(3, 0), false, false, true, true, false));
					meta.setLore(List.of(effectText));
				}
				sword.setItemMeta(meta);
			}
		}
		return sword;
	}
	
	private boolean hasPBlade(LivingEntity entity) {
		ItemStack sword = entity instanceof Player ? ((Player) entity).getInventory().getItemInMainHand() : entity.getEquipment() != null ? entity.getEquipment().getItemInMainHand() : null;
		return isPBlade(sword);
	}
	
	private boolean isPBlade(ItemStack sword) {
		return PowerTools.isSword(sword)
				&& sword.getItemMeta() != null
				&& sword.getItemMeta().getPersistentDataContainer().has(isPolar, PersistentDataType.BYTE);
	}
	
	private boolean isPBladeRecipe(ItemStack[] matrix) {
		boolean broken = false, hasMats = false, hasSword = false;
		for (ItemStack stack : matrix) {
			if (stack != null && stack.getType() != Material.AIR) {
				if (PowerTools.isSword(stack)
						&& (stack.getItemMeta() == null || !stack.getItemMeta().hasEnchants())) {
					if (!hasSword) hasSword = true;
					else {
						broken = true;
						break;
					}
				} else if (stack.getType() == Material.ICE
						|| stack.getType() == Material.SNOWBALL
						|| stack.getType() == Material.SNOW_BLOCK) {
					hasMats = true;
				} else {
					broken = true;
					break;
				}
			}
		}
		return !broken && hasMats && hasSword;
	}
	
	@SuppressWarnings("DataFlowIssue")
	@EventHandler(ignoreCancelled = true)
	private void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof LivingEntity
				&& event.getEntity() instanceof LivingEntity damager) {
			if (hasPBlade(damager)) {
				if (damager instanceof Player) {
					PowerUser user = getUser((Player) damager);
					user.causeDamage(this, event);
				}
				ItemStack sword = damager instanceof Player ? ((Player) damager).getInventory().getItemInMainHand() : damager.getEquipment() != null ? damager.getEquipment().getItemInMainHand() : null;
				LivingEntity entity = (LivingEntity) event.getEntity();
				int slowAmp = sword != null && sword.getItemMeta() != null ? sword.getItemMeta().getPersistentDataContainer().get(pBladeEffectAmps, PersistentDataType.INTEGER) : -1;
				entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, PowerTime.toTicks(3, 0), slowAmp));
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	private void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			final PowerUser user = getUser((Player) event.getWhoClicked());
			if (user.allowPower(this)
					&& event.getInventory() instanceof final CraftingInventory inv) {
				runTaskLater(() -> {
					if (isPBladeRecipe(inv.getMatrix())) {
						inv.setResult(createPBlade(inv.getMatrix()));
						user.getPlayer().updateInventory();
					}
				}, 1L);
				if (event.getCurrentItem() != null
						&& isPBlade(event.getCurrentItem())) {
					if (event.getSlotType() == SlotType.RESULT) {
						if (event.isShiftClick()) {
							event.getView().getBottomInventory().addItem(event.getCurrentItem());
							inv.setMatrix(new ItemStack[inv.getMatrix().length]);
							inv.setResult(null);
							user.getPlayer().updateInventory();
						}
						else if (event.getCursor() == null
								|| event.getCursor().getType() == Material.AIR) {
							event.setCursor(event.getCurrentItem());
							inv.setMatrix(new ItemStack[inv.getMatrix().length]);
							inv.setResult(null);
							user.getPlayer().updateInventory();
						}
					}
				}
			}
		}
	}

}
