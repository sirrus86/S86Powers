package me.sirrus86.s86powers.powers.internal.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.UserContainer;

@PowerManifest(name = "Power Collector", type = PowerType.UTILITY, author = "sirrus86", concept = "sirrus86", icon=Material.ENCHANTED_BOOK,
	description = "Power books have a [dropChance]% chance to drop from mobs, as well as a chance to appear in treasure chests in the world. Power books can be read to learn new powers.")
public final class PowerCollector extends Power {

	private final Set<LootTables> lootTables = EnumSet.of(LootTables.ABANDONED_MINESHAFT, LootTables.BURIED_TREASURE, LootTables.DESERT_PYRAMID,
			LootTables.END_CITY_TREASURE, LootTables.FISHING_TREASURE, LootTables.IGLOO_CHEST, LootTables.JUNGLE_TEMPLE, LootTables.NETHER_BRIDGE,
			LootTables.PILLAGER_OUTPOST, LootTables.SHIPWRECK_TREASURE, LootTables.SIMPLE_DUNGEON, LootTables.STRONGHOLD_CORRIDOR,
			LootTables.STRONGHOLD_CROSSING, LootTables.STRONGHOLD_LIBRARY, LootTables.UNDERWATER_RUIN_BIG, LootTables.UNDERWATER_RUIN_SMALL,
			LootTables.WOODLAND_MANSION);
	
	private static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	
	private final NamespacedKey powerKey = createNamespacedKey("power-key");
	
	private double dropChance;
	private Firework firework = null;
	private Map<LootTables, Double> lootChance;
	private List<Power> powerWeight;
	
	@Override
	protected void onEnable() {
		lootChance = new HashMap<>();
		powerWeight = new ArrayList<>();
	}
	
	@Override
	protected void options() {
		for (LootTables tables : lootTables) {
			lootChance.put(tables, option("loot-chance." + tables.name().replace("_", "-").toLowerCase(), 1.0D,
					"Chance to find power books within the " + WordUtils.capitalizeFully(tables.name().replace("_", " ")) + " loot table."));
		}
		dropChance = option("drop-chance", 0.5D, "Chance to find power books when enemies are defeated.");
	}
	
	private ItemStack createPowerBook(Power power) {
		ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK, 1);
		ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		meta.setDisplayName(ChatColor.RESET.toString() + power.getType().getColor() + power.getName());
		meta.getPersistentDataContainer().set(powerKey, PersistentDataType.STRING, power.getClass().getSimpleName());
		List<String> lore = Collections.singletonList(ChatColor.RESET.toString() + ChatColor.GRAY + "Use to learn " + power.getName() + ".");
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}
	
	private LootTables getLootTables(LootTable table) {
		if (table != null) {
			for (LootTables tables : lootTables) {
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
				&& event.getEntity() instanceof LivingEntity
				&& !(event.getEntity() instanceof Player)
				&& event.getDrops() != null
				&& event.getDroppedExp() > 0
				&& random.nextDouble() < dropChance / 100.0D) {
			Collections.shuffle(powerWeight);
			event.getDrops().add(createPowerBook(powerWeight.get(0)));
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
					Inventory chestInv = chest.getBlockInventory();
					boolean deposited = false;
					for (int i = 0; i < chestInv.getSize(); i ++) {
						int j = random.nextInt(chestInv.getSize());
						if (chestInv.getItem(j) == null) {
							chestInv.setItem(j, createPowerBook(powerWeight.get(0)));
							deposited = true;
							break;
						}
					}
					if (!deposited
							&& chestInv.firstEmpty() != -1) {
						chestInv.setItem(chestInv.firstEmpty(), createPowerBook(powerWeight.get(0)));
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
					Power power = plugin.getConfigManager().getPower(pName);
					if (power != null) {
						UserContainer uCont = UserContainer.getContainer(user);
						if (uCont.hasPower(power)) {
							user.sendMessage(LocaleString.SELF_ALREADY_HAS_POWER.build(power));
						}
						else {
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
							uCont.addPower(power, true);
							user.sendMessage(LocaleString.SELF_ADD_POWER_SUCCESS.build(power));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onServerLoad(ServerLoadEvent event) {
		for (Power power : plugin.getConfigManager().getPowers()) {
			if (power.getType() != PowerType.UTILITY) {
				for (int i = 0; i < Math.max(0, option("power-weight." + power.getClass().getSimpleName(), 1,
						"Chance that " + power.getName() + " will be the power found. Higher values increase chances.")); i ++) {
					powerWeight.add(power);
				}
			}
		}
	}

}
