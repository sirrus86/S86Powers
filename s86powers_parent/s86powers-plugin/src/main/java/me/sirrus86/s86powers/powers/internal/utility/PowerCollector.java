package me.sirrus86.s86powers.powers.internal.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.Lists;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerAdapter;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.PowerUserAdapter;

@PowerManifest(name = "Power Collector", type = PowerType.UTILITY, author = "sirrus86", concept = "sirrus86", icon = Material.ENCHANTED_BOOK,
	description = "Power books have a [dropChance]% chance to drop from mobs, as well as a chance to appear in treasure chests in the world. Power books can be read to learn new powers.")
public final class PowerCollector extends Power {
	
	private final NamespacedKey powerKey = createNamespacedKey("power-key");
	
	private boolean enforceCap;
	private Firework firework = null;
	private Map<LootTables, Double> lootChance;
	private int powerCap;
	private List<Power> powerWeight;
	
	@Override
	protected void onEnable() {
		lootChance = new HashMap<>();
		powerWeight = new ArrayList<>();
	}
	
	@Override
	protected void config() {
		for (LootTables tables : LootTables.values()) {
			lootChance.put(tables, option("loot-chance." + tables.name().replace("_", "-").toLowerCase(), 0.5D,
					"Chance to find power books within the " + WordUtils.capitalizeFully(tables.name().replace("_", " ")) + " loot table."));
		}
		for (Power power : S86Powers.getConfigManager().getPowers()) {
			if (power.getType() != PowerType.UTILITY) {
				for (int i = 0; i < Math.max(0, option("power-weight." + power.getClass().getSimpleName(), 1,
						"Chance that " + power.getName() + " will be the power found. Higher values increase chances.")); i ++) {
					powerWeight.add(power);
				}
			}
		}
		enforceCap = option("enforce-cap", false, "Whether to prevent power books from dropping if too many players have the power assigned.");
		powerCap = option("power-cap", 20, "Maximum number of players that have power assigned before books stop dropping for that power.");
	}
	
