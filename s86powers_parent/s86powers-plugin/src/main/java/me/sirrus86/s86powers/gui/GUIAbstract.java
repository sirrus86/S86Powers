package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
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
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;

public abstract class GUIAbstract {

	final static S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	private final static String GUIHEADER = ChatColor.BOLD.toString() + ChatColor.GOLD + "S86 Powers" + ChatColor.RESET + " v" + plugin.getDescription().getVersion() + " by sirrus86";
	
	final static ItemStack BACK = createItem(Material.ARROW, LocaleString.BACK.toString(), null);
	
	Map<Integer, GUIAction> actions = new HashMap<>();
	private static final Map<UUID, GUIAbstract> currentGUI = new HashMap<>();
	private static final Map<UUID, List<GUIAbstract>> previousGUI = new HashMap<>();
	
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
	
	static ItemStack createItem(Material material, String name, List<String> text) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(item.getType());
		if (meta != null) {
			meta.setDisplayName(ChatColor.RESET + name);
			if (text != null
					&& !text.isEmpty()) {
				meta.setLore(text);
			}
			meta.addItemFlags(ItemFlag.values());
			item.setItemMeta(meta);
		}
		return item;
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
		plugin.getServer().getScheduler().runTask(plugin, () -> gui.open(player));
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
	
	void setItem(int slot, ItemStack item, GUIAction action) {
		guiInv.setItem(slot, item);
		if (action != null) {
			actions.put(slot, action);
		}
	}
	
	void setItem(int slot, ItemStack item, String name, List<String> text, GUIAction action) {
		ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(item.getType());
		if (meta != null) {
			meta.setDisplayName(ChatColor.RESET + name);
			if (text != null
					&& !text.isEmpty()) {
				meta.setLore(text);
			}
			meta.addItemFlags(ItemFlag.values());
			item.setItemMeta(meta);
		}
		guiInv.setItem(slot, item);
		if (action != null) {
			actions.put(slot, action);
		}
	}
	
	abstract void setItems();
	
}
