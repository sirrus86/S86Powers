package me.sirrus86.s86powers.powers.internal.offense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

	private PowerOption<List<String>> snowMats1, snowMats2, snowMats3;
	private PowerOption<List<PotionEffect>> snowMats1Effects, snowMats2Effects, snowMats3Effects;
	private PowerOption<Map<String, Integer>> snowMats1Enchants, snowMats2Enchants, snowMats3Enchants;
	private String effectDesc;
	
	private final NamespacedKey isPolar = createNamespacedKey("is-polar-blade"),
			pBladeEffectAmps = createNamespacedKey("effect-amps"),
			pBladeEffectDurs = createNamespacedKey("effect-durs"),
			pBladeEffectTypes = createNamespacedKey("effect-types");
	
	@Override
	protected void config() {
		effectDesc = locale("message.effect-descriptor", ChatColor.RED + "[effect] [power] ([time])");
		snowMats1 = option("material1.types", List.of("ICE"), "Materials used in creating Polar Blades.");
		snowMats1Effects = option("material1.effects", List.of(new PotionEffect(PotionEffectType.SLOW, 0, 0)), "Effects caused by Polar Blades.");
		snowMats1Enchants = option("material1.enchants", Map.of("SHARPNESS", 0), "Enchants placed on Polar Blades.");
		snowMats2 = option("material2.types", List.of("SNOWBALL"), "Materials used in creating Polar Blades.");
		snowMats2Effects = option("material2.effects", List.of(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toMillis(5, 0), 0)), "Effects caused by Polar Blades.");
		snowMats2Enchants = option("material2.enchants", new HashMap<>(), "Enchants placed on Polar Blades.");
		snowMats3 = option("material3.types", List.of("SNOW_BLOCK"), "Materials used in creating Polar Blades.");
		snowMats3Effects = option("material3.effects", List.of(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toMillis(5, 0), 3)), "Effects caused by Polar Blades.");
		snowMats3Enchants = option("material3.enchants", new HashMap<>(), "Enchants placed on Polar Blades.");
		supplies(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.ICE, 16), new ItemStack(Material.SNOW_BLOCK, 16));
	}
	
	@SuppressWarnings("deprecation")
	private ItemStack createPBlade(PowerUser user, ItemStack[] matrix) {
		ItemStack sword = null;
		Map<PotionEffectType, PotionEffect> effects = new HashMap<>();
		Map<Enchantment, Integer> enchants = new HashMap<>();
		for (int i = 0; i < matrix.length; i ++) {
			ItemStack stack = matrix[i];
			if (stack != null && stack.getType() != Material.AIR) {
				if (PowerTools.isSword(stack)) {
					sword = stack.clone();
				}
				else if (user.getOption(snowMats1).contains(stack.getType().name())){
					if (!user.getOption(snowMats1Effects).isEmpty()) {
						for (PotionEffect effect : user.getOption(snowMats1Effects)) {
							PotionEffectType effectType = effect.getType();
							int amp = effect.getAmplifier(),
									dur = effect.getDuration();
							if (effects.containsKey(effectType)) {
								amp += effects.get(effect.getType()).getAmplifier() + 1;
								dur = Math.max(dur, effects.get(effect.getType()).getDuration());
							}
							effects.put(effectType, new PotionEffect(effectType, amp, dur));
						}
					}
					if (!user.getOption(snowMats1Enchants).isEmpty()) {
						for (String enchantStr : user.getOption(snowMats1Enchants).keySet()) {
							Enchantment enchant = Enchantment.getByName(enchantStr);
							int strength = user.getOption(snowMats1Enchants).get(enchantStr);
							if (enchants.containsKey(enchant)) {
								strength += enchants.get(enchant);
							}
							enchants.put(enchant, strength);
						}
					}
				}
				else if (user.getOption(snowMats2).contains(stack.getType().name())){
					if (!user.getOption(snowMats2Effects).isEmpty()) {
						for (PotionEffect effect : user.getOption(snowMats2Effects)) {
							PotionEffectType effectType = effect.getType();
							int amp = effect.getAmplifier(),
									dur = effect.getDuration();
							if (effects.containsKey(effectType)) {
								amp += effects.get(effect.getType()).getAmplifier() + 1;
								dur = Math.max(dur, effects.get(effect.getType()).getDuration());
							}
							effects.put(effectType, new PotionEffect(effectType, amp, dur));
						}
					}
					if (!user.getOption(snowMats2Enchants).isEmpty()) {
						for (String enchantStr : user.getOption(snowMats2Enchants).keySet()) {
							Enchantment enchant = Enchantment.getByName(enchantStr);
							int strength = user.getOption(snowMats2Enchants).get(enchantStr);
							if (enchants.containsKey(enchant)) {
								strength += enchants.get(enchant);
							}
							enchants.put(enchant, strength);
						}
					}
				}
				else if (user.getOption(snowMats3).contains(stack.getType().name())){
					if (!user.getOption(snowMats3Effects).isEmpty()) {
						for (PotionEffect effect : user.getOption(snowMats3Effects)) {
							PotionEffectType effectType = effect.getType();
							int amp = effect.getAmplifier(),
									dur = effect.getDuration();
							if (effects.containsKey(effectType)) {
								amp += effects.get(effect.getType()).getAmplifier() + 1;
								dur = Math.max(dur, effects.get(effect.getType()).getDuration());
							}
							effects.put(effectType, new PotionEffect(effectType, amp, dur));
						}
					}
					if (!user.getOption(snowMats3Enchants).isEmpty()) {
						for (String enchantStr : user.getOption(snowMats3Enchants).keySet()) {
							Enchantment enchant = Enchantment.getByName(enchantStr);
							int strength = user.getOption(snowMats3Enchants).get(enchantStr);
							if (enchants.containsKey(enchant)) {
								strength += enchants.get(enchant);
							}
							enchants.put(enchant, strength);
						}
					}
				}
			}
		}
		if (sword != null) {
			ItemMeta meta = sword.hasItemMeta() ? getRequiredItem().getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(sword.getType());
			meta.setDisplayName(ChatColor.RESET + this.getName());
			meta.getPersistentDataContainer().set(isPolar, PersistentDataType.BYTE, (byte) 0x1);
			if (!effects.isEmpty()) {
				List<String> newLore = new ArrayList<>();
				int[] amps = new int[effects.size()],
						durs = new int[effects.size()],
						types = new int[effects.size()];
				for (int i = 0; i < effects.size(); i ++) {
					PotionEffect effect = effects.get(effects.keySet().toArray()[i]);
					amps[i] = effect.getAmplifier();
					durs[i] = effect.getDuration();
					types[i] = effect.getType().getId();
					String effectText = effectDesc.replace("[type]", PowerTools.getPotionEffectName(effect.getType()))
							.replace("[power]", PowerTools.getRomanNumeral(effect.getAmplifier()))
							.replace("[time]", PowerTime.asClock(effect.getDuration(), false, false, true, true, false));
					newLore.add(effectText);
				}
				meta.getPersistentDataContainer().set(pBladeEffectAmps, PersistentDataType.INTEGER_ARRAY, amps);
				meta.getPersistentDataContainer().set(pBladeEffectDurs, PersistentDataType.INTEGER_ARRAY, durs);
				meta.getPersistentDataContainer().set(pBladeEffectTypes, PersistentDataType.INTEGER_ARRAY, types);
				meta.setLore(newLore);
			}
			if (!enchants.isEmpty()) {
				for (Enchantment enchant : enchants.keySet()) {
					meta.addEnchant(enchant, enchants.get(enchant), true);
				}
			}
			sword.setItemMeta(meta);
		}
		return sword;
	}
	
	@SuppressWarnings("deprecation")
	private Collection<PotionEffect> getPBladeEffects(ItemStack sword) {
		List<PotionEffect> effects = new ArrayList<>();
		if (isPBlade(sword)) {
			PersistentDataContainer container = sword.getItemMeta().getPersistentDataContainer();
			if (container.has(pBladeEffectAmps, PersistentDataType.INTEGER_ARRAY)
					&& container.has(pBladeEffectDurs, PersistentDataType.INTEGER_ARRAY)
					&& container.has(pBladeEffectTypes, PersistentDataType.INTEGER_ARRAY)) {
				int[] amps = container.get(pBladeEffectAmps, PersistentDataType.INTEGER_ARRAY),
						durs = container.get(pBladeEffectDurs, PersistentDataType.INTEGER_ARRAY),
						types = container.get(pBladeEffectTypes, PersistentDataType.INTEGER_ARRAY);
				for (int i = 0; i < types.length; i ++) {
					effects.add(new PotionEffect(PotionEffectType.getById(types[i]), amps[i], durs[i]));
				}
			}
		}
		return effects;
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
	
	private boolean isPBladeRecipe(PowerUser user, ItemStack[] matrix) {
		boolean broken = false, hasMats = false, hasSword = false;
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
				else if (user.getOption(snowMats1).contains(stack.getType().name())
						|| user.getOption(snowMats2).contains(stack.getType().name())
						|| user.getOption(snowMats3).contains(stack.getType().name())) {
					hasMats = true;
				}
				else {
					broken = true;
					break;
				}
			}
		}
		return !broken && hasMats && hasSword;
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
				LivingEntity entity = (LivingEntity) event.getEntity();
				entity.addPotionEffects(getPBladeEffects(sword));
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
						if (isPBladeRecipe(user, inv.getMatrix())) {
							inv.setResult(createPBlade(user, inv.getMatrix()));
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
