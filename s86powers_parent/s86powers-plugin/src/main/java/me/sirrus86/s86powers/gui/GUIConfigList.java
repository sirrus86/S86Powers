package me.sirrus86.s86powers.gui;

import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.tools.PowerTools;

public class GUIConfigList extends GUIAbstractList<String> {
	
	public GUIConfigList(int page, Collection<String> list) {
		super(page, list);
	}
	
	@Override
	protected void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				String configOption = list.get(i);
				String configDesc = LocaleString.getString(configOption.substring(configOption.indexOf(".") + 1).toUpperCase().replaceAll("-", "_") + "_CONFIG");
				String configValue = S86Powers.getConfigManager().getConfigValue(configOption).toString();
				List<String> lore = PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), configDesc, 30);
				lore.add(ChatColor.RESET + LocaleString.VALUE.toString() + ": " + ChatColor.GRAY + configValue);
				setItem(i - index, new ItemStack(Material.PAPER), configOption, lore, null);
			}
			setItem(48, BACK, this::openLast);
			if (page > 1) {
				setItem(49, PAGE1, player -> openGUI(player, GUIConfig.CONFIG_LIST_GUI.get(page - 2)));
			}
			if (page < GUIConfig.CONFIG_LIST_GUI.size()) {
				setItem(50, PAGE2, player -> openGUI(player, GUIConfig.CONFIG_LIST_GUI.get(page)));
			}
		}
	}
	
}
