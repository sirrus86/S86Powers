package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.tools.PowerTools;

public final class GUIConfig extends GUIAbstract {

	static List<GUIConfigList> CONFIG_LIST_GUI = new ArrayList<>();
	
	final static ItemStack LIST = createItem(Material.FILLED_MAP, LocaleString.LIST.toString(), PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), LocaleString.CONFIG_LIST_HELP.toString(), 30)),
			RELOAD = createItem(Material.BOOK, LocaleString.RELOAD.toString(), PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), LocaleString.CONFIG_RELOAD_HELP.toString(), 30)),
			SAVE = createItem(Material.WRITABLE_BOOK, LocaleString.SAVE.toString(), PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), LocaleString.CONFIG_SAVE_HELP.toString(), 30));
	
	public GUIConfig() {
		super(2, LocaleString.CONFIG.toString());
		CONFIG_LIST_GUI = GUIAbstractList.createLists(GUIConfigList.class, S86Powers.getConfigManager().getConfigOptions().keySet());
		setItems();
	}
	
	@Override
	protected void setItems() {
		setItem(0, LIST, player -> {
			if (player.hasPermission(S86Permission.CONFIG_LIST)) {
				openNext(player, CONFIG_LIST_GUI.get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers config list");
			}
		});
		setItem(1, RELOAD, player -> {
			player.closeInventory();
			player.performCommand("powers config reload");
		});
		setItem(2, SAVE, player -> {
			player.closeInventory();
			player.performCommand("powers config save");
		});
		setItem(12, BACK, this::openLast);
	}

}
