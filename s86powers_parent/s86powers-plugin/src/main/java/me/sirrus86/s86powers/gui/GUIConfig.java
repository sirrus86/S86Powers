package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.tools.PowerTools;

public final class GUIConfig extends GUIAbstract {

	static List<GUIConfigList> CONFIG_LIST_GUI = new ArrayList<>();
	
	public GUIConfig() {
		super(2, LocaleString.CONFIG.toString());
		CONFIG_LIST_GUI = GUIAbstractList.createLists(GUIConfigList.class, S86Powers.getConfigManager().getConfigOptions().keySet());
		setItems();
	}
	
	@Override
	protected void setItems() {
		setItem(0, LIST, LocaleString.LIST.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.CONFIG_LIST_HELP.toString(), 30), player -> {
			if (player.hasPermission(S86Permission.CONFIG_LIST)) {
				openNext(player, CONFIG_LIST_GUI.get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers config list");
			}
		});
		setItem(1, RELOAD, LocaleString.RELOAD.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.CONFIG_RELOAD_HELP.toString(), 30), player -> {
			player.closeInventory();
			player.performCommand("powers config reload");
		});
		setItem(2, SAVE, LocaleString.SAVE.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.CONFIG_SAVE_HELP.toString(), 30), player -> {
			player.closeInventory();
			player.performCommand("powers config save");
		});
		setItem(12, BACK, LocaleString.BACK.toString(), (String) null, player -> {
			openLast(player);
		});
	}

}