	private ItemStack createPowerBook(Power power) {
		ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK, 1);
		ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		meta.setDisplayName(ChatColor.RESET.toString() + power.getType().getColor() + power.getName());
		meta.getPersistentDataContainer().set(powerKey, PersistentDataType.STRING, power.getClass().getSimpleName());
		String powerDesc = PowerTools.getFilteredText(power, PowerAdapter.getAdapter(power).getDescription());
		List<String> lore = PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), powerDesc, 30);
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}
	
	private boolean canAddPower(PowerUser user, Power power) {
		if (ConfigOption.Users.ENFORCE_POWER_CAP) {
			if (user.getAssignedPowers().size() < ConfigOption.Users.POWER_CAP_TOTAL
					&& user.getPlayer().hasPermission(PowerAdapter.getAdapter(power).getAssignPermission())) {
				if (ConfigOption.Users.REPLACE_POWERS_OF_SAME_TYPE
						&& user.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE) {
					List<Power> powers = Lists.newArrayList(user.getAssignedPowersByType(power.getType()));
					Collections.shuffle(powers);
					Power removePower = powers.get(0);
					PowerUserAdapter.getAdapter(user).removePower(removePower);
					user.sendMessage(LocaleString.SELF_REMOVE_POWER_SUCCESS.build(removePower));
					return true;
				}
				else {
					return (user.getAssignedPowers().size() < ConfigOption.Users.POWER_CAP_TOTAL
							&& user.getAssignedPowersByType(power.getType()).size() < ConfigOption.Users.POWER_CAP_PER_TYPE);
				}
			}
			else {
				return false;
			}
		}
		else {
			return user.getPlayer().hasPermission(PowerAdapter.getAdapter(power).getAssignPermission());
		}
	}
	
	private LootTables getLootTables(LootTable table) {
		if (table != null) {
			for (LootTables tables : LootTables.values()) {
				if (Bukkit.getLootTable(tables.getKey()).equals(table)) {
					return tables;
				}
			}
		}
		return null;
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onDmg(EntityDamageByEntityEvent event) {
		if (firework != null
				&& event.getDamager() == firework) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	private void onDeath(EntityDeathEvent event) {
		if (ConfigOption.Plugin.USE_LOOT_TABLES
				&& event.getDroppedExp() > 0
				&& event.getEntity() instanceof Lootable) {
			Lootable entity = (Lootable) event.getEntity();
			if (entity.getLootTable() != null
					&& getLootTables(entity.getLootTable()) != null
					&& lootChance.containsKey(getLootTables(entity.getLootTable()))
					&& random.nextDouble() < lootChance.get(getLootTables(entity.getLootTable())) / 100.0D) {
				Collections.shuffle(powerWeight);
				Power power = powerWeight.get(0);
				if (!enforceCap
						|| power.getUsers().size() < powerCap) {
					event.getDrops().add(createPowerBook(power));
				}
			}
		}
	}
	
	@EventHandler
	private void onInteract(PlayerInteractEvent event) {
		if (ConfigOption.Plugin.USE_LOOT_TABLES
				&& event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
			PowerUser user = getUser(event.getPlayer());
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK
					&& event.getClickedBlock() != null
					&& (event.getClickedBlock().getType() == Material.CHEST
						|| event.getClickedBlock().getType() == Material.TRAPPED_CHEST)) {
				Chest chest = (Chest) event.getClickedBlock().getState();
				if (chest.getLootTable() != null
						&& getLootTables(chest.getLootTable()) != null
						&& lootChance.containsKey(getLootTables(chest.getLootTable()))
						&& random.nextDouble() < lootChance.get(getLootTables(chest.getLootTable())) / 100.0D) {
					Collections.shuffle(powerWeight);
					Power power = powerWeight.get(0);
					if (!enforceCap
							|| power.getUsers().size() < powerCap) {
						Inventory chestInv = chest.getBlockInventory();
						boolean deposited = false;
						for (int i = 0; i < chestInv.getSize(); i ++) {
							int j = random.nextInt(chestInv.getSize());
							if (chestInv.getItem(j) == null) {
								chestInv.setItem(j, createPowerBook(power));
								deposited = true;
								break;
							}
						}
						if (!deposited
								&& chestInv.firstEmpty() != -1) {
							chestInv.setItem(chestInv.firstEmpty(), createPowerBook(power));
						}
					}
				}
			}
			else if (event.getAction().name().startsWith("RIGHT")
					&& event.hasItem()
					&& event.getItem().getType() == Material.ENCHANTED_BOOK
					&& event.getItem().hasItemMeta()) {
				ItemStack stack = event.getItem();
				ItemMeta meta = stack.getItemMeta();
				if (meta.getPersistentDataContainer().has(powerKey, PersistentDataType.STRING)) {
					String pName = meta.getPersistentDataContainer().get(powerKey, PersistentDataType.STRING);
					Power power = S86Powers.getConfigManager().getPower(pName);
					if (power != null) {
						if (user.hasPower(power)) {
							user.sendMessage(LocaleString.SELF_ALREADY_HAS_POWER.build(power));
						}
						else if (canAddPower(user, power)) {
							firework = user.getPlayer().getWorld().spawn(user.getPlayer().getEyeLocation(), Firework.class);
							FireworkMeta fMeta = firework.getFireworkMeta();
							fMeta.clearEffects();
							fMeta.addEffect(FireworkEffect.builder()
									.with(Type.BURST)
									.withColor(power.getType() == PowerType.DEFENSE ? Color.BLUE : (power.getType() == PowerType.OFFENSE ? Color.RED : Color.YELLOW))
									.withFlicker()
									.build());
							firework.setFireworkMeta(fMeta);
							firework.detonate();
							stack.setAmount(stack.getAmount() - 1);
							user.addPower(power, true);
							user.sendMessage(LocaleString.SELF_ADD_POWER_SUCCESS.build(power));
						}
						else if (user.getAssignedPowers().size() >= ConfigOption.Users.POWER_CAP_TOTAL) {
							user.sendMessage(LocaleString.SELF_TOO_MANY_POWERS.toString());
						}
						else if (user.getAssignedPowersByType(power.getType()).size() >= ConfigOption.Users.POWER_CAP_PER_TYPE) {
							user.sendMessage(LocaleString.SELF_TOO_MANY_POWERS_TYPE.build(power.getType()));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onServerLoad(ServerLoadEvent event) {
		config();
	}

}
