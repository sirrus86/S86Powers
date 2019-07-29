package me.sirrus86.s86powers.gui;

import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;

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
				String configDesc = LocaleString.getString(plugin.getConfigManager().getOptions().get(configOption).getName() + "_CONFIG");
				String configValue = plugin.getConfigManager().getConfigValue(configOption).toString();
				List<String> lore = PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), configDesc, 30);
				lore.add(ChatColor.RESET + LocaleString.VALUE.toString() + ": " + ChatColor.GRAY + configValue);
				setItem(i - index, LIST, configOption, lore, null);
			}
			setItem(48, BACK, LocaleString.BACK.toString(), (String) null, player -> {
				openLast(player);
			});
			if (page > 1) {
				setItem(49, PAGE, LocaleString.PAGE.toString() + " " + Integer.toString(page - 1), (String) null, player -> {
					openGUI(player, GUIConfig.CONFIG_LIST_GUI.get(page - 2));
				});
			}
			if (page < GUIConfig.CONFIG_LIST_GUI.size()) {
				setItem(50, PAGE, LocaleString.PAGE.toString() + " " + Integer.toString(page + 1), (String) null, player -> {
					openGUI(player, GUIConfig.CONFIG_LIST_GUI.get(page));
				});
			}
		}
	}
	
}
