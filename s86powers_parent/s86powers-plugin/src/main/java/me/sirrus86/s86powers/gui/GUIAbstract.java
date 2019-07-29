package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;

public abstract class GUIAbstract {

	static final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	private final static String GUIHEADER = ChatColor.BOLD + "" + ChatColor.GOLD + "S86 Powers" + ChatColor.RESET + " v" + plugin.getDescription().getVersion();
	
	@SuppressWarnings("deprecation")
	static final Material BACK = Material.LEGACY_SIGN, CONFIG = Material.CRAFTING_TABLE, DELETE = Material.BARRIER, ENABLE = Material.REDSTONE_TORCH,
			GROUP = Material.TOTEM_OF_UNDYING, LIST = Material.FILLED_MAP, PAGE = Material.PAPER, PLAYER = Material.PLAYER_HEAD,
			POWER = Material.BLAZE_POWDER, RELOAD = Material.BOOK, SAVE = Material.WRITABLE_BOOK;
	
	Map<Integer, GUIAction> actions = new HashMap<>();
	private static Map<UUID, GUIAbstract> currentGUI = new HashMap<>();
	private static Map<UUID, List<GUIAbstract>> previousGUI = new HashMap<>();
	
	static Map<UUID, PowerGroup> selectedGroup = new HashMap<>();
	static Map<UUID, Power> selectedPower = new HashMap<>();
	static Map<UUID, PowerUser> selectedUser = new HashMap<>();
	
	Inventory guiInv;
	
	public GUIAbstract(int rows, String name) {
		guiInv = Bukkit.createInventory(null, rows * 9, GUIHEADER + " - " + name);
	}
	
	void clearHistory(Player player) {
		if (previousGUI.containsKey(player.getUniqueId())) {
			previousGUI.get(player.getUniqueId()).clear();
		}
		selectedGroup.remove(player.getUniqueId());
		selectedPower.remove(player.getUniqueId());
		selectedUser.remove(player.getUniqueId());
	}
	
	public GUIAction getAction(int slot) {
		return actions.get(slot);
	}
	
	public static GUIAbstract getGUI(UUID uuid) {
		return currentGUI.get(uuid);
	}
	
	public void open(Player player) {
		player.openInventory(guiInv);
		currentGUI.put(player.getUniqueId(), this);
	}
	
	void openGUI(Player player, GUIAbstract gui) {
		player.closeInventory();
		if (!currentGUI.containsValue(gui)) {
			gui.reloadGUI();
		}
		plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				gui.open(player);
			}
		});
	}
	
	void openLast(Player player) {
		if (previousGUI.containsKey(player.getUniqueId())) {
			List<GUIAbstract> lastGUIs = previousGUI.get(player.getUniqueId());
			if (!lastGUIs.isEmpty()) {
				GUIAbstract lastGUI = lastGUIs.get(lastGUIs.size() - 1);
				lastGUIs.remove(lastGUIs.size() - 1);
				previousGUI.put(player.getUniqueId(), lastGUIs);
				openGUI(player, lastGUI);
			}
		}
	}
	
	void openNext(Player player, GUIAbstract nextGUI) {
		if (!previousGUI.containsKey(player.getUniqueId())) {
			previousGUI.put(player.getUniqueId(), new ArrayList<>());
		}
		previousGUI.get(player.getUniqueId()).add(this);
		openGUI(player, nextGUI);
	}
	
	void refresh() {}
	
	void reloadGUI() {
		actions.clear();
		guiInv.clear();
		refresh();
		setItems();
	}
	
	public void removeViewer(UUID uuid) {
		currentGUI.remove(uuid);
	}
	
	void setItem(int slot, Material material, String name, String text, GUIAction action) {
		setItem(slot, material, name, text != null ? Collections.singletonList(text) : null, action);
	}
	
	void setItem(int slot, Material material, String name, List<String> text, GUIAction action) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(material);
		meta.setDisplayName(ChatColor.RESET + name);
		if (text != null) {
			meta.setLore(text);
		}
		meta.addItemFlags(ItemFlag.values());
		item.setItemMeta(meta);
		guiInv.setItem(slot, item);
		if (action != null) {
			actions.put(slot, action);
		}
	}
	
	abstract void setItems();
	
}
