package me.sirrus86.s86powers.powers.internal.offense;

import java.util.EnumSet;

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

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Polar Blade", type = PowerType.OFFENSE, author = "sirrus86", concept = "bobby16may", version = MCVersion.v1_14, icon = Material.IRON_SWORD,
	description = "Able to craft Polar Blades. Polar Blades require a sword and snow materials. When crafted, comes equipped with a Sharpness enchant. Attacks with a Polar Blade also slow enemies.")
public final class PolarBlade extends Power {

	private final EnumSet<Material> snowMats = EnumSet.of(Material.ICE, Material.SNOWBALL, Material.SNOW_BLOCK);
	private PowerOption<Integer> maxSharp;
	private PowerOption<Long> slowDur;
	private String slowDesc;
	
	private final NamespacedKey isPolar = createNamespacedKey("is-polar-blade"),
			slowFactor = createNamespacedKey("slow-factor");
	
	@Override
	protected void config() {
		maxSharp = option("maximum-sharpness", 8, "Maximum sharpness enchant that can be applied to a Polar Blade.");
		slowDur = option("slowness-duration", PowerTime.toMillis(5, 0), "Amount of time slowness effect lasts.");
		slowDesc = locale("message.slowness-descriptor", ChatColor.RED + "Slowness [power] ([time])");
		supplies(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.ICE, 16), new ItemStack(Material.SNOW_BLOCK, 16));
	}
	
	private ItemStack createPBlade(ItemStack[] matrix) {
		ItemStack sword = null;
		int slow = 0, sharp = 0;
		for (int i = 0; i < matrix.length; i ++) {
			ItemStack stack = matrix[i];
			if (stack != null && stack.getType() != Material.AIR) {
				if (PowerTools.isSword(stack)) {
					sword = stack.clone();
				}
				else if (snowMats.contains(stack.getType())) {
					if (stack.getType() == Material.ICE) {
						sharp ++;
					}
					else if (stack.getType() == Material.SNOWBALL) {
						slow ++;
					}
					else if (stack.getType() == Material.SNOW_BLOCK) {
						slow += 4;
					}
				}
			}
		}
		if (sword != null) {
			ItemMeta meta = sword.hasItemMeta() ? getRequiredItem().getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(sword.getType());
			meta.setDisplayName(ChatColor.RESET + this.getName());
			meta.getPersistentDataContainer().set(isPolar, PersistentDataType.BYTE, (byte) 0x1);
			if (slow > 0) {
				meta.getPersistentDataContainer().set(slowFactor, PersistentDataType.INTEGER, slow - 1);
				String slowing = slowDesc.replace("[power]", PowerTools.getRomanNumeral(slow)).replace("[time]", PowerTime.asClock(getOption(slowDur), false, false, true, true, false));
				meta.setLore(Lists.newArrayList(slowing));
			}
			if (sharp > 0) {
				meta.addEnchant(Enchantment.DAMAGE_ALL, sharp < getOption(maxSharp) ? sharp : getOption(maxSharp), true);
			}
			sword.setItemMeta(meta);
		}
		return sword;
	}
	
	private int getPBladeSlow(ItemStack sword) {
		if (isPBlade(sword)
				&& sword.getItemMeta().getPersistentDataContainer().has(slowFactor, PersistentDataType.INTEGER)) {
			return sword.getItemMeta().getPersistentDataContainer().get(slowFactor, PersistentDataType.INTEGER);
		}
		return -1;
	}
	
	private boolean hasPBlade(LivingEntity entity) {
		ItemStack sword = entity instanceof Player ? ((Player) entity).getInventory().getItemInMainHand() : entity.getEquipment().getItemInMainHand();
		return sword != null
				&& isPBlade(sword);
	}
	
	private boolean isPBlade(ItemStack sword) {
		return PowerTools.isSword(sword)
				&& sword.hasItemMeta()
				&& sword.getItemMeta().getPersistentDataContainer().has(isPolar, PersistentDataType.BYTE);
	}
	
	private boolean isPBladeRecipe(ItemStack[] matrix) {
		boolean broken = false, hasSnow = false, hasSword = false;
		for (int i = 0; i < matrix.length; i ++) {
			ItemStack stack = matrix[i];
			if (stack != null && stack.getType() != Material.AIR) {
				if (PowerTools.isSword(stack)
						&& (!stack.hasItemMeta() || !stack.getItemMeta().hasEnchants())) {
					if (!hasSword) hasSword = true;
					else {
						broken = true;
						break;
					}
				}
				else if (snowMats.contains(stack.getType())) {
					hasSnow = true;
				}
				else {
					broken = true;
					break;
				}
			}
		}
		return !broken && hasSnow && hasSword;
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof LivingEntity
				&& event.getEntity() instanceof LivingEntity) {
			LivingEntity damager = (LivingEntity) event.getDamager();
			if (hasPBlade(damager)) {
				if (damager instanceof Player) {
					PowerUser user = getUser((Player) damager);
					user.causeDamage(this, event);
				}
				ItemStack sword = damager instanceof Player ? ((Player) damager).getInventory().getItemInMainHand() : damager.getEquipment().getItemInMainHand();
				int slow = getPBladeSlow(sword);
				if (slow >= 0) {
					LivingEntity entity = (LivingEntity) event.getEntity();
					entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toTicks(getOption(slowDur)), slow, true));
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	private void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			final PowerUser user = getUser((Player) event.getWhoClicked());
			if (user.allowPower(this)
					&& event.getInventory() instanceof CraftingInventory) {
				final CraftingInventory inv = (CraftingInventory) event.getInventory();
				runTaskLater(new Runnable() {
					@Override
					public void run() {
						if (isPBladeRecipe(inv.getMatrix())) {
							inv.setResult(createPBlade(inv.getMatrix()));
							user.getPlayer().updateInventory();
						}
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
						else {
							if (event.getCursor() == null
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

}
